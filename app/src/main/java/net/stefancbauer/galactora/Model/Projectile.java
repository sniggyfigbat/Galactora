package net.stefancbauer.galactora.Model;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import net.stefancbauer.galactora.LocalMaths.Vector2f;
import net.stefancbauer.galactora.Model.Engine.CollisionCircle;
import net.stefancbauer.galactora.Model.Engine.CollisionRectangle;

import java.util.Vector;

/**
 * Created by P13186907 on 09/03/2018.
 */

public class Projectile extends GameObject {
    private float m_acceleration; //!< How much the projectile speeds up / slows down by in GU per tick squared.
    private int m_lifespan; //!< Lifespan in ticks. If -1, dies once it gets further than 40 units away from the origin.
    private int m_damage; //!< How much damage this projectile deals.
    private float m_explosionRadius; //!< How large a radius explosion to create for detonation. If zero or less, do not detonate.
    private String m_explosionEffect; //!< Name of explosion effect to call.

    public boolean m_ownedByPlayer; //!< Whether or not this projectile is owned by player or by enemies, disabling direct friendly fire accordingly (explosions deal damage to everyone).
    public boolean m_reflected = false; //!< Whether this projectile has already been reflected by a shield this tick (and thus should be ignored).
    public boolean m_detonated = false; //!< Whether this projectile has detonated.

    /*private Bitmap m_explosionTextureBase = null; //!< Base Texture source for detonation explosion particles.
    private Vector<Rect> m_explosionTextureRect = new Vector<Rect>(); //!< Texture rects for detonation explosion particles.
    private Vector2f m_explosionSize = new Vector2f(); //!< Base size of detonation explosion particles.
    private Vector2f m_explosionSizeVar = new Vector2f(); //!< Variance in size of detonation explosion particles.*/


    private Projectile(Vector2f velocity, float acceleration, int lifespan, int damage, float explosionRadius, String explosionEffect, boolean ownedByPlayer) //!< Private Constructor, use Factory Functions.
    {
        super(velocity, 0.0f);
        m_acceleration = acceleration;
        m_lifespan = lifespan;
        m_damage = damage;
        m_explosionRadius = explosionRadius;
        m_explosionEffect = explosionEffect;
        m_ownedByPlayer = ownedByPlayer;
    }

    public static Projectile getRedBolt(Vector2f position, float rotation) //!< Factory function to create the player's red bolt projectiles.
    {
        // MAGIC:
        float speed = 0.2f;
        float acceleration = 0.0f;
        int lifespan = -1;
        int damage = 1;
        float explosionRadius = 0.0f;
        String explosionEffect = "redbolt";
        boolean ownedByPlayer = true;

        float altRot = rotation + 90.0f; // Rotation for trig starts with (1, 0) not (0, 1), must adjust for this.
        Vector2f velocity = new Vector2f((float)Math.cos(Math.toRadians(altRot)), (float)Math.sin(Math.toRadians(altRot))).multiply(speed);
        Projectile returnProjectile = new Projectile(velocity, acceleration, lifespan, damage, explosionRadius, explosionEffect, ownedByPlayer);

        returnProjectile.m_sprite = new Sprite("game_foreground_spritesheet", new Rect(256, 208, 288, 272), 1, new Vector2f(0.5f, 1.0f), new Vector2f(0.25f, 0.5f), rotation);
        //returnProjectile.addCollisionable(new CollisionCircle(0.25f), new Vector2f(0.0f, 0.25f), 45.0f);
        returnProjectile.addCollisionable(new CollisionRectangle(0.17678f, 0.17678f), new Vector2f(0.0f, 0.25f), 45.0f);
        returnProjectile.updateState(position, rotation, new Vector<GameObject>());

        return returnProjectile;
    }

    public static Projectile getGreenBolt(Vector2f position, float rotation) //!< Factory function to create the enemies' green bolt projectiles.
    {
        // MAGIC:
        float speed = 0.15f;
        float acceleration = 0.0f;
        int lifespan = -1;
        int damage = 1;
        float explosionRadius = 0.0f;
        String explosionEffect = "greenbolt";
        boolean ownedByPlayer = false;

        float altRot = rotation + 90.0f; // Rotation for trig starts with (1, 0) not (0, 1), must adjust for this.
        Vector2f velocity = new Vector2f((float)Math.cos(Math.toRadians(altRot)), (float)Math.sin(Math.toRadians(altRot))).multiply(speed);
        Projectile returnProjectile = new Projectile(velocity, acceleration, lifespan, damage, explosionRadius, explosionEffect, ownedByPlayer);

        returnProjectile.m_sprite = new Sprite("game_foreground_spritesheet", new Rect(288, 208, 320, 272), 1, new Vector2f(0.5f, 1.0f), new Vector2f(0.25f, 0.5f), rotation);
        returnProjectile.addCollisionable(new CollisionCircle(0.25f), new Vector2f(0.0f, 0.25f), 0.0f);
        returnProjectile.updateState(position, rotation, new Vector<GameObject>());

        return returnProjectile;
    }

    public static Projectile getRedBomb(Vector2f position, float rotation, float buttonHeldLength) //!< Factory function to create the player's red bomb projectiles.
    {
        // MAGIC:
        float speed = (((float)(Math.min(Math.max(buttonHeldLength, 30), 60)))/60.0f) * 0.5f;
        float acceleration = -0.01f;
        int lifespan = (int)((((float)(Math.min(Math.max(buttonHeldLength, 30), 60)))/60.0f) * 50.0f);
        int damage = 2;
        float explosionRadius = 3.0f;
        String explosionEffect = "redbomb";
        boolean ownedByPlayer = true;

        float altRot = rotation + 90.0f; // Rotation for trig starts with (1, 0) not (0, 1), must adjust for this.
        Vector2f velocity = new Vector2f((float)Math.cos(Math.toRadians(altRot)), (float)Math.sin(Math.toRadians(altRot))).multiply(speed);
        Projectile returnProjectile = new Projectile(velocity, acceleration, lifespan, damage, explosionRadius, explosionEffect, ownedByPlayer);

        returnProjectile.m_sprite = new Sprite("game_foreground_spritesheet", new Rect(320, 208, 336, 248), 1, new Vector2f(0.25f, 0.625f), new Vector2f(0.125f, 0.5f), rotation);
        returnProjectile.addCollisionable(new CollisionCircle(0.125f), new Vector2f(0.0f), 0.0f);
        returnProjectile.addCollisionable(new CollisionRectangle(0.125f, 0.25f), new Vector2f(0.0f, -0.25f), 0.0f);
        returnProjectile.updateState(position, rotation, new Vector<GameObject>());

        return returnProjectile;
    }

    public static Projectile getYellowBomb(Vector2f position, float rotation, float targetDistance) //!< Factory function to create the queen's yellow bomb projectiles.
    {
        // MAGIC:
        float speed = 0.1f;
        float acceleration = 0.0f;
        int lifespan = (int)Math.max((targetDistance/speed), (3.5f/speed));
        int damage = 2;
        float explosionRadius = 2.0f;
        String explosionEffect = "yellowbomb";
        boolean ownedByPlayer = false;

        float altRot = rotation + 90.0f; // Rotation for trig starts with (1, 0) not (0, 1), must adjust for this.
        Vector2f velocity = new Vector2f((float)Math.cos(Math.toRadians(altRot)), (float)Math.sin(Math.toRadians(altRot))).multiply(speed);
        Projectile returnProjectile = new Projectile(velocity, acceleration, lifespan, damage, explosionRadius, explosionEffect, ownedByPlayer);

        returnProjectile.m_sprite = new Sprite("game_foreground_spritesheet", new Rect(336, 208, 368, 288), 1, new Vector2f(0.5f, 1.25f), new Vector2f(0.25f, 0.875f), rotation);
        returnProjectile.addCollisionable(new CollisionCircle(0.25f), new Vector2f(0.0f), 0.0f);
        returnProjectile.updateState(position, rotation, new Vector<GameObject>());

        return returnProjectile;
    }

    @Override
    public void update() {
        if (m_reflected) { updateRotation(); }
        m_reflected = false; // Reset m_reflected for next turn.
        float maxSpeed = 0.5f; // MAGIC: Max speed for any projectile.
        Vector2f newPosition = Vector2f.add(getPosition(), m_velocity);
        setPosition(newPosition);
        if (m_acceleration != 0.0f) {
            float speed = m_velocity.getMagnitude();
            if (speed + m_acceleration >= maxSpeed) { speed = maxSpeed; }
            else if (speed + m_acceleration <= 0.0f) { speed = 0.0f; }
            else {speed += m_acceleration; }
            m_velocity.setMagnitude(speed);
        }

        if (m_lifespan > 0) { m_lifespan--; }
        m_sprite.incrementCurrentFrame(true);

        // TODO: create trail particles.
    }

    public int getLifespan() { return m_lifespan; }
    public void addLifespan(int lifespan) {
        if (m_lifespan > -1) {
            m_lifespan += lifespan;
            if (m_lifespan <= 0) { m_lifespan = 0; delete(); }
        }
    }
    public void setLifespan(int lifespan) { m_lifespan = lifespan; }

    public void updateRotation() //!< Sets the rotation to be pointed towards the direction of m_velocity.
    {
        setRotation((float)Math.toDegrees(Math.atan2(m_velocity.y, m_velocity.x)) - 90.0f);
    }

    public int getDamage() { return m_damage; }
    public float getExplosionRadius() { return m_explosionRadius; }

    @Override
    public void draw(Canvas c, Paint p) { if (m_sprite != null) { m_sprite.draw(c, p); } }

    public boolean isReadyToDetonate() {
        return ((m_lifespan == 0 || getPosition().getMagnitudeSquared() > 1600.0f) && !m_detonated);
    }

    public int explode(Vector<Explosion> destVec)
    {
        if (m_explosionRadius > 0.0f) {
            destVec.addElement(new Explosion(getPosition(), m_explosionRadius, m_damage));
        }
        if (bmLink != null) { bmLink.addEffect(m_explosionEffect, getPosition()); }
        m_detonated = true;
        delete();
        return m_damage;
    }

    /*@Override
    public void delete(){
        super.delete();

    }*/
}
