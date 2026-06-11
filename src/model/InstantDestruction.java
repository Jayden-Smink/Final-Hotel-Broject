package model;

public class InstantDestruction implements IDestructionStrategy {
    @Override
    public void destroy(Area area) {
        area.isDestroyed = true;
        area.isOnFire = false;
    }

    @Override
    public void update(Area area) {
        // er is niks voor een update want is INSTANTLY VERNIETIGD
    }
}

