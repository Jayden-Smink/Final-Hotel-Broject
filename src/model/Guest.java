package model;

public class Guest extends Person {
    public GuestState state = GuestState.IDLE;
    public int assignedRoomId = -1;
    public boolean isCheckingOut = false;

    // --- NIEUW: Variabelen voor de dynamische routinecyclus ---
    public int activityTimer = 0;
    public boolean isInRoom = false;
    public String currentActivity = "NONE"; // "NONE", "ROOM", "RESTAURANT", "CINEMA", "FITNESS"

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