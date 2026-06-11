package view;

public class StopwatchTimer {

    private long startTime;
    private long elapsedBeforePause;
    private boolean running;
    private int speed;

    public StopwatchTimer() {
        this.elapsedBeforePause = 0;
        this.running = false;
        this.speed = 1;
    }

    public void start() {
        if (running) return;
        startTime = System.currentTimeMillis();
        running = true;
    }

    public void pause() {
        if (!running) return;
        elapsedBeforePause += (System.currentTimeMillis() - startTime) * speed;
        running = false;
    }

    public void setSpeed(int speed) {
        if (running) {
            // Sla huidige elapsed op met oude snelheid voor we wisselen
            elapsedBeforePause += (System.currentTimeMillis() - startTime) * this.speed;
            startTime = System.currentTimeMillis();
        }
        this.speed = Math.max(1, speed);
    }

    public long getElapsedMillis() {
        if (running) return elapsedBeforePause + (System.currentTimeMillis() - startTime) * speed;
        return elapsedBeforePause;
    }
}