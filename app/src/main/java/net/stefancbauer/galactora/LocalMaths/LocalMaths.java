package net.stefancbauer.galactora.LocalMaths;

import java.util.Random;

/**
 * Used as a container for assorted useful static maths functions.
 */

public class LocalMaths {

    public static Vector2f getPointInCircle() { return getPointInCircle(new Random(), 1.0f); }
    public static Vector2f getPointInCircle(float radius) { return getPointInCircle(new Random(), radius); }
    public static Vector2f getPointInCircle(Random rand) { return getPointInCircle(rand, 1.0f); }
    public static Vector2f getPointInCircle(Random rand, float radius) //!< Returns a statistically evenly distributed point within a circle of the specified radius, centred on (0, 0).
    {
        // Credit to Stack Overflow users for original problem solution, which is frighteningly elegant and subtle.
        // https://stackoverflow.com/questions/5837572/generate-a-random-point-within-a-circle-uniformly
        float t = (float)(2.0 * Math.PI * rand.nextFloat());
        float u = rand.nextFloat() + rand.nextFloat();
        float r;
        if (u > 1) { r = 2.0f - u; } else { r = u; }
        return new Vector2f((float)(r * Math.cos(t)), (float)(r * Math.sin(t))).multiply(radius);
    }
}
