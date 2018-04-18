package net.stefancbauer.galactora.Model;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Pair;

import net.stefancbauer.galactora.LocalMaths.Vector2f;
import net.stefancbauer.galactora.Model.Background.BackgroundManager;
import net.stefancbauer.galactora.Model.Engine.Collisionable;
import net.stefancbauer.galactora.Model.Engine.Weld;

import java.util.Vector;

/**
 * An abstract GameObject class.
 */

public abstract class GameObject {
    private final Vector2f m_position = new Vector2f(); //!< The current position of the object.
    public final Vector2f m_velocity = new Vector2f(); //!< The velocity of the object in GU per tick. May or may not be applied in the update function.
    private float m_rotation = 0.0f; //!< The current rotation of the object.
    public float m_rotationVelocity = 0.0f; //!< The rotational velocity of the object in degrees per tick. May or may not be applied in the update function.
    private boolean m_toBeDestroyed = false; //!< Whether this object should be destroyed and cleaned up.

    public Game gameLink = null; //!< Link to the game object.
    public BackgroundManager bmLink = null; //!< A pointer to the BackgroundManager that should be used for effects. Also a gross violation of scope.

    public Sprite m_sprite = null; //!< The sprite tied to this GameObject.
    protected final Vector<Pair<Collisionable, Matrix>> m_collisionables = new Vector<Pair<Collisionable, Matrix>>(); //!< All collision-boxes for this object. Matrix is to define the relative transform of the Collisionable relative to the local origin. (Remember, rotate then translate!)
    private boolean m_isWelded = false; //!< Used to check whether or not this object is welded to any others.
    public boolean m_chainsDeletion = true; //!< Whether or not, when deleted, will delete all objects welded to it.

    // Constructors. Thank heavens Java doesn't implement anything as useful or friendly as default arguments.
    public GameObject() {}
    public GameObject(Vector2f velocity, float rotationVelocity)
    {
        m_velocity.set(velocity);
        m_rotationVelocity = rotationVelocity;
    }
    public GameObject(Game game) { gameLink = game; }
    public GameObject(Game game, Vector2f velocity, float rotationVelocity)
    {
        gameLink = game;
        m_velocity.set(velocity);
        m_rotationVelocity = rotationVelocity;
    }

    public abstract void update();
    public abstract void draw(Canvas c, Paint p);

    public void updateState(Vector2f inp_position, float inp_rotation, Vector<GameObject> alreadyTriggered) //!< This is the correct way to handle updates of state. Chains to attached Collisionables and welds. alreadyTriggered stops mutual weld targets from cycling.
    {
        alreadyTriggered.addElement(this);

        m_position.set(inp_position);
        m_rotation = inp_rotation;

        if (m_sprite != null) {
            m_sprite.setPosition(m_position);
            m_sprite.setRotation(m_rotation);
        }

        for (Pair<Collisionable, Matrix> collisionable: m_collisionables) {
            float[] state = new float[]{0.0f, 0.0f, 1.0f, 0.0f}; // Build an upwards-pointing normal vector to apply a matrix to.
            Matrix matrix = new Matrix(collisionable.second);
            matrix.postRotate(m_rotation);
            matrix.postTranslate(m_position.x, m_position.y);
            matrix.mapPoints(state);

            collisionable.first.m_position.set(state[0], state[1]);
            collisionable.first.m_rotation = (float)Math.toDegrees(Math.atan2((state[3] - state[1]), (state[2] - state[0])));
        }

        if (m_isWelded) {
            Weld.chainUpdate(this, alreadyTriggered, m_position, m_rotation);
        }
    }

    public void addCollisionable(Collisionable inp_obj, Vector2f relativePosition, float relativeRotation) //!< Handles the setting up of m_collisionables entries.
    {
        Matrix matrix = new Matrix();
        matrix.setRotate(relativeRotation);
        matrix.postTranslate(relativePosition.x, relativePosition.y);

        m_collisionables.addElement(new Pair<Collisionable, Matrix>(inp_obj, matrix));
    }

    public boolean checkCollision(GameObject other, Vector2f r_collisionNormal) //!< Cycles through all Collisionables in two GameObjects, checking all pairs for collision.
    {
        boolean returnBool = false;
        for (int i = 0; i < m_collisionables.size() && !returnBool; i++) {
            for (int j = 0; j < other.m_collisionables.size() && !returnBool; j++) {
                returnBool = m_collisionables.get(i).first.checkCollision(other.m_collisionables.get(j).first, r_collisionNormal);
            }
        }
        return returnBool;
    }

    public boolean checkCollision(Explosion other, Vector2f r_collisionNormal) //!< Cycles through all Collisionables in two GameObjects, checking all pairs for collision.
    {
        boolean returnBool = false;
        for (int i = 0; i < m_collisionables.size() && !returnBool; i++) {
                returnBool = m_collisionables.get(i).first.checkCollision(other.m_collisionable, r_collisionNormal);
        }
        return returnBool;
    }

    // Getters/Setters
    public boolean isToBeDestroyed() { return m_toBeDestroyed; }
    public void delete() {
        m_toBeDestroyed = true;
        if (m_chainsDeletion) { Weld.chainDelete(this); }
    }

    public Vector2f getPosition() { return new Vector2f(m_position); }
    public void setPosition(Vector2f inp_position) { updateState(inp_position, m_rotation, new Vector<GameObject>()); }

    public float getRotation() { return m_rotation; }
    public void setRotation(float rotation) { updateState(m_position, rotation, new Vector<GameObject>()); }

    public boolean isWelded() { return m_isWelded; }
    public void setIsWelded(boolean isWelded) { m_isWelded = isWelded; }

    public static float sanitiseAngle(float inp_angle) { return sanitiseAngle(inp_angle, false); }
    public static float sanitiseAngle(float inp_angle, boolean to360) //!< Cleans up a rotation, in degrees, to either -180 to 180 (default), or 0 to 360.
    {
        float output = inp_angle % 360.0f;
        if ((!to360 && output <= -180.0f) || (to360 && output < 0.0f)) { output += 360.0f; }
        if (!to360 && output > 180.0f) { output += 360.0f; }
        return output;
    }
}
