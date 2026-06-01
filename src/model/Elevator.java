package model;

import java.util.ArrayList;
import java.util.List;

public class Elevator {
    public double curX;
    public double curY;
    public int targetFloor;
    public boolean isMoving;
    public int maxCapacity = 10;
    public List<Guest> passengers = new ArrayList<>();

    private final int tileSize;
    private final double elevatorSpeed = 0.8;

    public int minFloor = 0;
    public int maxFloor = 999;

    /*
     * 1 = letterlijk 1 tick.
     * Bij Timer(16) is dat bijna onzichtbaar.
     * Zet op 30 of 60 als je de stop duidelijk wilt zien.
     */
    private final int waitTicksPerFloor = 30;
    private int waitTicksRemaining = 0;
    private int lastArrivedFloor = -1;

    public Elevator(double startX, double startY) {
        this(startX, startY, 60);
    }

    public Elevator(double startX, double startY, int tileSize) {
        this.tileSize = tileSize;
        this.curX = startX;
        this.curY = startY;
        this.targetFloor = (int) (startY / tileSize);
        this.isMoving = false;
        this.lastArrivedFloor = this.targetFloor;
    }

    public void setBounds(int minFloor, int maxFloor) {
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;

        clampTargetFloor();
        clampCurrentY();

        lastArrivedFloor = getCurrentFloor();
    }

    public void setTargetFloor(int floor) {
        if (floor < minFloor) {
            targetFloor = minFloor;
        } else if (floor > maxFloor) {
            targetFloor = maxFloor;
        } else {
            targetFloor = floor;
        }
    }

    public int getCurrentFloor() {
        return (int) Math.round(curY / tileSize);
    }

    public boolean isWaiting() {
        return waitTicksRemaining > 0;
    }

    public void update() {
        clampTargetFloor();

        /*
         * Eerst wachten afhandelen.
         * Zolang waitTicksRemaining > 0 beweegt de lift NIET.
         */
        if (waitTicksRemaining > 0) {
            waitTicksRemaining--;
            isMoving = false;
            updatePassengerPositions();
            return;
        }

        double targetY = targetFloor * tileSize;

        if (Math.abs(curY - targetY) > elevatorSpeed) {
            isMoving = true;

            if (curY < targetY) {
                curY += elevatorSpeed;
            } else {
                curY -= elevatorSpeed;
            }
        } else {
            curY = targetY;
            isMoving = false;

            int arrivedFloor = getCurrentFloor();

            /*
             * Alleen wachten als de lift op een nieuwe verdieping aankomt.
             * Hij blijft dus niet eindeloos wachten op dezelfde verdieping.
             */
            if (arrivedFloor != lastArrivedFloor) {
                waitTicksRemaining = waitTicksPerFloor;
                lastArrivedFloor = arrivedFloor;
            }
        }

        clampCurrentY();
        updatePassengerPositions();
    }

    private void clampTargetFloor() {
        if (targetFloor < minFloor) {
            targetFloor = minFloor;
        }

        if (targetFloor > maxFloor) {
            targetFloor = maxFloor;
        }
    }

    private void clampCurrentY() {
        double minY = minFloor * tileSize;
        double maxY = maxFloor * tileSize;

        if (curY < minY) {
            curY = minY;
            isMoving = false;
        }

        if (curY > maxY) {
            curY = maxY;
            isMoving = false;
        }
    }

    private void updatePassengerPositions() {
        for (Guest guest : passengers) {
            guest.y = this.curY;
            guest.x = this.curX + 5;
        }
    }
}