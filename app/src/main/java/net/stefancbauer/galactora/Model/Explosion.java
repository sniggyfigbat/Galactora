package net.stefancbauer.galactora.Model;

import net.stefancbauer.galactora.LocalMaths.Vector2f;
import net.stefancbauer.galactora.Model.Engine.CollisionCircle;

/**
 * An undrawn explosion object, essentially just a container for a CircleCollision and a damage number. Should only ever exist for a tick or so.
 */

public class Explosion {
    public int m_damage; //!< How much damage is done to any GameObject Overlapping this explosion.
    public CollisionCircle m_collisionable; //!< the collision checker for the explosion.

    public Explosion(Vector2f position, float radius, int damage) //!< Full Constructor.
    {
        m_damage = damage;

        m_collisionable = new CollisionCircle(radius);
        m_collisionable.m_position.set(position);
    }
}
