package net.stefancbauer.galactora.LocalMaths;

/**
 * Created by P13186907 on 16/02/2018.
 */

public class Vector2i {

    public int x;
    public int y;

    public Vector2i() {
        this.x = 0;
        this.y = 0;
    }

    public Vector2i(int x) {
        this.x = x;
        this.y = x;
    }

    public Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2i(Vector2i in) {
        this.x = in.x;
        this.y = in.y;
    }

    public static boolean areEqual(Vector2i inp_A, Vector2i inp_B) {
        boolean returnBool = true;
        if (inp_A.x != inp_B.x) { returnBool = false; }
        if (inp_A.y != inp_B.y) { returnBool = false; }
        return returnBool;
    }

    public static boolean areWithin(Vector2i inp_A, Vector2i inp_B, int margin) {
        boolean returnBool = true;
        if (inp_A.x < inp_B.x - margin) { returnBool = false; }
        if (inp_A.x > inp_B.x + margin) { returnBool = false; }
        if (inp_A.y < inp_B.y - margin) { returnBool = false; }
        if (inp_A.y > inp_B.y + margin) { returnBool = false; }
        return returnBool;
    }


    // Setters
    public void setZero() {
        this.x = 0;
        this.y = 0;
    }

    public void set(int x) {
        this.x = x;
        this.y = x;
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void set(Vector2i input) {
        this.x = input.x;
        this.y = input.y;
    }


    // Type Converters
    public Vector2f toFloat() {
        float newX = (float)this.x;
        float newY = (float)this.y;
        return new Vector2f(newX, newY);
    }

    public Vector2d toDouble() {
        double newX = (double)this.x;
        double newY = (double)this.y;
        return new Vector2d(newX, newY);
    }

    public Vector2l toLong() {
        long newX = (long)this.x;
        long newY = (long)this.y;
        return new Vector2l(newX, newY);
    }


    // Maths and Utility
    // Addition
    public static Vector2i add(Vector2i first, Vector2i second) {
        return new Vector2i(first.x + second.x, first.y + second.y);
    }

    // Scalar Multiplication
    public Vector2i multiply(float scalar) {
        // Warning! This will lose all non-integer data!
        // It is recommended you convert to a Vector2f instead!
        this.x = (int)((float)this.x * scalar);
        this.y = (int)((float)this.y * scalar);
        return this;
    }

    // Subtraction
    public static Vector2i subtract(Vector2i first, Vector2i second) {
        return new Vector2i(first.x - second.x, first.y - second.y);
    }

    // Dot Product
    public static int dotProduct(Vector2i first, Vector2i second) {
        return ((first.x * second.x) + (first.y * second.y));
    }

    // Get Magnitude Squared
    public int getMagnitudeSquared() {
        return ((this.x * this.x) + (this.y * this.y));
    }

    // Get Magnitude
    public float getMagnitude() {
        return (float)Math.sqrt((this.x * this.x) + (this.y * this.y));
    }

    // Set Magnitude
    public void setMagnitude(float newMagnitude) {
        // Warning! This will lose all non-integer data!
        // It is recommended you convert to a Vector2f instead!
        if (newMagnitude == 0.0f) { this.set(0); }
        else {
            double oldMagnitude = Math.sqrt((this.x * this.x) + (this.y * this.y));
            this.x *= (float) (newMagnitude / oldMagnitude);
            this.y *= (float) (newMagnitude / oldMagnitude);
        }
    }

    // Get Midpoint
    public static Vector2f getMidpoint(Vector2i first, Vector2i second) {
        float xVal = ((first.x - second.x) / 2) + second.x;
        float yVal = ((first.y - second.y) / 2) + second.y;
        return new Vector2f(xVal, yVal);
    }

    // Get Unit Vector
    public Vector2i getUnitVector() {
        float magnitude = getMagnitude();
        if (magnitude == 0.0f) { return new Vector2i(); }
        else { return new Vector2i(this).multiply(1.0f / magnitude); }
    }

    // Get String
    public String getString() {
        return "(" + this.x + ", " + this.y + ")";
    }
}
