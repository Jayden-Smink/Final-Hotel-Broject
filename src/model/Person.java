package model;

import java.util.Random;

public abstract class Person {
    public int id;
    public double x, y;
    public double targetX, targetY;
    public double speed = 2.0;

    // Unieke afwijking om te zorgen dat poppetjes niet in elkaar overlopen
    public int personalOffset;

    public Person(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;

        // Genereert een flinke offset tussen -25 en +25 pixels voor duidelijke afstand
        this.personalOffset = new Random().nextInt(51) - 25;
    }

    public void setTarget(double tx, double ty) {
        this.targetX = tx;
        this.targetY = ty;
    }
}