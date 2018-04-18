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
 * A shield, which reflects incoming projectiles.
 */

public class Shield extends GameObject {
    private boolean m_isPlayers = false; //!< Whether or not this shield belongs to the player, rather than an enemy.
    private int m_lifespan; //!< Lifespan in ticks. If -1, lasts forever.

    public boolean m_ownedByPlayer; //!< Whether or not this piece of armour is owned by player or by enemies, disabling direct friendly fire accordingly (explosions deal damage to everyone).

    private Shield(boolean isPlayers, int lifespan, boolean ownedByPlayer) //!< Private Constructor, use Factory functions.
    {
        super();
        m_chainsDeletion = false;
        m_isPlayers = isPlayers;
        m_lifespan = lifespan;
        m_ownedByPlayer = ownedByPlayer;
    }

    public static Shield getPlayerShield(int lifespan) //!< Returns a player shield, centred correctly, with the correct sprite and collisionables.
    {
        Shield returnShield = new Shield(true, lifespan, true);

        returnShield.m_sprite = new Sprite("game_foreground_spritesheet", new Rect(384, 0, 512, 128), 4, new Vector2f(2.0f), new Vector2f(1.0f));
        returnShield.addCollisionable(new CollisionCircle(1.0f), new Vector2f(), 0.0f);

        return returnShield;
    }

    public static Shield getGuardianShield() //!< Returns a guardian shield, centred correctly, with the correct sprite and collisionables.
    {
        Shield returnShield = new Shield(false, -1, false);

        returnShield.m_sprite = new Sprite("game_foreground_spritesheet", new Rect(112, 144, 240, 176), 16, new Vector2f(2.0f, 0.5f), new Vector2f(1.0f, 0.75f));
        returnShield.addCollisionable(new CollisionRectangle(1.0f, 0.125f), new Vector2f(0.0f, -0.5f), 0.0f);

        return returnShield;
    }

    @Override
    public void update() {
        if (m_lifespan > 0) { m_lifespan--; }
        if (m_lifespan == 0) { delete(); }
        m_sprite.incrementCurrentFrame(true);
    }

    @Override
    public void draw(Canvas c, Paint p) { m_sprite.draw(c, p); }

    public int getLifespan() { return m_lifespan; }
    public void addLifespan(int lifespan) {
        if (m_lifespan > -1) {
            m_lifespan += lifespan;
            if (m_lifespan <= 0) { m_lifespan = 0; delete(); }
        }
    }
    public void setLifespan(int lifespan) { m_lifespan = lifespan; }

    @Override
    public void delete(){
        super.delete();
        Vector<GameObject> weldeds = Weld.getWelded(this);
        for (GameObject obj: weldeds) {
            if (obj instanceof PlayerShip) { ((PlayerShip) obj).m_shield = null; }
        }
    }
}
