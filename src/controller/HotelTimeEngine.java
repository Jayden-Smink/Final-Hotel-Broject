package controller;

public class HotelTimeEngine {

    private boolean paused = false;
    private int speed = 1;

    public void togglePause()
    {
        paused = !paused;
    }
    public boolean isPaused()
    {
        return paused;
    }

    public void setSpeed(int s)
    {
        this.speed = Math.max(1, s);
    }
    public int  getSpeed()
    {
        return speed;
    }
}