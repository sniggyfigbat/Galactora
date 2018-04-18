package net.stefancbauer.galactora.LocalMaths;

/**
 * Created by P13186907 on 16/02/2018.
 */

public class Vector2d {

    public double x;
    public double y;

    public Vector2d() {
        this.x = 0.0d;
        this.y = 0.0d;
    }

    public Vector2d(double x) {
        this.x = x;
        this.y = x;
    }

    public Vector2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2d(Vector2d in) {
        this.x = in.x;
        this.y = in.y;
    }

    public static boolean areEqual(Vector2d inp_A, Vector2d inp_B) {
        boolean returnBool = true;
        if (inp_A.x != inp_B.x) { returnBool = false; }
        if (inp_A.y != inp_B.y) { returnBool = false; }
        return returnBool;
    }

    public static boolean areWithin(Vector2d inp_A, Vector2d inp_B, double margin) {
        boolean returnBool = true;
        if (inp_A.x < inp_B.x - margin) { returnBool = false; }
        if (inp_A.x > inp_B.x + margin) { returnBool = false; }
        if (inp_A.y < inp_B.y - margin) { returnBool = false; }
        if (inp_A.y > inp_B.y + margin) { returnBool = false; }
        return returnBool;
    }


    // Setters
    public void setZero() {
        this.x = 0.0d;
        this.y = 0.0d;
    }

    public void set(double x) {
        this.x = x;
        this.y = x;
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void set(Vector2d input) {
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

    public Vector2l toLong() {
        long newX = (long)this.x;
        long newY = (long)this.y;
        return new Vector2l(newX, newY);
    }


    // Maths and Utility
    // Addition
    public static Vector2d add(Vector2d first, Vector2d second) {
        return new Vector2d(first.x + second.x, first.y + second.y);
    }

    // Scalar Multiplication
    public Vector2d multiply(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }

    // Subtraction
    public static Vector2d subtract(Vector2d first, Vector2d second) {
        return new Vector2d(first.x - second.x, first.y - second.y);
    }

    // Dot Product
    public static double dotProduct(Vector2d first, Vector2d second) {
        return ((first.x * second.x) + (first.y * second.y));
    }

    // Get Magnitude Squared
    public double getMagnitudeSquared() {
        return ((this.x * this.x) + (this.y * this.y));
    }

    // Get Magnitude
    public double getMagnitude() {
        return Math.sqrt((this.x * this.x) + (this.y * this.y));
    }

    // Set Magnitude
    public void setMagnitude(double newMagnitude) {
        if (newMagnitude == 0.0) { this.set(0.0); }
        else {
            double oldMagnitude = Math.sqrt((this.x * this.x) + (this.y * this.y));
            this.x *= (newMagnitude / oldMagnitude);
            this.y *= (newMagnitude / oldMagnitude);
        }
    }

    // Get Midpoint
    public static Vector2d getMidpoint(Vector2d first, Vector2d second) {
        double xVal = ((first.x - second.x) / 2) + second.x;
        double yVal = ((first.y - second.y) / 2) + second.y;
        return new Vector2d(xVal, yVal);
    }

    // Get Unit Vector
    public Vector2d getUnitVector() {
        double magnitude = getMagnitude();
        if (magnitude == 0.0) { return new Vector2d(); }
        else { return new Vector2d(this).multiply(1.0 / magnitude); }
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