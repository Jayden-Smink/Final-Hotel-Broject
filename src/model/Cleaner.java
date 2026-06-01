package model;

public class Cleaner extends Person {
    public CleanerState state = CleanerState.IDLE;
    public int assignedRoomId = -1;
    public int homeRoomId = -1; // fixed starting room
    public int cleaningTimer = 0;

    public Cleaner(int id, double x, double y) {
        super(id, x, y);
    }

    @Override
    public void setTarget(double tx, double ty) {
        super.setTarget(tx, ty);
    }
}