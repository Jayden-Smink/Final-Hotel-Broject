package model;

public class GodzillaModel {
    public double x;
    public double y;
    public double speed;
    public boolean isActive = false;
    public int currentColumn = 0;
    public int columnDestroyTimer = 0;
    public static final int COLUMN_DESTROY_INTERVAL = 120; // 2 seconds per column at 60fps

    public GodzillaModel(double startX, double startY, double speed) {
        this.x = startX;
        this.y = startY;
        this.speed = speed;
    }

    public void moveRight() {
        x += speed;
    }

    public boolean hasReachedNextColumn(int tileSize, int horizontalOffset) {
        int expectedX = (currentColumn * tileSize) + horizontalOffset;
        return x >= expectedX;
    }

    public void nextColumn() {
        currentColumn++;
        columnDestroyTimer = 0;
    }

    public boolean isDone(int maxColumn) {
        return currentColumn > maxColumn;
    }
}