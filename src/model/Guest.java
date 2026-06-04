package model;

public class Guest extends Person {
    public GuestState state = GuestState.IDLE;
    public int assignedRoomId = -1;
    public boolean isCheckingOut = false;

    public int activityTimer = 0;
    public boolean isInRoom = false;
    public String currentActivity = "NONE";

    public int elevatorWaitTimer = 0;  // hoe lang de gast al wacht op de lift
    public boolean forceStairs = false; // geeft op en neemt de trap
    public int waitingOnFloor = -1;

    public Guest(int id, double x, double y) {
        super(id, x, y);
        this.state = GuestState.WALKING;
        this.currentActivity = "ROOM";
    }

    @Override
    public void setTarget(double tx, double ty) {
        super.setTarget(tx, ty);
        this.state = GuestState.WALKING;
    }
}