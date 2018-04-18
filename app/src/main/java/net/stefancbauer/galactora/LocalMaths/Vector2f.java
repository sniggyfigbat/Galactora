package net.stefancbauer.galactora.LocalMaths;

/**
 * Created by P13186907 on 16/02/2018.
 */

public class Vector2f {

    public float x;
    public float y;

    public Vector2f() {
        this.x = 0.0f;
        this.y = 0.0f;
    }

    public Vector2f(float x) {
        this.x = x;
        this.y = x;
    }

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2f(Vector2f in) {
        this.x = in.x;
        this.y = in.y;
    }

    public static boolean areEqual(Vector2f inp_A, Vector2f inp_B) {
        boolean returnBool = true;
        if (inp_A.x != inp_B.x) { returnBool = false; }
        if (inp_A.y != inp_B.y) { returnBool = false; }
        return returnBool;
    }

    public static boolean areWithin(Vector2f inp_A, Vector2f inp_B, float margin) {
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

    public void set(float x) {
        this.x = x;
        this.y = x;
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void set(Vector2f input) {
        this.x = input.x;
        this.y = input.y;
    }


    // Type Converters
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

    public Vector2l toLong() {
        long newX = (long)this.x;
        long newY = (long)this.y;
        return new Vector2l(newX, newY);
    }


    // Maths and Utility
    // Addition
    public static Vector2f add(Vector2f first, Vector2f second) {
        return new Vector2f(first.x + second.x, first.y + second.y);
    }

    // Scalar Multiplication
    public Vector2f multiply(float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }

    // Subtraction
    public static Vector2f subtract(Vector2f first, Vector2f second) {
        return new Vector2f(first.x - second.x, first.y - second.y);
    }

    // Dot Product
    public static float dotProduct(Vector2f first, Vector2f second) {
        return ((first.x * second.x) + (first.y * second.y));
    }

    // Get Magnitude Squared
    public float getMagnitudeSquared() {
        return ((this.x * this.x) + (this.y * this.y));
    }

    // Get Magnitude
    public float getMagnitude() {
        return (float)Math.sqrt((this.x * this.x) + (this.y * this.y));
    }

    // Set Magnitude
    public void setMagnitude(float newMagnitude) {
        if (newMagnitude == 0.0f) { this.set(0.0f); }
        else {
            double oldMagnitude = Math.sqrt((this.x * this.x) + (this.y * this.y));
            this.x *= (float) (newMagnitude / oldMagnitude);
            this.y *= (float) (newMagnitude / oldMagnitude);
        }
    }

    // Get Midpoint
    public static Vector2f getMidpoint(Vector2f first, Vector2f second) {
        float xVal = ((first.x - second.x) / 2) + second.x;
        float yVal = ((first.y - second.y) / 2) + second.y;
        return new Vector2f(xVal, yVal);
    }

    // Get Unit Vector
    public Vector2f getUnitVector() {
        float magnitude = getMagnitude();
        if (magnitude == 0.0f) { return new Vector2f(); }
        else { return new Vector2f(this).multiply(1.0f / magnitude); }
    }

    // Get String
    public String getString(){
        return "(" + this.x + ", " + this.y + ")";
    }

    public String getString(int decimalPlaces){
        int factor = (int)Math.pow(10, decimalPlaces);
        double roundedX = Math.round(this.x * factor) / factor;
        double roundedY = Math.round(this.y * factor) / factor;
        return "(" + roundedX + ", " + roundedY + ")";
    }
}
