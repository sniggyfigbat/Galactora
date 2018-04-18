package net.stefancbauer.galactora.Model;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import net.stefancbauer.galactora.Controller.BitmapManager;
import net.stefancbauer.galactora.LocalMaths.Vector2f;
import net.stefancbauer.galactora.View.GameSurfaceView;

import java.util.Vector;

/**
 * Class that defines a displayable sprite.
 */

public class Sprite {
    protected final String m_textureName;
    protected final Bitmap m_baseTexture; //!< The Texture to display from.
    protected final Rect m_baseTextureRect; //!< A Rect that defines the portion of the Texture to draw as the first frame, and the size of all following frames. In pixels, assumes origin in top left.
    protected final Vector<Bitmap> m_textures = new Vector<Bitmap>(); //!< The textures currently used by this sprite (textures used by each frame, for animated sprites).
    public final int m_frameCount;
    protected int m_currentFrame = 0;
    protected Vector2f m_size = new Vector2f(); //!< The dimensions of the sprite, in game units.
    protected Vector2f m_origin = new Vector2f(); //!< The coordinates of the local origin, relative to the bottom-left corner, in game units.
    protected Vector2f m_position = new Vector2f(); //!< Defines the m_position of the local origin in world-space.
    protected float m_rotation = 0.0f; //!< Rotation of sprite, counterclockwise, in degrees.
    protected int m_alpha = 255; //!< The degree of extra transparency to apply when drawing.
    protected boolean m_visible = true; //!< Whether or not to draw this sprite.

    public Sprite(Bitmap bitmap) //!< Basic constructor, maps the given bitmap onto a single-frame proportional sprite.
    {
        m_textureName = "Custom";
        m_baseTexture = bitmap;
        m_baseTextureRect = new Rect(0, 0, m_baseTexture.getWidth(), m_baseTexture.getHeight());
        m_frameCount = 1;

        float width = GameSurfaceView.DPtoGU * m_baseTextureRect.right;
        float height = GameSurfaceView.DPtoGU * m_baseTextureRect.bottom;

        m_size.set(width, height);
        m_origin.set(0.5f * width, 0.5f * height);
        m_position.set(0.0f);
        m_rotation = 0.0f;

        generateTextures();
    }

    public Sprite(String textureName) //!< Basic constructor, maps the entire bitmap onto a single-frame 1x1GU sprite.
    {
        m_textureName = new String(textureName);
        m_baseTexture = BitmapManager.getInstance().getBitmap(textureName);
        m_baseTextureRect = new Rect(0, 0, m_baseTexture.getWidth(), m_baseTexture.getHeight());
        m_frameCount = 1;
        m_size.set(1.0f);
        m_origin.set(0.5f);
        m_position.set(0.0f);
        m_rotation = 0.0f;

        generateTextures();
    }

    public Sprite(String textureName, Vector2f size, Vector2f origin) //!< Constructor, maps the entire bitmap onto single-frame sprite of the specified size.
    { this(textureName, size, origin, new Vector2f(), 0.0f); }

    public Sprite(String textureName, Vector2f size, Vector2f origin, Vector2f position)  //!< Constructor, maps the entire bitmap onto single-frame sprite of the specified size and m_position.
    { this(textureName, size, origin, position, 0.0f); }

    public Sprite(String textureName, Vector2f size, Vector2f origin, Vector2f position, float rotation)  //!< Constructor, maps the entire bitmap onto single-frame sprite of the specified size, m_position, and rotation.
    {
        m_textureName = new String(textureName);
        m_baseTexture = BitmapManager.getInstance().getBitmap(textureName);
        m_baseTextureRect = new Rect(0, 0, m_baseTexture.getWidth(), m_baseTexture.getHeight());
        m_frameCount = 1;
        m_size.set(size);
        m_origin.set(origin);
        m_position.set(position);
        m_rotation = rotation;

        generateTextures();
    }

    public Sprite(String textureName, Rect baseTextureRect, int frameCount, Vector2f size, Vector2f origin)  //!< Constructor, maps the specified section (and specified number of frames below it) of a bitmap onto multi-frame sprite of the specified size.
    { this(textureName, baseTextureRect, frameCount, size, origin, new Vector2f(0.0f, 0.0f), 0.0f); }

    public Sprite(String textureName, Rect baseTextureRect, int frameCount, Vector2f size, Vector2f origin, Vector2f position)  //!< Constructor, maps the specified section (and specified number of frames below it) of a bitmap onto multi-frame sprite of the specified size and position.
    { this(textureName, baseTextureRect, frameCount, size, origin, position, 0.0f); }

    public Sprite(String textureName, Rect baseTextureRect, Vector2f size, Vector2f origin, Vector2f position, float rotation)  //!< Constructor, maps the specified section of a bitmap onto single-frame sprite of the specified size, position, and rotation.
    { this(textureName, baseTextureRect, 1, size, origin, position, rotation); }

    public Sprite(String textureName, Rect baseTextureRect, int frameCount, Vector2f size, Vector2f origin, float rotation)  //!< Constructor, maps the specified section of a bitmap (and specified number of frames below it) of a bitmap onto multi-frame sprite of the specified size and rotation.
    { this(textureName, baseTextureRect, frameCount, size, origin, new Vector2f(), rotation); }

    public Sprite(String textureName, Rect baseTextureRect, int frameCount, Vector2f size, Vector2f origin, Vector2f position, float rotation)  //!< Full constructor, maps the specified section (and specified number of frames below it) of a bitmap onto a multi-frame sprite of the specified size, m_position, and rotation.
    {
        m_textureName = new String(textureName);
        m_baseTexture = BitmapManager.getInstance().getBitmap(textureName);
        m_baseTextureRect = new Rect(baseTextureRect);
        m_frameCount = frameCount;
        m_size.set(size);
        m_origin.set(origin);
        m_position.set(position);
        m_rotation = rotation;

        generateTextures();
    }

    protected void generateTextures() //!< Generates the correct bitmap for display, which is stored uniquely in memory. This is inefficient and basically evil.
    {
        // Argh, everything to do with image handling in Java is icky.
        // This is horrible and I hate it.
        // I am scared and sad and I want to go home to C++.

        // Each frame is the same size as the next, but offset on the spritesheet by its height. Putting in incorrect figures can therefore really fuck everything up.

        // Building the transform matrix. Probably.
        int width = m_baseTextureRect.width();
        int height = m_baseTextureRect.height();
        float xScale = (m_size.x * GameSurfaceView.GUtoDP) / (float)width;
        float yScale = (m_size.y * GameSurfaceView.GUtoDP) / (float)height;
        Matrix matrix = new Matrix(); // in pixel units, I think. Although with Android WHO THE F*** KNOWS?!!!
        matrix.setScale(xScale, yScale);

        for (int i = 0; i < m_frameCount; i++) {
            int offset = i * height;
            if (offset + height <= m_baseTexture.getHeight()) {
                m_textures.addElement(Bitmap.createBitmap(m_baseTexture, m_baseTextureRect.left, m_baseTextureRect.top + offset, width, height, matrix, false));
            } else {
                Log.d("ERROR", "Size overflow when attempting to add sprite " + i + " to " + m_textureName + ".");
            }
        }

    }

    public int getCurrentFrame(){ return m_currentFrame; }
    public void setCurrentframe(int currentFrame){ this.m_currentFrame = currentFrame; }

    public void incrementCurrentFrame() { incrementCurrentFrame(true, false, 0, 0); } //!< Chains with assumption of looping through all frames.
    public void incrementCurrentFrame(boolean loop) { incrementCurrentFrame(loop, false, 0, 0); } //!< Chains with assumption of any loop including all frames.
    public void incrementCurrentFrame(int rangeMin, int rangeMax) { incrementCurrentFrame(true, true, rangeMin, rangeMax); } //!< Chains with assumption of looping through frames in range (inclusive).
    public void incrementCurrentFrame(boolean loop, int rangeMin, int rangeMax) { incrementCurrentFrame(loop, true, rangeMin, rangeMax); } //!< Chains with assumption of any loop being restricted to frames in range (inclusive).
    public void incrementCurrentFrame(boolean loop, boolean restrict, int rangeMin, int rangeMax) //!< Increments the current frame for animation, with certain limits. If loop is true the animation will loop, and if restrict is true it will move from the end of range to beginning of range (inclusive).
    {
        int max;
        int dest;

        // Input checking on range, if necessary
        if (restrict) {
            max = rangeMax;
            dest = rangeMin;

            if (dest < 0) { dest = 0; }
            if (dest >= m_frameCount) { dest = m_frameCount - 1; }
            if (max < dest) { max = dest; }
            if (max >= m_frameCount) { max = m_frameCount - 1; }
        } else {
            max = m_frameCount - 1;
            dest = 0;
        }

        if (m_currentFrame < max) { m_currentFrame++; }
        else if (loop) { m_currentFrame = dest; }
    }

    public void draw(Canvas c, Paint p) {
        if (m_visible) {
            Bitmap texture = m_textures.get(m_currentFrame);
            p.setAlpha(m_alpha);

            float width = texture.getWidth();
            float height = texture.getHeight();

            Matrix matrix = new Matrix();
            matrix.setTranslate((m_origin.x / m_size.x) * -width , ((m_size.y - m_origin.y) / m_size.y) * -height); // Is that minus correct? WHO KNOWS! WHAT THE F*** IS A CONSISTENT FRAME OF F***ING REFERENCE ANYWAY?!!!
            matrix.postRotate(-m_rotation); // m_rotation is reversed to account for left-to-right handedness switch.
            Vector2f screenPosition = GameSurfaceView.GUtoDP(m_position).toFloat();
            matrix.postTranslate(screenPosition.x, screenPosition.y);

            c.drawBitmap(texture, matrix, p);
        }
    }

    public Vector2f getSize() {
        return m_size;
    }
    public void setSize(Vector2f m_size) {
        this.m_size = m_size;
        generateTextures();
    }

    public Vector2f getOrigin() {
        return m_origin;
    }
    public void setOrigin(Vector2f m_origin) {
        this.m_origin.set(m_origin);
    }

    public Vector2f getPosition() {
        return m_position;
    }
    public void setPosition(Vector2f m_position) { this.m_position.set(m_position); }

    public float getRotation() {
        return m_rotation;
    }
    public void setRotation(float m_rotation) {
        this.m_rotation = m_rotation % 360;
    }

    public int getAlpha() { return m_alpha; }
    public void setAlpha(int alpha) { m_alpha = Math.min(Math.max(alpha, 0), 255); }

    public boolean isVisible() { return m_visible; }
    public void setVisible(boolean visible) { m_visible = visible; }
}
