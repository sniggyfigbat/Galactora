package net.stefancbauer.galactora.Model;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import net.stefancbauer.galactora.LocalMaths.Vector2f;
import net.stefancbauer.galactora.Model.Engine.CollisionRectangle;

/**
 * A piece of armour, which is destroyed after taking a certain amount of damage.
 */

public class Armour extends GameObject {
    private boolean m_isPlayers = false; //!< Whether or not this armour piece belongs to the player, rather than an enemy.
    private int m_HP; //!< HP. If -1, invincible (should probably use a shield instead).
    public String m_typeName = ""; //!< Used to decide what gib effect to call on destruction.

    public boolean m_ownedByPlayer; //!< Whether or not this piece of armour is owned by player or by enemies, disabling direct friendly fire accordingly (explosions deal damage to everyone).

    private Armour(boolean isPlayers, int HP, boolean ownedByPlayer) //!< Private Constructor, use Factory functions.
    {
        super();
        m_chainsDeletion = false;
        m_isPlayers = isPlayers;
        m_HP = HP;
        m_ownedByPlayer = ownedByPlayer;
    }

    public static Armour getQueenArmour(boolean isLeftArm) {
        Armour returnArmour = new Armour(false, 2, false);

        if (isLeftArm) {
            returnArmour.m_sprite = new Sprite("game_foreground_spritesheet", new Rect(256, 128, 304, 208), 1, new Vector2f(0.75f, 1.25f), new Vector2f(1.0f));
            returnArmour.m_typeName = "queenleft";
            returnArmour.addCollisionable(new CollisionRectangle(0.375f, 0.5f), new Vector2f(-0.625f, -0.5f), 0.0f);
        } else {
            // Is Right Arm.
            returnArmour.m_sprite = new Sprite("game_foreground_spritesheet", new Rect(336, 128, 384, 208), 1, new Vector2f(0.75f, 1.25f), new Vector2f(-0.25f, 1.0f));
            returnArmour.m_typeName = "queenright";
            returnArmour.addCollisionable(new CollisionRectangle(0.375f, 0.5f), new Vector2f(0.625f, -0.5f), 0.0f);
        }

        return returnArmour;
    }

    public void dealDamage(int damage) {
        if (m_HP < -1) {
            m_HP -= damage;
            if (m_HP <= 0) { delete(); }
        }
    }

    @Override
    public void update() {}

    @Override
    public void draw(Canvas c, Paint p) { m_sprite.draw(c, p); }

    @Override
    public void delete(){
        super.delete();
        if (bmLink != null) { bmLink.addArmourGibs(this); }
    }

    public int getHP() { return m_HP; }
    public void addHP(int HP) {
        if (m_HP > -1) {
            m_HP += HP;
            if (m_HP <= 0) { m_HP = 0; delete(); }
        }
    }
    public void setHP(int HP) { m_HP = HP; }
}
