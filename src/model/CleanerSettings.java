package model;

public class CleanerSettings {
    private int cleaningDurationSeconds;

    public CleanerSettings(int cleaningDurationSeconds) {
        this.cleaningDurationSeconds = cleaningDurationSeconds;
    }

    public int getCleaningDurationFrames() {
        return cleaningDurationSeconds * 60; // convert to frames at 60fps
    }

    public int getCleaningDurationSeconds() {
        return cleaningDurationSeconds;
    }

    public void setCleaningDurationSeconds(int seconds) {
        this.cleaningDurationSeconds = seconds;
    }
}