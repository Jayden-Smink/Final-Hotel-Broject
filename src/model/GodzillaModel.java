package model;

public class GodzillaModel {
    public double x;
    public double y; // Let op: deze basis-y wordt momenteel niet actief gebruikt voor loopgedrag, maar behouden we
    public double speed;
    public boolean isActive = false;
    public int currentColumn = 0;
    public int columnDestroyTimer = 0;
    public static final int COLUMN_DESTROY_INTERVAL = 120; // 2 seconds per column at 60fps

    // ✅ Toegevoegd: Houdt de tijd bij voor de danspasjes
    private int danceTick = 0;

    public GodzillaModel(double startX, double startY, double speed) {
        this.x = startX;
        this.y = startY;
        this.speed = speed;
    }

    public void moveRight() {
        x += speed;
        if (isActive) {
            danceTick++; // Verhoog de teller zolang Godzilla actief is
        }
    }

    // ✅ Getters voor de dansbewegingen (Tuned op een lekker ritme)
    public int getDanceOffsetY() {
        // Stuitert op en neer: Math.sin geeft waarden tussen -1 en 1.
        // Delen door 4.0 bepaalt de snelheid, vermenigvuldigen met 15 bepaalt de hoogte (15 pixels bounce).
        return (int) (Math.sin(danceTick / 4.0) * 15);
    }

    public int getDanceOffsetX() {
        // Wiegt naar links en rechts (met een cosinus zodat het breekt met de y-bounce)
        return (int) (Math.cos(danceTick / 6.0) * 8);
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