package net.stefancbauer.galactora.Model.Engine;

import net.stefancbauer.galactora.LocalMaths.Vector2f;

/**
 * A simple circle collision detector.
 */

public class CollisionCircle extends Collisionable {
    public final float m_radius; //!< Determines the radius of collision on the circle. Cannot be changed once set.

    public CollisionCircle(float radius) //!< Full Constructor.
    {
        super(CollisionableType.CIRCLE, Math.abs(radius));
        m_radius = Math.abs(radius);
    }

    public boolean checkCollision(Collisionable other, Vector2f r_collisionNormal) //!< Double dispatch function.
    {
        boolean returnBool = other.checkCollision(this, r_collisionNormal);
        r_collisionNormal.multiply(-1.0f);
        return returnBool;
    }

    public boolean checkCollision(CollisionCircle other, Vector2f r_collisionNormal) //!< Checks collision against another CollisionCircle.
    {
        boolean returnBool = false;

        Vector2f relativeVec = Vector2f.subtract(m_position, other.m_position);
        if (relativeVec.getMagnitudeSquared() <= ((m_radius + other.m_radius) * (m_radius + other.m_radius))) {
            returnBool = true;
            relativeVec.setMagnitude(1.0f);
            r_collisionNormal.set(relativeVec);
        }

        return returnBool;
    }

    public boolean checkCollision(CollisionRectangle other, Vector2f r_collisionNormal) //!< Checks collision against a CollisionRectangle.
    {
        boolean returnBool = other.checkCollision(this, r_collisionNormal);
        r_collisionNormal.multiply(-1.0f);
        return returnBool;
    }

    /*public boolean checkCollision(CollisionComplexConvex other, Vector2f r_collisionNormal) //!< Checks collision against a CollisionRectangle.
    {
        Log.d("Error", "Attempted to check collision between CIRCLE-type and COMPLEXCONVEX-type. Functionality not implemented.");
        return false;
    }*/
}
