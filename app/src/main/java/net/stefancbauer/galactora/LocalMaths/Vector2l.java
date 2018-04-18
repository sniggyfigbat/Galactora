package net.stefancbauer.galactora.LocalMaths;

/**
 * Created by P13186907 on 16/02/2018.
 */

public class Vector2l {

    public long x;
    public long y;

    public Vector2l() {
        this.x = 0;
        this.y = 0;
    }

    public Vector2l(long x) {
        this.x = x;
        this.y = x;
    }

    public Vector2l(long x, long y) {
        this.x = x;
        this.y = y;
    }

    public Vector2l(Vector2l in) {
        this.x = in.x;
        this.y = in.y;
    }

    public static boolean areEqual(Vector2l inp_A, Vector2l inp_B) {
        boolean returnBool = true;
        if (inp_A.x != inp_B.x) { returnBool = false; }
        if (inp_A.y != inp_B.y) { returnBool = false; }
        return returnBool;
    }

    public static boolean areWithin(Vector2l inp_A, Vector2l inp_B, long margin) {
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

    public void set(long x) {
        this.x = x;
        this.y = x;
    }

    public void set(long x, long y) {
        this.x = x;
        this.y = y;
    }

    public void set(Vector2l input) {
        this.x = input.x;
        this.y = input.y;
    }


    // Type Converters
    public Vector2f toFloat() {
        float newX = (float)this.x;
        float newY = (float)this.y;
        return new Vector2f(newX, newY);
    }

    public Vector2i toInteger() {
        int newX = (int)this.x;
        int newY = (int)this.y;
        return new Vector2i(newX, newY);
    }

    public Vector2d toDouble() {
        double newX = (double)this.x;
        double newY = (double)this.y;
        return new Vector2d(newX, newY);
    }


    // Maths and Utility
    // Addition
    public static Vector2l add(Vector2l first, Vector2l second) {
        return new Vector2l(first.x + second.x, first.y + second.y);
    }

    // Scalar Multiplication
    public Vector2l multiply(double scalar) {
        // Warning! This will lose all non-integer data!
        // It is recommended you convert to a Vector2l instead!
        this.x = (long)((double)this.x * scalar);
        this.y = (long)((double)this.y * scalar);
        return this;
    }

    // Subtraction
    public static Vector2l subtract(Vector2l first, Vector2l second) {
        return new Vector2l(first.x - second.x, first.y - second.y);
    }

    // Dot Product
    public static long dotProduct(Vector2l first, Vector2l second) {
        return ((first.x * second.x) + (first.y * second.y));
    }

    // Get Magnitude Squared
    public long getMagnitudeSquared() {
        return ((this.x * this.x) + (this.y * this.y));
    }

    // Get Magnitude
    public double getMagnitude() {
        return Math.sqrt((this.x * this.x) + (this.y * this.y));
    }

    // Set Magnitude
    public void setMagnitude(double newMagnitude) {
        // Warning! This will lose all non-integer data!
        // It is recommended you convert to a Vector2l instead!
        if (newMagnitude == 0.0f) { this.set(0); }
        else {
            double oldMagnitude = Math.sqrt((this.x * this.x) + (this.y * this.y));
            this.x *= (double) (newMagnitude / oldMagnitude);
            this.y *= (double) (newMagnitude / oldMagnitude);
        }
    }

    // Get Midpoint
    public static Vector2d getMidpoint(Vector2l first, Vector2l second) {
        double xVal = ((first.x - second.x) / 2) + second.x;
        double yVal = ((first.y - second.y) / 2) + second.y;
        return new Vector2d(xVal, yVal);
    }

    // Get Unit Vector
    public Vector2l getUnitVector() {
        double magnitude = getMagnitude();
        if (magnitude == 0.0) { return new Vector2l(); }
        else { return new Vector2l(this).multiply(1.0 / magnitude); }
    }

    // Get String
    public String getString() {
        return "(" + this.x + ", " + this.y + ")";
    }
}
