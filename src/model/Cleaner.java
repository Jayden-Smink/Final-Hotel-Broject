package model;

import java.util.ArrayList;

public class Cleaner extends Person {
    public CleanerState state = CleanerState.IDLE;
    public boolean isDead = false;
    public int assignedRoomId = -1;
    public int homeRoomId = -1;
    public int cleaningTimer = 0;
    public ArrayList<Integer> dirtyRooms = new ArrayList<>(); // ← add this

    public Cleaner(int id, double x, double y) {
        super(id, x, y);
    }

    @Override
    public void setTarget(double tx, double ty) {
        super.setTarget(tx, ty);
    }
}