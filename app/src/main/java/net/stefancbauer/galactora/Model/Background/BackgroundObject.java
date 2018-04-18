package net.stefancbauer.galactora.Model.Background;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import net.stefancbauer.galactora.LocalMaths.LocalMaths;
import net.stefancbauer.galactora.LocalMaths.Vector2f;
import net.stefancbauer.galactora.Model.Armour;
import net.stefancbauer.galactora.Model.Enemy;
import net.stefancbauer.galactora.Model.EnemyType;
import net.stefancbauer.galactora.Model.Sprite;

import java.util.Random;
import java.util.Vector;

/**
 * BackgroundObjects are non-interacting objects that scroll from the bottom of the screen to the top, then despawn, at a set speed.
 */

public class BackgroundObject extends Sprite {
    public Vector2f m_velocity = new Vector2f(); //!< The m_velocity of the object, in game units per tick, on the y-axis.
    private float m_rotVelocity; //!< The rotational m_velocity of the object, in degrees per tick.

    public boolean m_toBeDestroyed; //!< Flag for whether object has run its course and should be destroyed.
    private float m_yCutoff; //!< The y-value above which the object should be deleted.

    public boolean m_fadeOut = false; //!< Whether or not sprite should fade into transparency (eg. for explosions). If true, object will be destroyed when fully transparent.
    public int m_fadeOutFactor = 1; //!< The amount to reduce alpha by each tick, if m_fadeOut is true.

    public BackgroundObject(String textureName, Rect baseTextureRect, Vector2f size, Vector2f position, float rotation, float yVelocity) //!< Partial Constructor, chains full Constructor with some default values.
    { this(textureName, baseTextureRect, size, new Vector2f(size).multiply(0.5f), position, rotation, yVelocity, 0.0f ); }

    public BackgroundObject(String textureName, Rect baseTextureRect, Vector2f size, Vector2f position, float rotation, float yVelocity, float rotVelocity) //!< Partial Constructor, chains full Constructor with some default values.
    { this(textureName, baseTextureRect, size, new Vector2f(size).multiply(0.5f), position, rotation, yVelocity, rotVelocity ); }

    public BackgroundObject(String textureName, Rect baseTextureRect, Vector2f size, Vector2f origin, Vector2f position, float rotation, float yVelocity, float rotVelocity) //!< Full Constructor
    {
        super(textureName, baseTextureRect, size, origin, position, rotation);
        m_velocity.y = yVelocity;
        m_rotVelocity = rotVelocity;

        m_yCutoff = -21.0f; // MAGIC

        incrementCurrentFrame();
    }

    public BackgroundObject(Bitmap bitmap, Vector2f position, float rotation, float yVelocity, float rotVelocity) //!< Creates a custom BackgroundObject from a bitmap.
    {
        super(bitmap);
        m_position = position;
        m_rotation = rotation;

        m_velocity.y = yVelocity;
        m_rotVelocity = rotVelocity;

        m_yCutoff = -21.0f; // MAGIC

        incrementCurrentFrame();
    }

    public void update(){
        if (m_fadeOut) {
            m_alpha -= m_fadeOutFactor;
            if (m_alpha <= 0) { m_toBeDestroyed = true; }
        }

        m_position.x += m_velocity.x;
        m_position.y += m_velocity.y;
        m_rotation = (m_rotation + m_rotVelocity) % 360;

        if (m_position.y < m_yCutoff) { m_toBeDestroyed = true; }
    }

    public static void getEffect(Random rand, Vector<BackgroundObject> destVec, String effectName, Vector2f position) //!< Factory function to create various in-game visual effects, eg. projectile explosions.
    {
        BackgroundObject temp;
        int explNum;
        int selector;

        switch (effectName)
        {
            case "redbolt":
                // Add blast.
                selector = rand.nextInt(4);
                if (selector < 1) {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(512, 384, 576,448), new Vector2f(1.0f), new Vector2f(0.5f), position, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 4.0f) - 2.0f));
                } else if (selector < 2) {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(512, 448, 576,512), new Vector2f(1.0f), new Vector2f(0.5f), position, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 4.0f) - 2.0f));
                } else if (selector < 3) {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(512, 512, 576,576), new Vector2f(1.0f), new Vector2f(0.5f), position, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 4.0f) - 2.0f));
                } else {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(512, 576, 576,640), new Vector2f(1.0f), new Vector2f(0.5f), position, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 4.0f) - 2.0f));
                }
                temp.m_fadeOut = true;
                temp.m_fadeOutFactor = 8;
                destVec.addElement(temp);
                break;
            case "greenbolt":
                // Add blast.
                temp = new BackgroundObject("game_foreground_spritesheet", new Rect(576, 384, 640,448), new Vector2f(1.0f), new Vector2f(0.5f), position, 0.0f, -0.05f, 0.0f);
                temp.m_fadeOut = true;
                temp.m_fadeOutFactor = 4;
                destVec.addElement(temp);
                break;
            case "redbomb":
                // Add main bomb blast.
                temp = new BackgroundObject("game_foreground_spritesheet", new Rect(512, 0, 896,384), new Vector2f(6.0f), new Vector2f(3.0f), position, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                temp.m_fadeOut = true;
                temp.m_fadeOutFactor = 4;
                destVec.addElement(temp);

                // Add between 5 and 8 smaller explosions.
                explNum = rand.nextInt(4) + 5;
                for (int i = 0; i < explNum; i++) {
                    float scalar = 0.75f + (rand.nextFloat() * 0.5f);
                    Vector2f subPosition = Vector2f.add(position, LocalMaths.getPointInCircle(rand, 3.0f));

                    selector = rand.nextInt(4);
                    if (selector < 1) {
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(512, 384, 576,448), new Vector2f(1.0f).multiply(scalar), new Vector2f(0.5f).multiply(scalar), subPosition, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else if (selector < 2) {
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(512, 448, 576,512), new Vector2f(1.0f).multiply(scalar), new Vector2f(0.5f).multiply(scalar), subPosition, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else if (selector < 3) {
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(512, 512, 576,576), new Vector2f(1.0f).multiply(scalar), new Vector2f(0.5f).multiply(scalar), subPosition, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else {
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(512, 576, 576,640), new Vector2f(1.0f).multiply(scalar), new Vector2f(0.5f).multiply(scalar), subPosition, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 4.0f) - 2.0f));
                    }
                    temp.m_fadeOut = true;
                    temp.m_fadeOutFactor = 8;
                    destVec.addElement(temp);
                }
                break;
            case "yellowbomb":
                // Add main bomb blast.
                temp = new BackgroundObject("game_foreground_spritesheet", new Rect(640, 384, 896,640), new Vector2f(4.0f), new Vector2f(2.0f), position, 0.0f, -0.05f, 0.0f);
                temp.m_fadeOut = true;
                temp.m_fadeOutFactor = 4;
                destVec.addElement(temp);

                // Add between 4 and 6 smaller explosions.
                explNum = rand.nextInt(3) + 4;
                for (int i = 0; i < explNum; i++) {
                    float scalar = 0.75f + (rand.nextFloat() * 0.5f);
                    Vector2f subPosition = Vector2f.add(position, LocalMaths.getPointInCircle(rand, 2.0f));

                    selector = rand.nextInt(4);
                    if (selector < 1) {
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(576, 448, 608,480), new Vector2f(0.5f).multiply(scalar), new Vector2f(0.25f).multiply(scalar), subPosition, 0.0f, -0.05f, 0.0f);
                    } else if (selector < 2) {
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(608, 448, 640,480), new Vector2f(0.5f).multiply(scalar), new Vector2f(0.25f).multiply(scalar), subPosition, 0.0f, -0.05f, 0.0f);
                    } else if (selector < 3) {
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(576, 480, 608,512), new Vector2f(0.5f).multiply(scalar), new Vector2f(0.25f).multiply(scalar), subPosition, 0.0f, -0.05f, 0.0f);
                    } else {
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(608, 480, 640,512), new Vector2f(0.5f).multiply(scalar), new Vector2f(0.25f).multiply(scalar), subPosition, 0.0f, -0.05f, 0.0f);
                    }
                    temp.m_fadeOut = true;
                    temp.m_fadeOutFactor = 8;
                    destVec.addElement(temp);
                }
                break;
        }
    }

    public static void getEnemyGibs(Random rand, Vector<BackgroundObject> destVec, Enemy enemy) //!< Factory function to create enemy-death visual effects, eg. Gibs.
    {
        float rotation = enemy.getRotation();
        Vector2f position = enemy.getPosition();
        EnemyType type = enemy.m_type;

        BackgroundObject temp;
        Matrix matrix = new Matrix();
        int selector;
        Vector<Integer> selectorOptions = new Vector<Integer>();
        float[] point = new float[2];
        int randomInt;
        float randomFloat;

        switch (type) {
            case DRONE:
                // Gibs, 6 possibilities, get 3 - 4 thereof.
                selectorOptions.clear(); // Probably unnecessary.
                for (int i = 0; i < 6; i++) { selectorOptions.addElement(i); }
                randomInt = 3 + rand.nextInt(2);

                for (int i = 0; i < randomInt; i ++) {
                    selector = rand.nextInt(selectorOptions.size());
                    if (selectorOptions.get(selector) < 1) {
                        // Left Eye
                        matrix.setRotate(rotation);
                        point[0] = -0.125f;
                        point[1] = 0.375f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(80, 128, 96,144), new Vector2f(0.25f), new Vector2f(0.125f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else if (selectorOptions.get(selector) < 2) {
                        // Right Eye
                        matrix.setRotate(rotation);
                        point[0] = 0.125f;
                        point[1] = 0.375f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(96, 128, 112,144), new Vector2f(0.25f), new Vector2f(0.125f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else if (selectorOptions.get(selector) < 3) {
                        // Left Antenna
                        matrix.setRotate(rotation);
                        point[0] = -0.25f;
                        point[1] = 0.25f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(80, 144, 96,176), new Vector2f(0.25f, 0.5f), new Vector2f(0.125f, 0.25f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else if (selectorOptions.get(selector) < 4) {
                        // Right Antenna
                        matrix.setRotate(rotation);
                        point[0] = 0.25f;
                        point[1] = 0.25f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(96, 144, 112,176), new Vector2f(0.25f, 0.5f), new Vector2f(0.125f, 0.25f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else if (selectorOptions.get(selector) < 5) {
                        // Left Wing
                        matrix.setRotate(rotation);
                        point[0] = -0.25f;
                        point[1] = 0.0f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(80, 176, 112,240), new Vector2f(0.5f, 1.0f), new Vector2f(0.25f, 0.5f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else {
                        // Right Wing
                        matrix.setRotate(rotation);
                        point[0] = 0.25f;
                        point[1] = 0.0f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(80, 240, 112,304), new Vector2f(0.5f, 1.0f), new Vector2f(0.25f, 0.5f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    }

                    temp.m_velocity.x = -0.01f + (rand.nextFloat() * 0.02f);
                    destVec.addElement(temp);
                    selectorOptions.remove(selector);
                }

                // Splatter
                selector = rand.nextInt(4);
                randomFloat = 1.0f + (rand.nextFloat() * 0.5f);
                if (selector < 1) {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(0, 128, 80,208), new Vector2f(1.25f).multiply(randomFloat), new Vector2f(0.625f).multiply(randomFloat), position, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                } else if (selector < 2) {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(0, 208, 80,288), new Vector2f(1.25f).multiply(randomFloat), new Vector2f(0.625f).multiply(randomFloat), position, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                } else if (selector < 3) {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(0, 400, 80,480), new Vector2f(1.25f).multiply(randomFloat), new Vector2f(0.625f).multiply(randomFloat), position, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                } else {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(0, 480, 80,560), new Vector2f(1.25f).multiply(randomFloat), new Vector2f(0.625f).multiply(randomFloat), position, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                }
                temp.m_fadeOut = true;
                temp.m_fadeOutFactor = 4;
                destVec.addElement(temp);
                break;
            case WARRIOR:
                // Gibs, 6 possibilities, get 3 - 4 thereof.
                selectorOptions.clear(); // Probably unnecessary.
                for (int i = 0; i < 6; i++) { selectorOptions.addElement(i); }
                randomInt = 3 + rand.nextInt(2);

                for (int i = 0; i < randomInt; i ++) {
                    selector = rand.nextInt(selectorOptions.size());
                    if (selectorOptions.get(selector) < 1) {
                        // Left Small Leg-Tentacle
                        matrix.setRotate(rotation);
                        point[0] = -0.375f;
                        point[1] = 0.25f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(48, 304, 80,336), new Vector2f(0.5f), new Vector2f(0.25f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else if (selectorOptions.get(selector) < 2) {
                        // Right Small Leg-Tentacle
                        matrix.setRotate(rotation);
                        point[0] = 0.375f;
                        point[1] = 0.25f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(80, 304, 112,336), new Vector2f(0.5f), new Vector2f(0.25f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else if (selectorOptions.get(selector) < 3) {
                        // Left Medium Leg-Tentacle
                        matrix.setRotate(rotation);
                        point[0] = -0.125f;
                        point[1] = -0.0625f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(0, 304, 24,352), new Vector2f(0.375f, 0.75f), new Vector2f(0.25f, 0.5f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else if (selectorOptions.get(selector) < 4) {
                        // Right Medium Leg-Tentacle
                        matrix.setRotate(rotation);
                        point[0] = 0.125f;
                        point[1] = -0.0625f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(24, 304, 48,352), new Vector2f(0.375f, 0.75f), new Vector2f(0.125f, 0.5f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else if (selectorOptions.get(selector) < 5) {
                        // Left Large Leg-Tentacle
                        matrix.setRotate(rotation);
                        point[0] = -0.25f;
                        point[1] = -0.125f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(48, 336, 80,400), new Vector2f(0.5f, 1.0f), new Vector2f(0.25f, 0.5f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else {
                        // Right Large Leg-Tentacle
                        matrix.setRotate(rotation);
                        point[0] = 0.25f;
                        point[1] = -0.125f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(80, 336, 112,400), new Vector2f(0.5f, 1.0f), new Vector2f(0.25f, 0.5f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    }

                    temp.m_velocity.x = -0.01f + (rand.nextFloat() * 0.02f);
                    destVec.addElement(temp);
                    selectorOptions.remove(selector);
                }

                // Splatter
                selector = rand.nextInt(4);
                randomFloat = 1.0f + (rand.nextFloat() * 0.5f);
                if (selector < 1) {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(240, 272, 320,352), new Vector2f(1.25f).multiply(randomFloat), new Vector2f(0.625f).multiply(randomFloat), position, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                } else if (selector < 2) {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(240, 352, 320,432), new Vector2f(1.25f).multiply(randomFloat), new Vector2f(0.625f).multiply(randomFloat), position, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                } else if (selector < 3) {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(240, 432, 320,512), new Vector2f(1.25f).multiply(randomFloat), new Vector2f(0.625f).multiply(randomFloat), position, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                } else {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(240, 512, 320,592), new Vector2f(1.25f).multiply(randomFloat), new Vector2f(0.625f).multiply(randomFloat), position, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                }
                temp.m_fadeOut = true;
                temp.m_fadeOutFactor = 4;
                destVec.addElement(temp);
                break;
            case GUARDIAN:
                // Gibs, 6 possibilities, get 3 - 4 thereof.
                selectorOptions.clear(); // Probably unnecessary.
                for (int i = 0; i < 6; i++) { selectorOptions.addElement(i); }
                randomInt = 3 + rand.nextInt(2);

                for (int i = 0; i < randomInt; i ++) {
                    selector = rand.nextInt(selectorOptions.size());
                    if (selectorOptions.get(selector) < 1) {
                        // Left Small Leg-Tentacle
                        matrix.setRotate(rotation);
                        point[0] = -0.25f;
                        point[1] = 0.375f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(80, 400, 96,416), new Vector2f(0.25f), new Vector2f(0.125f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else if (selectorOptions.get(selector) < 2) {
                        // Right Small Leg-Tentacle
                        matrix.setRotate(rotation);
                        point[0] = 0.25f;
                        point[1] = 0.375f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(96, 400, 112,416), new Vector2f(0.25f), new Vector2f(0.125f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else if (selectorOptions.get(selector) < 3) {
                        // Left Medium Leg-Tentacle
                        matrix.setRotate(rotation);
                        point[0] = -0.375f;
                        point[1] = 0.25f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(80, 416, 96,448), new Vector2f(0.25f, 0.5f), new Vector2f(0.125f, 0.25f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else if (selectorOptions.get(selector) < 4) {
                        // Right Medium Leg-Tentacle
                        matrix.setRotate(rotation);
                        point[0] = 0.375f;
                        point[1] = 0.25f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(96, 416, 112,448), new Vector2f(0.25f, 0.5f), new Vector2f(0.125f, 0.25f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else if (selectorOptions.get(selector) < 5) {
                        // Small Body-Ring
                        matrix.setRotate(rotation);
                        point[0] = 0.0f;
                        point[1] = 0.25f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(80, 448, 112,464), new Vector2f(0.5f, 0.25f), new Vector2f(0.25f, 0.125f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else {
                        // Large Body-Ring
                        matrix.setRotate(rotation);
                        point[0] = 0.0f;
                        point[1] = 0.0625f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(80, 464, 112,480), new Vector2f(0.5f, 0.25f), new Vector2f(0.25f, 0.125f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    }

                    temp.m_velocity.x = -0.01f + (rand.nextFloat() * 0.02f);
                    destVec.addElement(temp);
                    selectorOptions.remove(selector);
                }

                // Splatter
                selector = rand.nextInt(4);
                randomFloat = 1.0f + (rand.nextFloat() * 0.5f);
                if (selector < 1) {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(320, 512, 400,592), new Vector2f(1.25f).multiply(randomFloat), new Vector2f(0.625f).multiply(randomFloat), position, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                } else if (selector < 2) {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(400, 512, 480,592), new Vector2f(1.25f).multiply(randomFloat), new Vector2f(0.625f).multiply(randomFloat), position, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                } else if (selector < 3) {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(240, 592, 320,672), new Vector2f(1.25f).multiply(randomFloat), new Vector2f(0.625f).multiply(randomFloat), position, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                } else {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(320, 592, 400,672), new Vector2f(1.25f).multiply(randomFloat), new Vector2f(0.625f).multiply(randomFloat), position, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                }
                temp.m_fadeOut = true;
                temp.m_fadeOutFactor = 4;
                destVec.addElement(temp);
                break;
            case QUEEN:
                // Gibs, 10 possibilities, get 4 - 6 thereof.
                selectorOptions.clear(); // Probably unnecessary.
                for (int i = 0; i < 10; i++) { selectorOptions.addElement(i); }
                randomInt = 4 + rand.nextInt(3);

                for (int i = 0; i < randomInt; i ++) {
                    selector = rand.nextInt(selectorOptions.size());
                    if (selectorOptions.get(selector) < 1) {
                        // Left Small Leg-Tentacle
                        matrix.setRotate(rotation);
                        point[0] = -0.375f;
                        point[1] = 0.875f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(80, 480, 96,512), new Vector2f(0.25f, 0.5f), new Vector2f(0.125f, 0.25f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else if (selectorOptions.get(selector) < 2) {
                        // Right Small Leg-Tentacle
                        matrix.setRotate(rotation);
                        point[0] = 0.375f;
                        point[1] = 0.875f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(96, 480, 112,512), new Vector2f(0.25f, 0.5f), new Vector2f(0.125f, 0.25f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else if (selectorOptions.get(selector) < 3) {
                        // Left Medium Leg-Tentacle
                        matrix.setRotate(rotation);
                        point[0] = -0.625f;
                        point[1] = 0.75f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(80, 512, 112,560), new Vector2f(0.5f, 0.75f), new Vector2f(0.25f, 0.375f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else if (selectorOptions.get(selector) < 4) {
                        // Right Medium Leg-Tentacle
                        matrix.setRotate(rotation);
                        point[0] = 0.625f;
                        point[1] = 0.75f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(80, 560, 112,608), new Vector2f(0.5f, 0.75f), new Vector2f(0.25f, 0.375f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else if (selectorOptions.get(selector) < 5) {
                        // Left Large Leg-Tentacle
                        matrix.setRotate(rotation);
                        point[0] = -0.875f;
                        point[1] = 0.625f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(16, 560, 48,624), new Vector2f(0.5f, 1.0f), new Vector2f(0.25f, 0.5f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else if (selectorOptions.get(selector) < 6) {
                        // Right Large Leg-Tentacle
                        matrix.setRotate(rotation);
                        point[0] = 0.875f;
                        point[1] = 0.625f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(48, 560, 80,624), new Vector2f(0.5f, 1.0f), new Vector2f(0.25f, 0.5f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else if (selectorOptions.get(selector) < 7) {
                        // Left Upper Eye
                        matrix.setRotate(rotation);
                        point[0] = -0.1875f;
                        point[1] = -0.375f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(80, 128, 96,144), new Vector2f(0.25f), new Vector2f(0.125f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else if (selectorOptions.get(selector) < 8){
                        // Left Lower Eye
                        matrix.setRotate(rotation);
                        point[0] = -0.125f;
                        point[1] = -0.5f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(80, 128, 96,144), new Vector2f(0.25f), new Vector2f(0.125f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else if (selectorOptions.get(selector) < 9) {
                        // Right Upper Eye
                        matrix.setRotate(rotation);
                        point[0] = 0.1875f;
                        point[1] = -0.375f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(96, 128, 112,144), new Vector2f(0.25f), new Vector2f(0.125f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    } else {
                        // Right Lower Eye
                        matrix.setRotate(rotation);
                        point[0] = 0.125f;
                        point[1] = -0.5f;
                        matrix.mapPoints(point);
                        temp = new BackgroundObject("game_foreground_spritesheet", new Rect(96, 128, 112,144), new Vector2f(0.25f), new Vector2f(0.125f), Vector2f.add(position, new Vector2f(point[0], point[1])), rotation, (-0.06f + (rand.nextFloat() * 0.02f)), ((rand.nextFloat() * 4.0f) - 2.0f));
                    }

                    temp.m_velocity.x = -0.01f + (rand.nextFloat() * 0.02f);
                    destVec.addElement(temp);
                    selectorOptions.remove(selector);
                }

                // Splatter (x3)

                selector = rand.nextInt(4);
                randomFloat = 1.0f + (rand.nextFloat() * 0.5f);
                matrix.setRotate(rotation);
                point[0] = 0.0f;
                point[1] = 0.75f;
                matrix.mapPoints(point);
                if (selector < 1) {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(0, 128, 80,208), new Vector2f(1.25f).multiply(randomFloat), new Vector2f(0.625f).multiply(randomFloat), Vector2f.add(position, new Vector2f(point[0], point[1])), (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                } else if (selector < 2) {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(0, 208, 80,288), new Vector2f(1.25f).multiply(randomFloat), new Vector2f(0.625f).multiply(randomFloat), Vector2f.add(position, new Vector2f(point[0], point[1])), (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                } else if (selector < 3) {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(0, 400, 80,480), new Vector2f(1.25f).multiply(randomFloat), new Vector2f(0.625f).multiply(randomFloat), Vector2f.add(position, new Vector2f(point[0], point[1])), (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                } else {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(0, 480, 80,560), new Vector2f(1.25f).multiply(randomFloat), new Vector2f(0.625f).multiply(randomFloat), Vector2f.add(position, new Vector2f(point[0], point[1])), (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                }
                temp.m_fadeOut = true;
                temp.m_fadeOutFactor = 4;
                destVec.addElement(temp);

                selector = rand.nextInt(4);
                randomFloat = 1.0f + (rand.nextFloat() * 0.5f);
                matrix.setRotate(rotation);
                point[0] = 0.0f;
                point[1] = -0.75f;
                matrix.mapPoints(point);
                if (selector < 1) {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(0, 128, 80,208), new Vector2f(1.25f).multiply(randomFloat), new Vector2f(0.625f).multiply(randomFloat), Vector2f.add(position, new Vector2f(point[0], point[1])), (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                } else if (selector < 2) {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(0, 208, 80,288), new Vector2f(1.25f).multiply(randomFloat), new Vector2f(0.625f).multiply(randomFloat), Vector2f.add(position, new Vector2f(point[0], point[1])), (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                } else if (selector < 3) {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(0, 400, 80,480), new Vector2f(1.25f).multiply(randomFloat), new Vector2f(0.625f).multiply(randomFloat), Vector2f.add(position, new Vector2f(point[0], point[1])), (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                } else {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(0, 480, 80,560), new Vector2f(1.25f).multiply(randomFloat), new Vector2f(0.625f).multiply(randomFloat), Vector2f.add(position, new Vector2f(point[0], point[1])), (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                }
                temp.m_fadeOut = true;
                temp.m_fadeOutFactor = 4;
                destVec.addElement(temp);

                selector = rand.nextInt(4);
                randomFloat = 1.0f + (rand.nextFloat() * 0.5f);
                if (selector < 1) {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(0, 128, 80,208), new Vector2f(1.25f).multiply(randomFloat), new Vector2f(0.625f).multiply(randomFloat), position, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                } else if (selector < 2) {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(0, 208, 80,288), new Vector2f(1.25f).multiply(randomFloat), new Vector2f(0.625f).multiply(randomFloat), position, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                } else if (selector < 3) {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(0, 400, 80,480), new Vector2f(1.25f).multiply(randomFloat), new Vector2f(0.625f).multiply(randomFloat), position, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                } else {
                    temp = new BackgroundObject("game_foreground_spritesheet", new Rect(0, 480, 80,560), new Vector2f(1.25f).multiply(randomFloat), new Vector2f(0.625f).multiply(randomFloat), position, (rand.nextFloat() * 360.0f), -0.05f, ((rand.nextFloat() * 2.0f) - 1.0f));
                }
                temp.m_fadeOut = true;
                temp.m_fadeOutFactor = 4;
                destVec.addElement(temp);
                break;
        }
    }

    public static void getArmourGibs(Random rand, Vector<BackgroundObject> destVec, Armour armour) //!< Factory function to create destroyed armour visual effects.
    {
        BackgroundObject temp;
        Vector2f position = armour.getPosition();
        float rotation = armour.getRotation();
        String armourName = armour.m_typeName;

        switch (armourName) {
            case "queenleft":
                // Queen Left Arm
                temp = new BackgroundObject("game_foreground_spritesheet", new Rect(256, 128, 304, 208), new Vector2f(0.75f, 1.25f), new Vector2f(1.0f), position, rotation, -0.06f + (rand.nextFloat() * 0.02f), ((rand.nextFloat() * 2.0f) - 1.0f));
                temp.m_velocity.x = -0.01f + (rand.nextFloat() * 0.02f);
                destVec.addElement(temp);
                break;
            case "queenright":
                // Queen Right Arm
                temp = new BackgroundObject("game_foreground_spritesheet", new Rect(336, 128, 384, 208), new Vector2f(0.75f, 1.25f), new Vector2f(-0.25f, 1.0f), position, rotation, -0.06f + (rand.nextFloat() * 0.02f), ((rand.nextFloat() * 2.0f) - 1.0f));
                temp.m_velocity.x = -0.01f + (rand.nextFloat() * 0.02f);
                destVec.addElement(temp);
                break;
        }
    }

    public static void getLevelEffects(Context context, Random rand, Vector<BackgroundObject> destVec, String command, String parameter) //!< Factory function to create level-triggered visual effects.
    {
        // Many thanks to android--examples.blogspot.co.uk
        // Referenced heavily: https://android--examples.blogspot.co.uk/2015/11/android-how-to-draw-text-on-canvas.html

        // For reference:
        // public BackgroundObject(Bitmap bitmap, Vector2f position, float rotation, float yVelocity, float rotVelocity) //!< Creates a custom BackgroundObject from a bitmap.

        float yVel = -0.06f;
        int smallTextSize = 45;
        int largeTextSize = 90;
        int bitmapWidth = 1024;
        int bitmapHeight = 126;
        
        if (command.equalsIgnoreCase("level_victory")) {
            Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/franklin_gothic_heavy_regular.ttf");

            canvas.drawColor(Color.TRANSPARENT);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setAntiAlias(true);

            paint.setTextSize(largeTextSize);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(typeface);

            String txt = "LEVEL COMPLETE!";

            Rect rectangle = new Rect();
            paint.getTextBounds(
                    txt, // text
                    0, // start
                    txt.length(), // end
                    rectangle // bounds
            );

            canvas.drawText(
                    txt, // Text to draw
                    canvas.getWidth()/2, // x
                    canvas.getHeight()/2 + Math.abs(rectangle.height())/2, // y
                    paint // Paint
            );

            destVec.addElement(new BackgroundObject(bitmap, new Vector2f(0.0f, 21.0f), 0.0f, yVel, 0.0f));
        }
        else if (command.equalsIgnoreCase("scorecard")) {
            Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/franklin_gothic_heavy_regular.ttf");

            canvas.drawColor(Color.TRANSPARENT);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setAntiAlias(true);

            paint.setTextSize(smallTextSize);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(typeface);

            String txt = "SCORE: " + parameter;

            Rect rectangle = new Rect();
            paint.getTextBounds(
                    txt, // text
                    0, // start
                    txt.length(), // end
                    rectangle // bounds
            );

            canvas.drawText(
                    txt, // Text to draw
                    canvas.getWidth()/2, // x
                    canvas.getHeight()/2 + Math.abs(rectangle.height())/2, // y
                    paint // Paint
            );

            destVec.addElement(new BackgroundObject(bitmap, new Vector2f(0.0f, 21.0f), 0.0f, yVel, 0.0f));
        }
        else if (command.equalsIgnoreCase("game_over")) {
            Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/franklin_gothic_heavy_regular.ttf");

            canvas.drawColor(Color.TRANSPARENT);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.RED);
            paint.setAntiAlias(true);

            paint.setTextSize(largeTextSize);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(typeface);

            String txt = "GAME OVER";

            Rect rectangle = new Rect();
            paint.getTextBounds(
                    txt, // text
                    0, // start
                    txt.length(), // end
                    rectangle // bounds
            );

            canvas.drawText(
                    txt, // Text to draw
                    canvas.getWidth()/2, // x
                    canvas.getHeight()/2 + Math.abs(rectangle.height())/2, // y
                    paint // Paint
            );

            destVec.addElement(new BackgroundObject(bitmap, new Vector2f(0.0f, 21.0f), 0.0f, yVel, 0.0f));
        }
        else if (command.equalsIgnoreCase("level_01_start")) {
            // "Activate AUTO to shoot."
            Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/franklin_gothic_heavy_regular.ttf");

            canvas.drawColor(Color.TRANSPARENT);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setAntiAlias(true);

            paint.setTextSize(smallTextSize);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(typeface);

            String txt = "Activate AUTO to shoot.";

            Rect rectangle = new Rect();
            paint.getTextBounds(
                    txt, // text
                    0, // start
                    txt.length(), // end
                    rectangle // bounds
            );

            canvas.drawText(
                    txt, // Text to draw
                    canvas.getWidth()/2, // x
                    canvas.getHeight()/2 + Math.abs(rectangle.height())/2, // y
                    paint // Paint
            );

            destVec.addElement(new BackgroundObject(bitmap, new Vector2f(0.0f, 21.0f), 0.0f, yVel, 0.0f));

            // "Bomb and shield are limited!"
            bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);

            canvas.drawColor(Color.TRANSPARENT);

            txt = "Bomb and shield are limited!";

            rectangle = new Rect();
            paint.getTextBounds(
                    txt, // text
                    0, // start
                    txt.length(), // end
                    rectangle // bounds
            );

            canvas.drawText(
                    txt, // Text to draw
                    canvas.getWidth()/2, // x
                    canvas.getHeight()/2 + Math.abs(rectangle.height())/2, // y
                    paint // Paint
            );

            destVec.addElement(new BackgroundObject(bitmap, new Vector2f(0.0f, 23.0f), 0.0f, yVel, 0.0f));

            // "Clear all enemies!"
            bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);

            canvas.drawColor(Color.TRANSPARENT);

            txt = "Clear all enemies!";

            rectangle = new Rect();
            paint.getTextBounds(
                    txt, // text
                    0, // start
                    txt.length(), // end
                    rectangle // bounds
            );

            canvas.drawText(
                    txt, // Text to draw
                    canvas.getWidth()/2, // x
                    canvas.getHeight()/2 + Math.abs(rectangle.height())/2, // y
                    paint // Paint
            );

            destVec.addElement(new BackgroundObject(bitmap, new Vector2f(0.0f, 25.0f), 0.0f, yVel, 0.0f));

            // "LEVEL 1"
            bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);

            canvas.drawColor(Color.TRANSPARENT);

            paint.setTextSize(largeTextSize);

            txt = "LEVEL 1";

            rectangle = new Rect();
            paint.getTextBounds(
                    txt, // text
                    0, // start
                    txt.length(), // end
                    rectangle // bounds
            );

            canvas.drawText(
                    txt, // Text to draw
                    canvas.getWidth()/2, // x
                    canvas.getHeight()/2 + Math.abs(rectangle.height())/2, // y
                    paint // Paint
            );

            destVec.addElement(new BackgroundObject(bitmap, new Vector2f(0.0f, 28.0f), 0.0f, yVel, 0.0f));
        }
        else if (command.equalsIgnoreCase("level_02_start")) {
            // "Being hit costs you score."
            Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/franklin_gothic_heavy_regular.ttf");

            canvas.drawColor(Color.TRANSPARENT);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setAntiAlias(true);

            paint.setTextSize(smallTextSize);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(typeface);

            String txt = "Being hit costs you score.";

            Rect rectangle = new Rect();
            paint.getTextBounds(
                    txt, // text
                    0, // start
                    txt.length(), // end
                    rectangle // bounds
            );

            canvas.drawText(
                    txt, // Text to draw
                    canvas.getWidth()/2, // x
                    canvas.getHeight()/2 + Math.abs(rectangle.height())/2, // y
                    paint // Paint
            );

            destVec.addElement(new BackgroundObject(bitmap, new Vector2f(0.0f, 21.0f), 0.0f, yVel, 0.0f));

            // "Don't get hit!"
            bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);

            canvas.drawColor(Color.TRANSPARENT);

            txt = "Don't get hit!";

            rectangle = new Rect();
            paint.getTextBounds(
                    txt, // text
                    0, // start
                    txt.length(), // end
                    rectangle // bounds
            );

            canvas.drawText(
                    txt, // Text to draw
                    canvas.getWidth()/2, // x
                    canvas.getHeight()/2 + Math.abs(rectangle.height())/2, // y
                    paint // Paint
            );

            destVec.addElement(new BackgroundObject(bitmap, new Vector2f(0.0f, 23.0f), 0.0f, yVel, 0.0f));

            // "LEVEL 2"
            bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);

            paint.setTextSize(largeTextSize);

            txt = "LEVEL 2";

            rectangle = new Rect();
            paint.getTextBounds(
                    txt, // text
                    0, // start
                    txt.length(), // end
                    rectangle // bounds
            );

            canvas.drawText(
                    txt, // Text to draw
                    canvas.getWidth()/2, // x
                    canvas.getHeight()/2 + Math.abs(rectangle.height())/2, // y
                    paint // Paint
            );

            destVec.addElement(new BackgroundObject(bitmap, new Vector2f(0.0f, 25.0f), 0.0f, yVel, 0.0f));
        }
        else if (command.equalsIgnoreCase("level_03_start")) {
            // "Get more charges every 500 points!"
            Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/franklin_gothic_heavy_regular.ttf");

            canvas.drawColor(Color.TRANSPARENT);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setAntiAlias(true);

            paint.setTextSize(smallTextSize);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(typeface);

            String txt = "Get more charges every 500 points!";

            Rect rectangle = new Rect();
            paint.getTextBounds(
                    txt, // text
                    0, // start
                    txt.length(), // end
                    rectangle // bounds
            );

            canvas.drawText(
                    txt, // Text to draw
                    canvas.getWidth()/2, // x
                    canvas.getHeight()/2 + Math.abs(rectangle.height())/2, // y
                    paint // Paint
            );

            destVec.addElement(new BackgroundObject(bitmap, new Vector2f(0.0f, 21.0f), 0.0f, yVel, 0.0f));

            // "Kill all enemies for a perfection bonus!"
            bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);

            canvas.drawColor(Color.TRANSPARENT);

            txt = "Kill all enemies for a perfection bonus!";

            rectangle = new Rect();
            paint.getTextBounds(
                    txt, // text
                    0, // start
                    txt.length(), // end
                    rectangle // bounds
            );

            canvas.drawText(
                    txt, // Text to draw
                    canvas.getWidth()/2, // x
                    canvas.getHeight()/2 + Math.abs(rectangle.height())/2, // y
                    paint // Paint
            );

            destVec.addElement(new BackgroundObject(bitmap, new Vector2f(0.0f, 23.0f), 0.0f, yVel, 0.0f));

            // "LEVEL 3"
            bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);

            paint.setTextSize(largeTextSize);

            txt = "LEVEL 3";

            rectangle = new Rect();
            paint.getTextBounds(
                    txt, // text
                    0, // start
                    txt.length(), // end
                    rectangle // bounds
            );

            canvas.drawText(
                    txt, // Text to draw
                    canvas.getWidth()/2, // x
                    canvas.getHeight()/2 + Math.abs(rectangle.height())/2, // y
                    paint // Paint
            );

            destVec.addElement(new BackgroundObject(bitmap, new Vector2f(0.0f, 25.0f), 0.0f, yVel, 0.0f));
        }
        else if (command.equalsIgnoreCase("victory")) {
            Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/franklin_gothic_heavy_regular.ttf");

            canvas.drawColor(Color.TRANSPARENT);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setAntiAlias(true);

            paint.setTextSize(largeTextSize);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(typeface);

            String txt = "VICTORY!";

            Rect rectangle = new Rect();
            paint.getTextBounds(
                    txt, // text
                    0, // start
                    txt.length(), // end
                    rectangle // bounds
            );

            canvas.drawText(
                    txt, // Text to draw
                    canvas.getWidth()/2, // x
                    canvas.getHeight()/2 + Math.abs(rectangle.height())/2, // y
                    paint // Paint
            );

            destVec.addElement(new BackgroundObject(bitmap, new Vector2f(0.0f, 27.0f), 0.0f, yVel, 0.0f));

            bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);

            canvas.drawColor(Color.TRANSPARENT);

            paint.setTextSize(smallTextSize);

            txt = "FINAL SCORE: " + parameter;

            rectangle = new Rect();
            paint.getTextBounds(
                    txt, // text
                    0, // start
                    txt.length(), // end
                    rectangle // bounds
            );

            canvas.drawText(
                    txt, // Text to draw
                    canvas.getWidth()/2, // x
                    canvas.getHeight()/2 + Math.abs(rectangle.height())/2, // y
                    paint // Paint
            );

            destVec.addElement(new BackgroundObject(bitmap, new Vector2f(0.0f, 29.0f), 0.0f, yVel, 0.0f));
        }
        else if (command.equalsIgnoreCase("add_charges")) {
            Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/franklin_gothic_heavy_regular.ttf");

            canvas.drawColor(Color.TRANSPARENT);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.GREEN);
            paint.setAntiAlias(true);

            paint.setTextSize(smallTextSize);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(typeface);

            String txt = "CHARGES ADDED!";

            Rect rectangle = new Rect();
            paint.getTextBounds(
                    txt, // text
                    0, // start
                    txt.length(), // end
                    rectangle // bounds
            );

            canvas.drawText(
                    txt, // Text to draw
                    canvas.getWidth()/2, // x
                    canvas.getHeight()/2 + Math.abs(rectangle.height())/2, // y
                    paint // Paint
            );

            destVec.addElement(new BackgroundObject(bitmap, new Vector2f(0.0f, 10.0f), 0.0f, yVel, 0.0f));
        }
        else if (command.equalsIgnoreCase("add_score")) {
            Bitmap bitmap = Bitmap.createBitmap(256, bitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/franklin_gothic_medium_cond_regular.ttf");

            canvas.drawColor(Color.TRANSPARENT);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(95, 0, 191, 0));
            paint.setAntiAlias(true);

            paint.setTextSize(smallTextSize);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(typeface);

            String txt = "+" + parameter;

            Rect rectangle = new Rect();
            paint.getTextBounds(
                    txt, // text
                    0, // start
                    txt.length(), // end
                    rectangle // bounds
            );

            canvas.drawText(
                    txt, // Text to draw
                    canvas.getWidth()/2, // x
                    canvas.getHeight()/2 + Math.abs(rectangle.height())/2, // y
                    paint // Paint
            );

            float xVal = (rand.nextFloat() * 10.0f) - 5.0f;
            float rotation = rand.nextFloat() - 0.5f;
            destVec.addElement(new BackgroundObject(bitmap, new Vector2f(xVal, 21.0f), 0.0f, -0.025f, 0.0f));
        }
        else if (command.equalsIgnoreCase("subtract_score")) {
            Bitmap bitmap = Bitmap.createBitmap(256, bitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/franklin_gothic_medium_cond_regular.ttf");

            canvas.drawColor(Color.TRANSPARENT);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(95, 191, 0, 0));
            paint.setAntiAlias(true);

            paint.setTextSize(smallTextSize);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(typeface);

            String txt = "-" + parameter;

            Rect rectangle = new Rect();
            paint.getTextBounds(
                    txt, // text
                    0, // start
                    txt.length(), // end
                    rectangle // bounds
            );

            canvas.drawText(
                    txt, // Text to draw
                    canvas.getWidth()/2, // x
                    canvas.getHeight()/2 + Math.abs(rectangle.height())/2, // y
                    paint // Paint
            );

            float xVal = (rand.nextFloat() * 10.0f) - 5.0f;
            float rotation = rand.nextFloat() - 0.5f;
            destVec.addElement(new BackgroundObject(bitmap, new Vector2f(xVal, 21.0f), 0.0f, -0.025f, 0.0f));
        }
    }
}
