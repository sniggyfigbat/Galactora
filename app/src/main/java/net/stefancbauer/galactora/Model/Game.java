package net.stefancbauer.galactora.Model;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Pair;

import net.stefancbauer.galactora.LocalMaths.Vector2f;
import net.stefancbauer.galactora.Model.Background.BackgroundManager;
import net.stefancbauer.galactora.Model.Engine.Weld;
import net.stefancbauer.galactora.Model.Level.Level;
import net.stefancbauer.galactora.View.GameSurfaceView;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

/**
 * Handles Game Logic.
 */

public class Game {
    private final Point screenSize; //!< The size of the screen, in PIXELS! F***! WHY THE F*** IS ANDROID SUCH AN INCONSISTENT P**** OF ****?!! USE DP OR PX, BUT PICK ONE AND ONLY ONE!
    public final Random m_rand = new Random(); //!< The one true holy source of random numbers.
    private final Context m_context;

    public PlayerShip m_player;

    private final Vector<Pair<Vector2f, Boolean>> m_stackedEvents = new Vector<Pair<Vector2f, Boolean>>(); //!< Stacked up events, to be handled in update().
    private final Vector2f m_lastMovePosition = new Vector2f(0.0f, 3.0f); //!< The last indicated move m_position, eg. the destination point for the player's ship. In GU.
    private final Sprite m_uiPanel;
    private final Map<String, Button> m_buttons = new HashMap<>(); //!< All on-screen UI m_buttons

    // Texture references
    private BackgroundManager m_backgroundManager;

    public boolean m_paused = false;

    public boolean m_gameOver = false;
    private Vector<String> m_levelFiles = new Vector<String>();
    private int m_levelIndex;
    private Level m_level;

    public int m_score = 0;
    public int m_refillCounter = 0;
    public float m_difficultyMultiplier = 1.0f;

    public Vector<Enemy> m_enemies = new Vector<Enemy>();
    public Vector<Shield> m_shields = new Vector<Shield>();
    public Vector<Armour> m_armours = new Vector<Armour>();
    public Vector<Projectile> m_projectiles = new Vector<Projectile>();
    public Vector<Explosion> m_explosions = new Vector<Explosion>();

    // Grid-behaviour variables.
    private boolean m_finishedManouevre = true;
    private boolean m_movingLeft = false;
    private boolean m_gettingLarger = false;
    public float m_gridOffset = 0.0f;
    public float m_spacingMult = 0.0f;
    public float m_gridYMin = 0.0f; // Goes well into the negatives.


    public Game(Point screenSize, Context context) {
        this.m_context = context;
        this.screenSize = new Point(screenSize);

        m_player = new PlayerShip(this, new Vector2f(0.0f, -5.0f));

        m_backgroundManager = new BackgroundManager(m_context, m_rand);

        // Make UI
        //public Button(String textureName, Rect baseTextureRect, int frameCount, Vector2f bottomLeft, Vector2f topRight, Object object, ButtonBehaviour behaviour)
        m_uiPanel = new Sprite("game_ui_control_panel", new Rect(0, 1536, 1280, 1856), 1, new Vector2f(20.0f, 5.0f), new Vector2f(10.0f, 2.5f), new Vector2f(0.0f, -2.5f));
        m_buttons.put("bombbutton", new Button("game_ui_control_panel", new Rect(0, 0, 304, 256), 6, new Vector2f(-6.75f, -4.75f), new Vector2f(-2.0f, -0.75f), null, ButtonBehaviour.ONRELEASE));
        m_buttons.put("autobutton", new Button("game_ui_control_panel", new Rect(304, 816, 560, 960), 5, new Vector2f(-2.0f, -2.5f), new Vector2f(2.0f, -0.25f), null, ButtonBehaviour.TOGGLE));
        m_buttons.put("pausebutton", new Button("game_ui_control_panel", new Rect(864, 656, 1120, 832), 5, new Vector2f(-2.0f, -5.25f), new Vector2f(2.0f, -2.5f), null, ButtonBehaviour.TOGGLE));
        m_buttons.put("shieldbutton", new Button("game_ui_control_panel", new Rect(560, 0, 864, 256), 6, new Vector2f(2.0f, -4.75f), new Vector2f(6.75f, -0.75f), null, ButtonBehaviour.ONRELEASE));

        m_levelFiles.addElement("Levels/level_01");
        m_levelFiles.addElement("Levels/level_02");
        m_levelFiles.addElement("Levels/level_03");
        loadLevel(0);
    }

    public void update (long timeDiff) {
        // Update Buttons.
        for (Button button: m_buttons.values()) { button.update(); }
        handleEventStack();

        // Handle Button Input
        if (m_buttons.get("shieldbutton").poll()) { m_player.triggerShield(m_shields); }
        if (m_buttons.get("bombbutton").poll() && m_player.canFireBomb()) {
            m_player.m_bombCharges--;
            m_player.m_bombCooldown = m_player.m_bombCooldownMax;
            m_projectiles.addElement(Projectile.getRedBomb(m_player.getPosition(), m_player.getRotation(), m_buttons.get("bombbutton").getPressedLength()));
            m_projectiles.lastElement().bmLink = m_backgroundManager;
        }
        if (m_buttons.get("autobutton").poll() && m_player.m_boltCooldown == 0) {
            m_player.m_boltCooldown = m_player.m_boltCooldownMax;
            m_projectiles.addElement(Projectile.getRedBolt(m_player.getPosition(), m_player.getRotation()));
            m_projectiles.lastElement().bmLink = m_backgroundManager;
        }
        m_paused = m_buttons.get("pausebutton").poll();
        //for (Button button: m_buttons.values()) { boolean temp = button.poll(); } // Remove this line once all buttons are being used correctly!

        if (!m_paused) {
            // Update background
            m_backgroundManager.update();

            // Check for level progression
            if (m_level.m_levelComplete) {
                loadLevel(m_levelIndex + 1);
            }

            // Game object updates
            if (!m_gameOver) {

                updateGrid();
                m_level.update();
                m_player.update(m_lastMovePosition);

                for (Enemy enemy : m_enemies) {
                    enemy.update();
                }

                //for (Armour armour: m_armours) { armour.update(); } // No point at present.
                for (Shield shield : m_shields) {
                    shield.update();
                }
                for (Projectile projectile : m_projectiles) {
                    projectile.update();
                    if (projectile.isReadyToDetonate()) {
                        projectile.explode(m_explosions);
                    }
                }

                // Collisions
                Vector2f collisionNormalHolder = new Vector2f();
                Vector2f oldVelocityHolder = new Vector2f();
                Vector2f newVelocityHolder;

                // Projectile Shield Reflections
                for (Shield shield : m_shields) {
                    for (Projectile projectile : m_projectiles) {
                        if (projectile.m_ownedByPlayer != shield.m_ownedByPlayer &&
                                !projectile.m_reflected &&
                                !projectile.m_detonated &&
                                projectile.checkCollision(shield, collisionNormalHolder)) {
                            oldVelocityHolder.set(projectile.m_velocity);
                            collisionNormalHolder.multiply(((collisionNormalHolder.x * oldVelocityHolder.x) + (collisionNormalHolder.y * oldVelocityHolder.y)) * -2.0f);
                            newVelocityHolder = Vector2f.add(oldVelocityHolder, collisionNormalHolder);
                            projectile.m_velocity.set(newVelocityHolder);
                            projectile.m_reflected = true;
                            projectile.m_ownedByPlayer = !projectile.m_ownedByPlayer;
                        }
                    }
                }
                // Projectile vs Armour
                for (Armour armour : m_armours) {
                    for (Projectile projectile : m_projectiles) {
                        if (projectile.m_ownedByPlayer != armour.m_ownedByPlayer &&
                                !projectile.m_reflected &&
                                !projectile.m_detonated &&
                                !armour.isToBeDestroyed() &&
                                projectile.checkCollision(armour, collisionNormalHolder)) {
                            if (projectile.getExplosionRadius() > 0.0f) {
                                projectile.explode(m_explosions);
                            } else {
                                armour.addHP(-projectile.getDamage());
                                projectile.explode(m_explosions);
                            }
                        }
                    }
                }
                // Projectile vs Enemies
                for (Enemy enemy : m_enemies) {
                    for (Projectile projectile : m_projectiles) {
                        if (projectile.m_ownedByPlayer &&
                                !projectile.m_reflected &&
                                !projectile.m_detonated &&
                                !enemy.isToBeDestroyed() &&
                                projectile.checkCollision(enemy, collisionNormalHolder)) {
                            if (projectile.getExplosionRadius() > 0.0f) {
                                projectile.explode(m_explosions);
                            } else {
                                enemy.addHP(-projectile.getDamage());
                                projectile.explode(m_explosions);
                            }
                        }
                    }
                }
                // Projectile vs Player
                for (Projectile projectile : m_projectiles) {
                    if (!projectile.m_ownedByPlayer &&
                            !projectile.m_reflected &&
                            !projectile.m_detonated &&
                            !m_player.isToBeDestroyed() &&
                            projectile.checkCollision(m_player, collisionNormalHolder)) {
                        if (projectile.getExplosionRadius() > 0.0f) {
                            projectile.explode(m_explosions);
                        } else {
                            m_player.takeDamage(m_shields);
                            projectile.explode(m_explosions);
                        }
                    }
                }
                // Explosions vs Enemies & Armour & Player
                for (Explosion explosion : m_explosions) {
                    for (Enemy enemy : m_enemies) {
                        if (!enemy.isToBeDestroyed() && enemy.checkCollision(explosion, collisionNormalHolder)) {
                            enemy.addHP(-explosion.m_damage);
                        }
                    }
                    for (Armour armour : m_armours) {
                        if (!armour.isToBeDestroyed() && armour.checkCollision(explosion, collisionNormalHolder)) {
                            armour.addHP(-explosion.m_damage);
                        }
                    }
                    if (!m_player.isToBeDestroyed() && m_player.checkCollision(explosion, collisionNormalHolder)) {
                        m_player.takeDamage(m_shields);
                    }
                }
            }
        }


        // End update with cleanup of items marked for deletion.
        Weld.deleteWelds(); // Should go first.
        for (int i = m_enemies.size() - 1; i >= 0; i--) { if (m_enemies.get(i).isToBeDestroyed()) { m_enemies.remove(i); } }
        for (int i = m_shields.size() - 1; i >= 0; i--) { if (m_shields.get(i).isToBeDestroyed()) { m_shields.remove(i); } }
        for (int i = m_armours.size() - 1; i >= 0; i--) { if (m_armours.get(i).isToBeDestroyed()) { m_armours.remove(i); } }
        for (int i = m_projectiles.size() - 1; i >= 0; i--) { if (m_projectiles.get(i).isToBeDestroyed()) { m_projectiles.remove(i); } }
        m_explosions.clear();
        // TODO: Delete Collisionables?
    }

    public void draw (Canvas canvas, Paint paint) {
        // Background
        m_backgroundManager.drawBackground(canvas, paint);

        // Gameplay
        for (Projectile projectile : m_projectiles) { projectile.draw(canvas, paint); }
        m_player.draw(canvas, paint);
        for (Enemy enemy : m_enemies) { enemy.draw(canvas, paint); }
        for (Armour armour: m_armours) { armour.draw(canvas, paint); }
        for (Shield shield: m_shields) { shield.draw(canvas, paint); }

        // Foreground
        m_backgroundManager.drawForeground(canvas, paint);

        // UI
        m_uiPanel.draw(canvas, paint);
        for (Button button: m_buttons.values()) { button.draw(canvas, paint); }
    }

    private void loadLevel(int inp_newLevelIndex) {
        // Full Reset
        if (!m_gameOver) {
            for (int i = 0; i < m_enemies.size(); i++) {
                m_enemies.get(i).delete();
            }
            for (int i = 0; i < m_projectiles.size(); i++) {
                m_projectiles.get(i).delete();
            }

            if (inp_newLevelIndex != 0) {
                if (!m_level.m_hasAnEnemyEscaped) {
                    addScore(1000);
                }
            }

            if (inp_newLevelIndex < m_levelFiles.size()) {
                m_levelIndex = inp_newLevelIndex;
                m_level = new Level(m_context, m_levelFiles.get(m_levelIndex), this, m_backgroundManager);

                m_difficultyMultiplier = 1.0f + (0.1f * (m_level.m_difficultyRating - 1.0f));
            } else {
                // End of game.
                m_buttons.get("shieldbutton").setActive(false);
                m_buttons.get("bombbutton").setActive(false);
                m_gameOver = true;
                m_backgroundManager.addLevelEffect("victory", Integer.toString(m_score));
            }
        }
    }

    private void updateGrid() {
        float deltaMult = 0.005f;
        float deltaOffset = 0.025f;

        // Grid behaviour
        if (m_finishedManouevre) {
            // Pick a new behaviour.
            int choice = m_rand.nextInt(3);
            if (choice == 0) {
                // Spread out
                m_gettingLarger = true;
                m_spacingMult += deltaMult;
            }
            else if (choice == 1) {
                // Go right
                m_gridOffset += deltaOffset;
                m_movingLeft = false;
            }
            else if (choice == 2) {
                // Go left
                m_gridOffset -= deltaOffset;
                m_movingLeft = true;
            }

            m_finishedManouevre = false;
        }
        else if (m_spacingMult != 0.0f) {
            // Spreading out/in.
            if (m_gettingLarger) {
                m_spacingMult += deltaMult;
                if (m_spacingMult >= 0.4f) {
                    m_spacingMult = 0.4f;
                    m_gettingLarger = false;
                }
            } else {
                m_spacingMult -= deltaMult;
                if (m_spacingMult <= 0.0f) {
                    m_spacingMult = 0.0f;
                    m_finishedManouevre = true;
                }
            }
        }
        else if (m_gridOffset > 0.0f) {
            // Moving right and back.
            if (!m_movingLeft) {
                m_gridOffset += deltaOffset;
                if (m_gridOffset >= 2.0f) {
                    m_gridOffset = 2.0f;
                    m_movingLeft = true;
                }
            } else {
                m_gridOffset -= deltaOffset;
                if (m_gridOffset <= 0.0f) {
                    m_gridOffset = 0.0f;
                    m_finishedManouevre = true;
                }
            }
        }
        else if (m_gridOffset < 0.0f) {
            // Moving left and back.
            if (m_movingLeft) {
                m_gridOffset -= deltaOffset;
                if (m_gridOffset <= -2.0f) {
                    m_gridOffset = -2.0f;
                    m_movingLeft = false;
                }
            } else {
                m_gridOffset += deltaOffset;
                if (m_gridOffset >= 0.0f) {
                    m_gridOffset = 0.0f;
                    m_finishedManouevre = true;
                }
            }
        }

        // Updating enemies.
        m_gridYMin = 0.0f;
        Vector2f gridCentre = new Vector2f(m_gridOffset, 16.0f);
        float multiplier = 1.0f + m_spacingMult;
        for (Enemy enemy : m_enemies) {
            Vector2f unmod = enemy.unmodifiedGridPos;
            if (unmod.y < m_gridYMin) { m_gridYMin = unmod.y; }
            enemy.gridPos.set((unmod.x * multiplier) + gridCentre.x, unmod.y  + gridCentre.y);
        }
    }

    public void addScore(int addition) {
        m_score += addition;
        m_refillCounter += addition;

        if (addition > 0) { m_backgroundManager.addLevelEffect("add_score", Integer.toString(addition)); }
        else if (addition < 0) { m_backgroundManager.addLevelEffect("subtract_score", Integer.toString(Math.abs(addition))); }

        m_refillCounter = Math.max(m_refillCounter, 0);

        if (m_refillCounter > 500) {
            m_player.m_bombCharges = Math.min(m_player.m_bombCharges + 1, m_player.m_bombChargesMax);
            m_player.m_shieldCharges = Math.min(m_player.m_shieldCharges + 1, m_player.m_shieldChargesMax);
            m_refillCounter = m_refillCounter % 500;
            m_backgroundManager.addLevelEffect("add_charges", "");
        }
    }

    public void addEvent(Vector2f position, boolean touch) {
        position.x = Math.min(Math.max(position.x, 0.0f), (float)screenSize.x);
        position.y = Math.min(Math.max(position.y, 0.0f), (float)screenSize.y);

        Vector2f guPosition = GameSurfaceView.DPtoGU(position.toInteger());
        m_stackedEvents.addElement(new Pair<Vector2f, Boolean>(guPosition, touch));
    }

    public void handleEventStack() //!< When called, handles everything in m_stackedEvents
    {
        Vector2f newLastMovePosition = new Vector2f(m_lastMovePosition);
        for (int i = 0; i < m_stackedEvents.size(); i++) {
            boolean buttonPress = false;
            for (Map.Entry<String, Button> entry : m_buttons.entrySet()) {
                if (m_stackedEvents.get(i).second) {
                    if (entry.getValue().press(m_stackedEvents.get(i).first)) { buttonPress = true; }
                } else {
                    entry.getValue().release(m_stackedEvents.get(i).first);
                }
            }

            if (!buttonPress && m_stackedEvents.get(i).second) {
                newLastMovePosition.set(m_stackedEvents.get(i).first);
                newLastMovePosition.y += 1.0f;
            }
        }

        m_stackedEvents.clear();
        if (newLastMovePosition.y < 1.0f) { newLastMovePosition.y = 1.0f; }
        m_lastMovePosition.set(newLastMovePosition);
    }
}
