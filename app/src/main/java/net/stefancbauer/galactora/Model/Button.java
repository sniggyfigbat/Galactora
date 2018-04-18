package net.stefancbauer.galactora.Model;

import android.graphics.Rect;

import net.stefancbauer.galactora.LocalMaths.Vector2f;

/**
 * Used to build semi-generic button items. Mostly just a sprite with a check-bounds function and the ability to toggle a triggered boolean. Can contain a pointer to an object.
 */

public class Button extends Sprite {
    private Vector2f m_bottomLeft;
    private Vector2f m_topRight;

    private boolean m_pressed = false; //!< Whether button is currently pressed.
    private int m_pressedLength = 0; //!< How many ticks button has been pressed for.
    private boolean m_pressedSinceLastChecked = false; //!< Whether button has been m_pressed since button was last polled (and therefore should be ignored).
    private boolean m_triggered = false; //!< Whether button effect should trigger.

    private boolean m_active = true; //!< Whether or not button can be interacted with.

    public ButtonBehaviour m_behaviour; //!< The behaviour preset of this button.

    private Object m_pointer;

    public Button(String textureName, Rect baseTextureRect, Vector2f bottomLeft, Vector2f topRight) //!< Constructor, chains with assumption of only two frames, no pointer, and ONPRESS behaviour.
    { this(textureName, baseTextureRect, 2, bottomLeft, topRight, null, ButtonBehaviour.ONPRESS); }

    public Button(String textureName, Rect baseTextureRect, Vector2f bottomLeft, Vector2f topRight, ButtonBehaviour behaviour) //!< Constructor, chains with assumption of only two frames and no pointer.
    { this(textureName, baseTextureRect, 2, bottomLeft, topRight, null, behaviour); }

    public Button(String textureName, Rect baseTextureRect, int frameCount, Vector2f bottomLeft, Vector2f topRight, Object object, ButtonBehaviour behaviour) //!< Full Constructor. baseTextureRect is in pixels, bottomLeft and topRight are in GU.
    {
        super(textureName, baseTextureRect, frameCount, Vector2f.subtract(topRight, bottomLeft), new Vector2f((topRight.x - bottomLeft.x)/2.0f, (topRight.y - bottomLeft.y)/2.0f), Vector2f.getMidpoint(bottomLeft, topRight));
        m_currentFrame = m_frameCount - 1;
        m_bottomLeft = bottomLeft;
        m_topRight = topRight;
        m_pointer = object;

        m_behaviour = behaviour;
    }

    public boolean checkBounds(Vector2f checkPoint) //!< Checks whether or not point (in GU) is over the button.
    {
        boolean returnBool = true;
        if (!(checkPoint.x >= m_bottomLeft.x && checkPoint.x <= m_topRight.x)){ returnBool = false; }
        if (!(checkPoint.y >= m_bottomLeft.y && checkPoint.y <= m_topRight.y)){ returnBool = false; }

        return returnBool;
    }

    public boolean press( Vector2f pressPoint) //!< Tells the button that the user has pressed it (but not released it). Returns whether or not it's in bounds, saving repeated calling of checkBounds().
    {
        boolean inBounds = checkBounds(pressPoint);

        if (m_active && inBounds && !m_pressed && !m_pressedSinceLastChecked) {
            switch (m_behaviour) {
                case ONPRESS: m_triggered = true; break;
                case WHILEHELD: m_triggered = true; break;
                case TOGGLE: m_triggered = !m_triggered; break;
            }

            m_pressed = true;
            m_pressedSinceLastChecked = true;
            m_pressedLength = 0;
            if (m_behaviour != ButtonBehaviour.TOGGLE || (m_behaviour == ButtonBehaviour.TOGGLE && m_triggered)) { m_currentFrame = 0; }
        } else if (!inBounds && m_pressed) {
            m_pressed = false;
            if (m_behaviour == ButtonBehaviour.WHILEHELD) { m_triggered = false; }
            if (m_behaviour != ButtonBehaviour.TOGGLE) { m_currentFrame = m_frameCount - 1; }
        }

        return inBounds;
    }

    public void release( Vector2f pressPoint ) //!< Tells the button that the user has lifted their finger from the screen, and whether or not they were over the button as they did so.
    {
        if (m_active && checkBounds(pressPoint)) {
            if (m_pressed) {
                switch (m_behaviour) {
                    case ONRELEASE: m_triggered = true; break;
                    case WHILEHELD: m_triggered = false; break;
                }
            }
        }

        m_pressed = false;
    }

    public boolean isPressed(){ return m_pressed; }
    public boolean isTriggered(){ return m_triggered; }
    public int getPressedLength() { return m_pressedLength; }

    public boolean poll() //!< Returns whether or not the button is triggered. Doing this affects button logic, essentially telling it that the triggered effect will function.
    {
        boolean returnBool = false;
        switch (m_behaviour) {
            case ONPRESS:
            case ONRELEASE:
                returnBool = m_triggered;
                m_triggered = false;
                break;
            case TOGGLE:
            case WHILEHELD:
                returnBool = m_triggered;
                break;
        }
        m_pressedSinceLastChecked = false;
        return returnBool;
    }


    public Object getPointer(){ return m_pointer; } //!< Returns a reference to whatever object it's been told to point at.

    public void reset() //!< Resets the button.
    {
        m_pressed = false;
        m_pressedLength = 0;
        m_triggered = false;
        m_pressedSinceLastChecked = false;
    }

    public void update() //!< Purely for visual updates, affects no logic.
    {
        if (m_pressed) { m_pressedLength++; }

        if (m_behaviour == ButtonBehaviour.TOGGLE){
            if (!m_triggered) { super.incrementCurrentFrame(false); }
        } else {
            if (!m_pressed) { super.incrementCurrentFrame(false); }
        }
    }

    /*@Override
    public void incrementCurrentFrame() {}
    public void incrementCurrentFrame(boolean loop) {}
    public void incrementCurrentFrame(int rangeMin, int rangeMax) {}
    public void incrementCurrentFrame(boolean loop, int rangeMin, int rangeMax) {}
    public void incrementCurrentFrame(boolean loop, boolean restrict, int rangeMin, int rangeMax) {}*/

    @Override
    public void setPosition(Vector2f m_position) {}

    @Override
    public void setRotation(float m_rotation) {}

    public boolean isActive() { return m_active; }
    public void setActive(boolean active) {
        m_active = active;

        if (!m_active) {
            m_pressed = false;
            m_pressedSinceLastChecked = false;

            if (m_behaviour != ButtonBehaviour.TOGGLE) { m_triggered = false; }
        }
    }
}
