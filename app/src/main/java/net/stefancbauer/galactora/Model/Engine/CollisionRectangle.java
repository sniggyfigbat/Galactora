package net.stefancbauer.galactora.Model.Engine;

import android.graphics.Matrix;
import android.graphics.RectF;

import net.stefancbauer.galactora.LocalMaths.Vector2f;

/**
 * A simple rectangle collision detector.
 */

public class CollisionRectangle extends Collisionable {
    public final RectF m_rectangle; //!< Defines the rectangle collider, centred on a local origin. Cannot be changed once set.

    public CollisionRectangle(float xHalfExtent, float yHalfExtent) //!< Full Constructor.
    {
        super(CollisionableType.RECTANGLE, (float)Math.sqrt((xHalfExtent * xHalfExtent) + (yHalfExtent * yHalfExtent)));
        xHalfExtent = Math.abs(xHalfExtent);
        yHalfExtent = Math.abs(yHalfExtent);
        m_rectangle = new RectF(-xHalfExtent, yHalfExtent, xHalfExtent, -yHalfExtent);
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

        Vector2f relativeVec = Vector2f.subtract(other.m_position, m_position);
        if (relativeVec.getMagnitudeSquared() <= ((m_checkRadius + other.m_checkRadius) * (m_checkRadius + other.m_checkRadius))) {
            // Is within proximity, requires checking.

            // Make circle relative to rectangle.
            Matrix matrix = new Matrix();
            matrix.setRotate(-m_rotation);
            float[] circleCentre = new float[2];
            circleCentre[0] = relativeVec.x;
            circleCentre[1] = relativeVec.y;
            matrix.mapPoints(circleCentre);
            relativeVec.set(circleCentre[0], circleCentre[1]);

            // Check Collision
            Vector2f constrainedVec = new Vector2f(relativeVec);
            constrainedVec.x = Math.min(constrainedVec.x, m_rectangle.right);
            constrainedVec.x = Math.max(constrainedVec.x, m_rectangle.left);
            constrainedVec.y = Math.min(constrainedVec.y, m_rectangle.top);
            constrainedVec.y = Math.max(constrainedVec.y, m_rectangle.bottom);

            Vector2f distanceVec = Vector2f.subtract(relativeVec, constrainedVec);

            if (distanceVec.getMagnitudeSquared() <= (other.m_radius * other.m_radius)) {
                // Good god almighty, fuck a duck, we've a palpable hit!
                returnBool = true;
                relativeVec.setMagnitude(1.0f);
                r_collisionNormal.set(relativeVec);

                // Set up normal vector. This could be done without the if statements, but .setMagnitude is comparatively resource intensive.
                float[] normalVecSetup = new float[2];
                if (distanceVec.y == 0 && distanceVec.x > 0) { normalVecSetup[0] = -1.0f; normalVecSetup[1] = 0.0f; }
                else if (distanceVec.y == 0 && distanceVec.x < 0) { normalVecSetup[0] = 1.0f; normalVecSetup[1] = 0.0f; }
                else if (distanceVec.x == 0 && distanceVec.y > 0) { normalVecSetup[0] = 0.0f; normalVecSetup[1] = -1.0f; }
                else if (distanceVec.x == 0 && distanceVec.y < 0) { normalVecSetup[0] = 0.0f; normalVecSetup[1] = 1.0f; }
                else {
                    distanceVec.setMagnitude(1.0f);
                    normalVecSetup[0] = -distanceVec.x;
                    normalVecSetup[1] = -distanceVec.y;
                }

                // Apply rotation matrix, return collision normal.
                matrix.setRotate(m_rotation);
                matrix.mapPoints(normalVecSetup);
                r_collisionNormal.set(normalVecSetup[0], normalVecSetup[1]);
            }
        }

        return returnBool;
    }

    public boolean checkCollision(CollisionRectangle other, Vector2f r_collisionNormal) //!< Checks collision against a CollisionRectangle.
    {
        boolean returnBool = false;

        Vector2f relativeVec = Vector2f.subtract(other.m_position, m_position); // other from perspective of this.
        if (relativeVec.getMagnitudeSquared() <= ((m_checkRadius + other.m_checkRadius) * (m_checkRadius + other.m_checkRadius))) {
            // Set up the points for the two rectangles.
            float[] rect1 = new float[8];
            rect1[0] = m_rectangle.left;
            rect1[1] = m_rectangle.bottom;
            rect1[2] = m_rectangle.left;
            rect1[3] = m_rectangle.top;
            rect1[4] = m_rectangle.right;
            rect1[5] = m_rectangle.top;
            rect1[6] = m_rectangle.right;
            rect1[7] = m_rectangle.bottom;

            float[] rect2 = new float[8];
            rect2[0] = other.m_rectangle.left;
            rect2[1] = other.m_rectangle.bottom;
            rect2[2] = other.m_rectangle.left;
            rect2[3] = other.m_rectangle.top;
            rect2[4] = other.m_rectangle.right;
            rect2[5] = other.m_rectangle.top;
            rect2[6] = other.m_rectangle.right;
            rect2[7] = other.m_rectangle.bottom;

            Matrix matrix = new Matrix();
            matrix.setRotate(other.m_rotation);
            matrix.postTranslate(relativeVec.x, relativeVec.y);
            matrix.postRotate(-m_rotation);
            matrix.mapPoints(rect2);

            // Check if there's a gap on any axis
            boolean foundGap = false;

            if (!foundGap && checkAxisForGap(rect1, rect2, new Vector2f((rect1[2] - rect1[0]), (rect1[3] - rect1[1])))) { foundGap = true; }
            if (!foundGap && checkAxisForGap(rect1, rect2, new Vector2f((rect1[6] - rect1[0]), (rect1[7] - rect1[1])))) { foundGap = true; }
            if (!foundGap && checkAxisForGap(rect1, rect2, new Vector2f((rect1[2] - rect1[0]), (rect1[3] - rect1[1])))) { foundGap = true; }
            if (!foundGap && checkAxisForGap(rect1, rect2, new Vector2f((rect1[2] - rect1[0]), (rect1[3] - rect1[1])))) { foundGap = true; }

            if (!foundGap) {
                // Whooee, boys, we got ourselves a collision! Now we just need to roughly approximate a collision normal...

                returnBool = true;
                float[] overlapEscapes = new float[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; // All the vectors 'acting' on the boxes to get them to, uh, uncollide.

                // Check other's points.
                for (int i = 0; i < 8; i += 2) {
                    if ((rect2[i] > m_rectangle.left && rect2[i] < m_rectangle.right) && (rect2[i + 1] > m_rectangle.bottom && rect2[i + 1] < m_rectangle.top))
                    {
                        // Point lies inside this! Time to work out how much escape force it should apply, and in what direction.
                        // Points should always look for the closest escape route, and apply force in that direction.

                        float xNeg = m_rectangle.left - rect2[i];
                        float xPos = m_rectangle.right - rect2[i];
                        float xEscape;
                        if (Math.abs(xNeg) < Math.abs(xPos)) { xEscape = xNeg; } else { xEscape = xPos; }

                        float yNeg = m_rectangle.bottom - rect2[i + 1];
                        float yPos = m_rectangle.top - rect2[i + 1];
                        float yEscape;
                        if (Math.abs(yNeg) < Math.abs(yPos)) { yEscape = yNeg; } else { yEscape = yPos; }

                        if (Math.abs(xEscape) < Math.abs(yEscape)) { overlapEscapes[i] = -xEscape; } else { overlapEscapes[i + 1] = -yEscape; }
                    }
                }

                // Move stuff about so that other's in the centre at 0.
                relativeVec.multiply(-1); // this from perspective of other.
                matrix.setRotate(m_rotation);
                matrix.postTranslate(relativeVec.x, relativeVec.y);
                matrix.postRotate(-other.m_rotation);
                matrix.mapPoints(rect1);
                matrix.mapPoints(rect2);

                matrix.setRotate(m_rotation);
                matrix.postRotate(-other.m_rotation);
                matrix.mapPoints(overlapEscapes);

                // Check this's points.
                for (int i = 0; i < 8; i += 2) {
                    if ((rect2[i] > other.m_rectangle.left && rect2[i] < other.m_rectangle.right) && (rect2[i + 1] > other.m_rectangle.bottom && rect2[i + 1] < other.m_rectangle.top))
                    {
                        // Point lies inside other! Time to work out how much escape force it should apply, and in what direction.
                        // Points should always look for the closest escape route, and apply force in that direction.

                        float xNeg = other.m_rectangle.left - rect2[i];
                        float xPos = other.m_rectangle.right - rect2[i];
                        float xEscape;
                        if (Math.abs(xNeg) < Math.abs(xPos)) { xEscape = xNeg; } else { xEscape = xPos; }

                        float yNeg = other.m_rectangle.bottom - rect2[i + 1];
                        float yPos = other.m_rectangle.top - rect2[i + 1];
                        float yEscape;
                        if (Math.abs(yNeg) < Math.abs(yPos)) { yEscape = yNeg; } else { yEscape = yPos; }

                        if (Math.abs(xEscape) < Math.abs(yEscape)) { overlapEscapes[i + 8] = xEscape; } else { overlapEscapes[i + 9] = yEscape; }
                    }
                }

                // Return to a rotationally correct frame of reference
                matrix.setRotate(other.m_rotation);
                matrix.mapPoints(overlapEscapes);

                // Add all the escape vectors up
                Vector2f escapeVec = new Vector2f();
                for (int i = 0; i < 16; i += 2) {
                    escapeVec.x += overlapEscapes[i];
                    escapeVec.y += overlapEscapes[i + 1];
                }

                // Apply to r_collisionNormal
                escapeVec.setMagnitude(1.0f);
                r_collisionNormal.set(escapeVec);
            }
        }

        return returnBool;
    }

    private boolean checkAxisForGap(float[] inp_rect1, float[] inp_rect2, Vector2f axis) //!< Used when checking collisions with other Rectangle Types. Axis determines the normal to the axis the points will be compressed onto.
    {
        // Make sure I don't override the originals
        float[] rect1 = new float[8];
        System.arraycopy(inp_rect1, 0, rect1, 0, inp_rect1.length);
        float[] rect2 = new float[8];
        System.arraycopy(inp_rect2, 0, rect2, 0, inp_rect2.length);

        // Get rotation of axis and build matrix.
        float axisRot = (float)Math.toDegrees(Math.atan2(axis.y, axis.x)) - 90.0f;
        Matrix matrix = new Matrix();
        matrix.setRotate(-axisRot);

        // Apply matrix and find min/max of each rectangle
        matrix.mapPoints(rect1);
        matrix.mapPoints(rect2);

        float temp1;
        float temp2;

        temp1 = Math.max(rect1[0], rect1[2]);
        temp2 = Math.max(rect1[4], rect1[6]);
        float rect1Max = Math.max(temp1, temp2);
        temp1 = Math.min(rect1[0], rect1[2]);
        temp2 = Math.min(rect1[4], rect1[6]);
        float rect1Min = Math.min(temp1, temp2);

        temp1 = Math.max(rect2[0], rect2[2]);
        temp2 = Math.max(rect2[4], rect2[6]);
        float rect2Max = Math.max(temp1, temp2);
        temp1 = Math.min(rect2[0], rect2[2]);
        temp2 = Math.min(rect2[4], rect2[6]);
        float rect2Min = Math.min(temp1, temp2);

        // Check for a gap
        if (rect1Max >= rect2Min && rect2Max >= rect1Min) { return false; }
        else { return true; }
    }

    /*public boolean checkCollision(CollisionComplexConvex other, Vector2f r_collisionNormal) //!< Checks collision against a CollisionRectangle.
    {
        Log.d("Error", "Attempted to check collision between CIRCLE-type and COMPLEXCONVEX-type. Functionality not implemented.");
        return false;
    }*/
}
