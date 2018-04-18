package net.stefancbauer.galactora.Model;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import net.stefancbauer.galactora.LocalMaths.Vector2f;
import net.stefancbauer.galactora.Model.Engine.CollisionCircle;
import net.stefancbauer.galactora.Model.Engine.CollisionRectangle;
import net.stefancbauer.galactora.Model.Engine.Weld;

import java.util.Vector;

/**
 * Handles the player ship and associated functionality.
 */

public class PlayerShip extends GameObject {
    public Vector2f m_lastDestPos = new Vector2f(); //!< The destination the player ship is moving towards.
    public float m_maxMoveSpeed; //!< The maximum speed at which the player ship will move towards its indicated destination

    public int m_boltCooldownMax = 30; //!< The minimum length between firing bolts.
    public int m_boltCooldown = 0; //!< The cooldown before firing the next bolt.

    public final int m_shieldChargesMax = 3; //!< The maximum number of shield charges the player can store.
    public int m_shieldCharges = 3; //!< The current number of shield charges available to the player.
    public Shield m_shield = null; //!< reference to the current shield, to prevent stacking it.
    private int m_shieldLifespan = 60; //!< MAGIC: How long the player's shield lasts for.

    public final int m_bombChargesMax = 3; //!< The maximum number of bomb charges the player can store.
    public int m_bombCharges = 3; //!< The current number of bomb charges available to the player.
    public int m_bombCooldownMax = 60; //!< The minimum length between firing bombs.
    public int m_bombCooldown = 0; //!< The cooldown before firing the next bomb.

    public PlayerShip(Game inp_game, Vector2f position) //!< Full constructor.
    {
        gameLink = inp_game;

        m_sprite = new Sprite("game_foreground_spritesheet", new Rect(0, 0, 128, 128), 1, new Vector2f(2.0f), new Vector2f(1.0f));
        addCollisionable(new CollisionCircle(0.25f), new Vector2f(0.0f, 0.5f), 0.0f);
        addCollisionable(new CollisionRectangle(0.625f, 0.5f), new Vector2f(0.0f, -0.25f), 0.0f);
        setPosition(new Vector2f(0.0f, -5.0f));
        m_maxMoveSpeed = 0.2f; // MAGIC
    }

    @Override
    public void update() { update(m_lastDestPos);} //!< Should not be called, but will use last known destination if it is.

    public void update(Vector2f destPos) {
        m_lastDestPos.set(destPos);
        Vector2f relativePos = Vector2f.subtract(destPos, getPosition());
        float relativeDistanceSquared = relativePos.getMagnitudeSquared();
        Vector2f newPos = new Vector2f(getPosition());

        if (m_boltCooldown > 0) { m_boltCooldown--; }
        if (m_bombCooldown > 0) { m_bombCooldown--; }

        if (relativeDistanceSquared > 0.0f) {
            if (relativeDistanceSquared < (m_maxMoveSpeed * m_maxMoveSpeed)) { setPosition(destPos); }
            else {
                relativePos.setMagnitude(m_maxMoveSpeed);
                newPos = Vector2f.add(newPos, relativePos);
            }
        }

        setPosition(newPos);
    }

    @Override
    public void draw(Canvas c, Paint p) {
        m_sprite.draw(c, p);
    }

    @Override
    public void delete(){
        super.delete();
        //if (bmLink != null) { bmLink.addEnemyGibs(this); } // TODO: Make player gibs? Handle Life stuff? That kinda shit.
    }

    public void takeDamage(Vector<Shield> shields) // Player takes damage.
    {
        gameLink.addScore(-500);

        m_shield = Shield.getPlayerShield(m_shieldLifespan);
        Weld.weldObjects(this, m_shield);
        shields.addElement(m_shield);
    }

    public void addShieldCharge() { if (m_shieldCharges < m_shieldChargesMax) { m_shieldCharges++; } }
    public void triggerShield(Vector<Shield> shields) {
        if (m_shield == null && m_shieldCharges > 0) {
            m_shield = Shield.getPlayerShield(m_shieldLifespan);
            m_shieldCharges--;
            Weld.weldObjects(this, m_shield);
            shields.addElement(m_shield);
        }
    }
    public void addBombCharge() { if (m_bombCharges < m_bombChargesMax) { m_bombCharges++; } }
    public boolean canFireBomb() {
        if (m_bombCharges > 0 && m_bombCooldown == 0) { return true; }
        else {return false; }
    }
}
