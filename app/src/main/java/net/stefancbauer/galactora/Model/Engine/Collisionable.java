package net.stefancbauer.galactora.Model.Engine;

import net.stefancbauer.galactora.LocalMaths.Vector2f;

/**
 * A generic superclass for all collision-detection objects.
 */

public abstract class Collisionable {
    public final CollisionableType m_type; //!< The type of Collisionable this instance is.
    public Vector2f m_position = new Vector2f(); //!< Position of local origin in GU, eg centre of circle, midpoint of rectangle, etc.
    public float m_rotation = 0; //!< Rotation of Collisionable instance around local origin. Irrelevant for circles, obviously.

    public final float m_checkRadius; //!< Used to quickly discount distant pairs; The radius within which objects will perform complex checking algorithms. m_checkRadius should always inscribe a circle large enough to include all of this Collisionable.

    protected Collisionable(CollisionableType type, float checkRadius) { m_type = type; m_checkRadius = checkRadius; }

    public abstract boolean checkCollision(Collisionable other, Vector2f r_collisionNormal);
    public abstract boolean checkCollision(CollisionCircle other, Vector2f r_collisionNormal);
    public abstract boolean checkCollision(CollisionRectangle other, Vector2f r_collisionNormal);
}
