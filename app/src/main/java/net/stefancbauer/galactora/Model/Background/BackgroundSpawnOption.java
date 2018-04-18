package net.stefancbauer.galactora.Model.Background;

import android.graphics.Rect;

import net.stefancbauer.galactora.LocalMaths.Vector2f;

import java.util.Random;

/**
 * Contains the data needed to handle spawning a certain type of BackgroundObject. Essentially a form of factory.
 */

public class BackgroundSpawnOption {
    private String m_baseTextureName; //!< Name of base texture.
    private Rect m_baseTextureRect; //!< Rectangle of base texture to create bitmap from.
    private Vector2f m_size; //!< Base size, in GU, of spawned sprite.
    private Vector2f m_origin; //!< Local origin, in GU, of spawned sprite. Scales with m_size.
    private float m_sizeScalarVar; //!< Amount by which m_size mar vary. Eg, if m_sizeScaleVar is 0.3, m_size may be multiplied my anything between 0.7 and 1.3.
    private float m_rotation; //!< Base rotation of spawned sprite, in degrees.
    private float m_rotationVar; //!< Amount by which m_rotation mar vary, in degrees. Eg, if m_rotation is 45 and m_sizeScaleVar is 30, m_rotation may be anything between 15 and 75.
    private float m_rotationVel; //!< Base rotational speed of spawned sprite, in degrees per tick.
    private float m_rotationVelVar; //!< Amount by which m_rotationVel mar vary, in degrees per tick. Eg, if m_rotationVel is 45 and m_sizeScaleVelVar is 30, m_rotationVel may be anything between 15 and 75.

    public BackgroundSpawnOption(String baseTextureName, Rect baseTextureRect, Vector2f size, float sizeScalarVar) //!< Partial Constructor, chains full Constructor with some default values.
    {
        this(baseTextureName, baseTextureRect, size, new Vector2f(size).multiply(0.5f), sizeScalarVar, 0.0f, 0.0f, 0.0f, 0.0f);
    }

    public BackgroundSpawnOption(String baseTextureName, Rect baseTextureRect, Vector2f size, Vector2f origin, float sizeScalarVar) //!< Partial Constructor, chains full Constructor with some default-0 values.
    {
        this(baseTextureName, baseTextureRect, size, origin, sizeScalarVar, 0.0f, 0.0f, 0.0f, 0.0f);
    }

    public BackgroundSpawnOption(String baseTextureName, Rect baseTextureRect, Vector2f size, Vector2f origin, float sizeScalarVar, float rotation, float rotationVar) //!< Partial Constructor, chains full Constructor with some default-0 values.
    {
        this(baseTextureName, baseTextureRect, size, origin, sizeScalarVar, rotation, rotationVar, 0.0f, 0.0f);
    }

    public BackgroundSpawnOption(String baseTextureName, Rect baseTextureRect, Vector2f size, Vector2f origin, float sizeScalarVar, float rotation, float rotationVar, float rotationVel, float rotationVelVar) //!< Full Constructor
    {
        m_baseTextureName = new String(baseTextureName);
        m_baseTextureRect = new Rect(baseTextureRect);
        m_size = new Vector2f(size);
        m_origin = new Vector2f(origin);
        m_sizeScalarVar = Math.abs(sizeScalarVar);
        m_rotation = rotation;
        m_rotationVar = Math.abs(rotationVar);
        m_rotationVel = rotationVel;
        m_rotationVelVar = Math.abs(rotationVelVar);
    }

    public BackgroundObject getInstance(Random rand, Vector2f position, float yVelocity) // Factory function, spawning an instance of BackgroundObject with the appropriate sprite and attributes.
    {
        float spawnSizeScale = 1.0f + (rand.nextFloat() * 2.0f * m_sizeScalarVar) - m_sizeScalarVar;
        Vector2f spawnSize = new Vector2f(m_size).multiply(spawnSizeScale);
        Vector2f spawnOrigin = new Vector2f(m_origin).multiply(spawnSizeScale);

        float spawnRot = m_rotation + (rand.nextFloat() * 2.0f * m_rotationVar) - m_rotationVar;
        float spawnRotVel = m_rotationVel + (rand.nextFloat() * 2.0f * m_rotationVelVar) - m_rotationVelVar;

        return new BackgroundObject(m_baseTextureName, m_baseTextureRect, spawnSize, spawnOrigin, position, spawnRot, yVelocity, spawnRotVel);
    }
}
