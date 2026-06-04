package model;

public class FacilitySettings {
    private int cinemaDurationSeconds;
    private int restaurantDurationSeconds;
    private int fitnessDurationSeconds;

    public FacilitySettings(int cinemaDurationSeconds, int restaurantDurationSeconds, int fitnessDurationSeconds) {
        this.cinemaDurationSeconds = cinemaDurationSeconds;
        this.restaurantDurationSeconds = restaurantDurationSeconds;
        this.fitnessDurationSeconds = fitnessDurationSeconds;
    }

    public int getCinemaDurationFrames() {
        return cinemaDurationSeconds * 60;
    }

    public int getRestaurantDurationFrames() {
        return restaurantDurationSeconds * 60;
    }

    public int getFitnessDurationFrames() {
        return fitnessDurationSeconds * 60;
    }

    // Getters and setters
    public int getCinemaDurationSeconds() { return cinemaDurationSeconds; }
    public void setCinemaDurationSeconds(int s) { this.cinemaDurationSeconds = s; }

    public int getRestaurantDurationSeconds() { return restaurantDurationSeconds; }
    public void setRestaurantDurationSeconds(int s) { this.restaurantDurationSeconds = s; }

    public int getFitnessDurationSeconds() { return fitnessDurationSeconds; }
    public void setFitnessDurationSeconds(int s) { this.fitnessDurationSeconds = s; }
}