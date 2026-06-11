package model;

public class FireDestruction implements IDestructionStrategy{
    private final int fireDurationFrames;

    public FireDestruction(int fireDurationSeconds) {
        this.fireDurationFrames = fireDurationSeconds * 60;
    }

    @Override
    public void destroy(Area area) {
        area.isOnFire = true; // first set on fire
        area.isDestroyed = false; // not destroyed yet!
        area.fireTimer = 0;
    }

    @Override
    public void update(Area area) {
        if (area.isOnFire && !area.isDestroyed) {
            area.fireTimer++;
            if (area.fireTimer >= fireDurationFrames) {
                area.isDestroyed = true; // now fully destroyed
                area.isOnFire = false;
            }
        }
    }
}
