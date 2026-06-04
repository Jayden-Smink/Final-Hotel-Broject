package model;

public class GuestSettings {
    private final int elevatorWaitTimeout;

    public GuestSettings(int elevatorWaitSeconds) {
        this.elevatorWaitTimeout = elevatorWaitSeconds * 60; // seconden naar frames
    }

    public int getElevatorWaitTimeout() {
        return elevatorWaitTimeout;
    }
}