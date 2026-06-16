package controller;

public class HotelTimeEngine {

    private boolean paused = false;
    private int speed = 1;
    private int hteInterval = 20; // milliseconden tussen events (lager = sneller)

    public void togglePause() { paused = !paused; }
    public boolean isPaused() { return paused; }

    public void setSpeed(int s) { this.speed = Math.max(1, s); }
    public int getSpeed() { return speed; }

    public void setHteInterval(int ms) { this.hteInterval = Math.max(10, ms); }
    public int getHteInterval() { return hteInterval; }
}