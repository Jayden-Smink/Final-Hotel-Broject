package controller;

/**
 * Beheert de tijdsinstellingen van de hotelsimulatie,
 * zoals het pauzeren/hervatten en het aanpassen van de afspeelsnelheid.
 */
public class HotelTimeEngine {

    private boolean paused = false;
    private int speed = 1; // Standaard snelheid (1 update per tick)

    /**
     * Wisselt de huidige pauzestatus om (van pauze naar spelen, en andersom).
     */
    public void togglePause()
    {
        paused = !paused;
    }

    public boolean isPaused()
    {
        return paused;
    }

    /**
     * Stelt de versnelling van de simulatie in (bijv. 2x of 4x versnelling).
     * Math.max(1, s) zorgt ervoor dat de snelheid nooit lager dan 1 (of negatief) kan worden.
     */
    public void setSpeed(int s)
    {
        this.speed = Math.max(1, s);
    }

    public int getSpeed()
    {
        return speed;
    }
}