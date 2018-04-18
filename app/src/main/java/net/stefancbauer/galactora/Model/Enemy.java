package net.stefancbauer.galactora.Model;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import net.stefancbauer.galactora.LocalMaths.Vector2f;
import net.stefancbauer.galactora.Model.Background.BackgroundManager;
import net.stefancbauer.galactora.Model.Engine.CollisionCircle;
import net.stefancbauer.galactora.Model.Engine.CollisionRectangle;
import net.stefancbauer.galactora.Model.Engine.Weld;
import net.stefancbauer.galactora.Model.Pathing.Path;
import net.stefancbauer.galactora.Model.Pathing.PathNode;

import java.util.Random;
import java.util.Vector;

/**
 * Created by P13186907 on 23/02/2018.
 */

public class Enemy extends GameObject {
    public EnemyType m_type; //!< The type of enemy any instance is.
    private int m_HP; //!< How many hit points this enemy has.
    private int m_fireCooldown; //!< How long until this enemy will fire again.
    private int m_bulletsFiredThisBurst = 0; //!< Used by warriors to determine how many projectiles they've fired in this burst.

    public Vector<Projectile> pvLink = null; //!< Where spawned projectiles get put.
    public Random m_rand = null; //!< Random number generator.

    // Magic numbers and global difficulty changeables:
    public static float moveSpeed = 0.1f; //!< The maximum (and default) move speed of enemies.
    public static float rotateSpeed = 5.0f; //!< The maximum (and default) rotate speed of enemies.

    public static int droneFireCooldown = 640; //!< Minimum cooldown period between firing for drones.
    public static int warriorFireCooldown = 480; //!< Minimum cooldown period between firing bursts for warriors.
    public static int warriorBurstFireCooldown = 10; //!< Cooldown period between individual shots in a burst for warriors.
    public static int warriorBurstFireShots = 3; //!< Number of individual shots in a burst for warriors.
    public static int queenFireCooldown = 640; //!< Minimum cooldown period between firing for queens.

    public static int droneScore = 20;
    public static int warriorScore = 50;
    public static int guardianScore = 75;
    public static int queenScore = 100;

    // Behaviour Data
    public boolean inGridMode = true; //!< Whether enemy is is grid mode.
    public Vector2f unmodifiedGridPos = new Vector2f(0.0f, 16.0f); //!< The grid position of the enemy, before being modified by grid behaviours, relative to the grid centre.
    public Vector2f gridPos = new Vector2f(0.0f, 16.0f); //!< The desired grid position of the enemy, in game units.
    public boolean m_atStartPoint = false; //!< Whether the enemy has reached the start point of the path.
    public boolean m_mayFollowPath = false; //!< Whether all enemies in a pathgroup have reached their start points, and therefore path may be followed.
    public Path m_path = null; //!< The path the enemy is supposed to follow.
    public int m_pathNode = 1; //!< The next node of the path to head for.
    public boolean m_exiting = false; //!< Whether the enemy is exiting the level.

    private Enemy(Game game, EnemyType type) //!< Private Constructor, use Factories
    {
        super(game);
        m_rand = game.m_rand;
        m_fireCooldown = m_rand.nextInt(120) + 120;
        m_type = type;
    }

    public static Enemy getDrone(Game game, BackgroundManager bm, Vector<Projectile> pv, Vector2f position, float rotation) //!< Returns a properly set up Drone-type enemy at the position and rotation specified.
    {
        Enemy returnEnemy = new Enemy(game, EnemyType.DRONE);

        returnEnemy.m_sprite = new Sprite("game_foreground_spritesheet", new Rect(128, 0, 192, 64), 1, new Vector2f(1.0f), new Vector2f(0.5f));
        returnEnemy.addCollisionable(new CollisionRectangle(0.5f, 0.25f), new Vector2f(0.0f, -0.25f), 0.0f);
        returnEnemy.addCollisionable(new CollisionCircle(0.4375f), new Vector2f(0.0f, -0.0625f), 0.0f);
        returnEnemy.m_HP = 1;

        returnEnemy.bmLink = bm;
        returnEnemy.pvLink = pv;

        returnEnemy.updateState(position, rotation, new Vector<GameObject>());

        return returnEnemy;
    }

    public static Enemy getWarrior(Game game, BackgroundManager bm, Vector<Projectile> pv, Vector2f position, float rotation) //!< Returns a properly set up Warrior-type enemy at the position and rotation specified.
    {
        Enemy returnEnemy = new Enemy(game, EnemyType.WARRIOR);

        returnEnemy.m_sprite = new Sprite("game_foreground_spritesheet", new Rect(128, 64, 192, 128), 1, new Vector2f(1.0f), new Vector2f(0.5f));
        returnEnemy.addCollisionable(new CollisionCircle(0.5f), new Vector2f(), 0.0f);
        returnEnemy.m_HP = 2;

        returnEnemy.bmLink = bm;
        returnEnemy.pvLink = pv;

        returnEnemy.updateState(position, rotation, new Vector<GameObject>());

        return returnEnemy;
    }

    public static Enemy getGuardian(Game game, BackgroundManager bm, Vector<Projectile> pv, Vector2f position, float rotation, Vector<Shield> shields) //!< Returns a properly set up Guardian-type enemy at the position and rotation specified, places its shield in the given vector.
    {
        Enemy returnEnemy = new Enemy(game, EnemyType.GUARDIAN);

        returnEnemy.m_sprite = new Sprite("game_foreground_spritesheet", new Rect(192, 64, 256, 128), 1, new Vector2f(1.0f), new Vector2f(0.5f));
        returnEnemy.addCollisionable(new CollisionCircle(0.5f), new Vector2f(0.0f, 0.125f), 0.0f);
        returnEnemy.m_HP = 1;

        returnEnemy.bmLink = bm;
        returnEnemy.pvLink = pv;

        Shield returnShield = Shield.getGuardianShield();
        returnShield.bmLink = bm;
        Weld.weldObjects(returnEnemy, returnShield);
        shields.addElement(returnShield);

        returnEnemy.updateState(position, rotation, new Vector<GameObject>());

        return returnEnemy;
    }

    public static Enemy getQueen(Game game, BackgroundManager bm, Vector<Projectile> pv, Vector2f position, float rotation, Vector<Armour> armours) //!< Returns a properly set up Queen-type enemy at the position and rotation specified, places its armour pieces in the given vector.
    {
        Enemy returnEnemy = new Enemy(game, EnemyType.QUEEN);

        returnEnemy.m_sprite = new Sprite("game_foreground_spritesheet", new Rect(256, 0, 384, 128), 1, new Vector2f(2.0f), new Vector2f(1.0f));
        returnEnemy.addCollisionable(new CollisionRectangle(0.375f, 0.375f), new Vector2f(0.0f, 0.5f), 45.0f);
        returnEnemy.addCollisionable(new CollisionCircle(0.625f), new Vector2f(0.0f, 0.125f), 0.0f);
        returnEnemy.addCollisionable(new CollisionCircle(0.3125f), new Vector2f(0.0f, -0.5f), 0.0f);
        returnEnemy.m_HP = 3;

        returnEnemy.bmLink = bm;
        returnEnemy.pvLink = pv;

        Armour returnLeftArmour = Armour.getQueenArmour(true);
        returnLeftArmour.bmLink = bm;
        Weld.weldObjects(returnEnemy, returnLeftArmour);
        armours.addElement(returnLeftArmour);
        Armour returnRightArmour = Armour.getQueenArmour(false);
        returnRightArmour.bmLink = bm;
        Weld.weldObjects(returnEnemy, returnRightArmour);
        armours.addElement(returnRightArmour);

        returnEnemy.updateState(position, rotation, new Vector<GameObject>());

        return returnEnemy;
    }

    public int getHP() { return m_HP; }
    public void addHP(int HP) {
        if (m_HP > -1) {
            m_HP += HP;
            if (m_HP <= 0) { m_HP = 0; delete(); }
        }
    }
    public void setHP(int HP) { m_HP = HP; }

    @Override
    public void update() {
        // Handle Movement
        if (inGridMode) {
            // In grid mode

            // Handle rotation
            float relativeAngle = sanitiseAngle(0.0f - getRotation()); // When in grid mode, enemies should try to be at zero rotation.
            float newAngle = 0.0f;
            if (Math.abs(relativeAngle) < rotateSpeed) { newAngle = 0.0f; }
            else if (relativeAngle < 0.0f ) { newAngle = sanitiseAngle(getRotation() - rotateSpeed, true); }
            else if (relativeAngle > 0.0f ) { newAngle = sanitiseAngle(getRotation() + rotateSpeed, true); }

            // Handle movement and pathing.
            Vector2f newPos = new Vector2f(gridPos);
            if (!Vector2f.areEqual(getPosition(), gridPos)) {
                Vector2f relativePos = Vector2f.subtract(gridPos, getPosition());
                if (relativePos.getMagnitudeSquared() <= (moveSpeed * moveSpeed)) { newPos.set(gridPos); }
                else { newPos.set(Vector2f.add(getPosition(), relativePos.getUnitVector().multiply(moveSpeed))); }
            }

            // Apply the data
            updateState(newPos, newAngle, new Vector<GameObject>());
        } else {
            // In path mode

            boolean reachedEndOfPath = false;
            if (!m_atStartPoint) {
                if (moveTowards(m_path.nodes.get(0).coords, moveSpeed, 1.0f) > 0.0f) { m_atStartPoint = true; }
            } else if (m_mayFollowPath) {
                // Path from node to node.
                float proportionLeft = 1.0f;
                while (!reachedEndOfPath && proportionLeft > 0.0f) {
                    if (m_pathNode >= m_path.nodes.size()) { reachedEndOfPath = true; }
                    else {
                        PathNode node = m_path.nodes.get(m_pathNode);

                        proportionLeft = moveTowards(node.coords, node.getTravelSpeed(), proportionLeft);
                        if (proportionLeft > 0.0f) { m_pathNode++; }
                    }
                }
            }
            if (reachedEndOfPath) {
                m_path.m_toBeDestroyed = true;
                m_path = null;
                m_pathNode = 1;
                m_atStartPoint = false;
                m_mayFollowPath = false;
                inGridMode = true;

                if (m_exiting) { delete(); }
            }
        }

        // Handle firing
        if (m_fireCooldown > 0) { m_fireCooldown--; }
        if (m_fireCooldown == 0  && m_type != EnemyType.GUARDIAN) {
            // Out of cooldown! Fire at will!
            if (inGridMode) {
                // Check if player is within 1GU of x coord. If yes, is an aimed shot. If not, it's a bombardment shot.
                float playerX = gameLink.m_player.getPosition().x;
                if (getPosition().x + 2.0f > playerX && getPosition().x - 2.0f < playerX) { fireProjectile(false); }
                else { fireProjectile(true); }
            } else {
                // Check if line of fire is on player, plus or minus a few degrees.
                Vector2f relPlayerPos = Vector2f.subtract(gameLink.m_player.getPosition(), getPosition());
                float perfectRotation = ((float)Math.toDegrees(Math.atan2(relPlayerPos.y, relPlayerPos.x)) - 90.0f) % 360.0f;
                perfectRotation = perfectRotation < 0.0f ? perfectRotation + 360.0f : perfectRotation;
                float currentFireRotation = (getRotation()  - 180.0f) % 360.0f;
                currentFireRotation = currentFireRotation < 0.0f ? currentFireRotation + 360.0f : currentFireRotation;
                if (currentFireRotation + 2.0f > perfectRotation && currentFireRotation - 2.0f < perfectRotation) { fireProjectile(false); }
                else if (m_type == EnemyType.WARRIOR && m_bulletsFiredThisBurst > 0) { fireProjectile(true); }
            }
        }
    }

    private float moveTowards(Vector2f destPoint, float inp_speed, float inp_proportionLeft) // Moves the enemy towards a point. Returns the proportion of moveable distance left, from 0.0f to 1.0f;
    {
        float returnFloat = 0.0f;
        if (!Vector2f.areEqual(getPosition(), destPoint)) {
            float speed = Math.min(inp_speed, moveSpeed); // Limit to max movespeed.
            float dist = speed * inp_proportionLeft;

            Vector2f relative = Vector2f.subtract(destPoint, getPosition());
            float relativeAngle = (float) Math.toDegrees(Math.atan2(relative.y, relative.x));

            // Handle rotation
            float actualAngle = getRotation() - 90.0f;
            float evenMoreRelativeAngle = sanitiseAngle(relativeAngle - actualAngle); // When in grid mode, enemies should try to be at zero rotation.
            float newAngle = getRotation();
            if (Math.abs(evenMoreRelativeAngle) < rotateSpeed) {
                newAngle = relativeAngle + 90;
            } else if (evenMoreRelativeAngle < 0.0f) {
                newAngle = sanitiseAngle(getRotation() - rotateSpeed, true);
            } else if (evenMoreRelativeAngle > 0.0f) {
                newAngle = sanitiseAngle(getRotation() + rotateSpeed, true);
            }

            // Handle movement and pathing.
            Vector2f newPos = new Vector2f();
            if (relative.getMagnitudeSquared() <= (dist * dist)) {
                newPos.set(destPoint);
                returnFloat = inp_proportionLeft * ((dist - relative.getMagnitude()) / dist);
            } else {
                newPos.set(Vector2f.add(getPosition(), relative.getUnitVector().multiply(dist)));
            }

            // Apply the data
            updateState(newPos, newAngle, new Vector<GameObject>());
        }
        else { returnFloat = 1.0f; }
        return returnFloat;
    }

    @Override
    public void draw(Canvas c, Paint p) {
        m_sprite.draw(c, p);

        /*if (isWelded()){
            Vector<GameObject> weldeds = Weld.getWelded(this);
            for (GameObject obj: weldeds) {
                if (obj instanceof Shield) { obj.draw(c, p); }
            }
        }*/ // Don't actually need to do this, now gets handled in Game.
    }

    @Override
    public void delete(){
        super.delete();

        float temp = 0.0f;
        if (this.m_type == EnemyType.DRONE) {temp = droneScore; }
        else if (this.m_type == EnemyType.WARRIOR) {temp = warriorScore; }
        else if (this.m_type == EnemyType.GUARDIAN) {temp = guardianScore; }
        else if (this.m_type == EnemyType.QUEEN) {temp = queenScore; }

        gameLink.addScore((int)(temp * gameLink.m_difficultyMultiplier));

        if (bmLink != null) { bmLink.addEnemyGibs(this); }
    }

    public void fireProjectile(boolean bombardmentShot) //!< Fires a bullet and resets countdown. If bullet is a bombardment, it means player is not in line of fire, so add double the cooldown.
    {
        switch (m_type) {
            case DRONE:
                pvLink.addElement(Projectile.getGreenBolt(getPosition(), getRotation() + 180.0f));
                pvLink.lastElement().bmLink = bmLink;

                if (!bombardmentShot) { m_fireCooldown = (int)((m_rand.nextFloat() + 1.0f) * Enemy.droneFireCooldown); }
                else { m_fireCooldown = (int)((m_rand.nextFloat() + 2.0f) * Enemy.droneFireCooldown); }
                break;
            case WARRIOR:
                pvLink.addElement(Projectile.getGreenBolt(getPosition(), getRotation() + 180.0f));
                pvLink.lastElement().bmLink = bmLink;

                m_bulletsFiredThisBurst++;
                if (m_bulletsFiredThisBurst < Enemy.warriorBurstFireShots) { m_fireCooldown = Enemy.warriorBurstFireCooldown; }
                else {
                    if (!bombardmentShot) { m_fireCooldown = (int)((m_rand.nextFloat() + 1.0f) * Enemy.warriorFireCooldown); }
                    else { m_fireCooldown = (int)((m_rand.nextFloat() + 2.0f) * Enemy.warriorFireCooldown); }
                    m_bulletsFiredThisBurst = 0;
                }
                break;
            case GUARDIAN:
                // Do nothing. It has a shield, not a gun.
                break;
            case QUEEN:
                if (!bombardmentShot) {
                    // Only fire aimed, non-bombardment shots. Bombs are too OP to spam.
                    float distance = Vector2f.subtract(gameLink.m_player.getPosition(), getPosition()).getMagnitude();
                    pvLink.addElement(Projectile.getYellowBomb(getPosition(), getRotation() + 180.0f, distance));
                    pvLink.lastElement().bmLink = bmLink;
                    m_fireCooldown = (int)((m_rand.nextFloat() + 1.0f) * Enemy.queenFireCooldown);
                }
                break;
        }
    }
}
