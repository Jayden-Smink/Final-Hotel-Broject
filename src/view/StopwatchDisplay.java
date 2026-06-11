package view;

/**
 * Verantwoordelijk voor het omzetten van milliseconden naar een leesbare tijdstring.
 * Geen UI, geen tijdlogica.
 */
public class StopwatchDisplay {

    /**
     * Zet milliseconden om naar "HH:MM:SS.t" formaat.
     * Bijvoorbeeld: 3723400 → "01:02:03.4"
     */
    public String format(long millis) {
        long tenths  = (millis / 100) % 10;
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / 60000) % 60;
        long hours   = millis / 3600000;
        return String.format("%02d:%02d:%02d.%d", hours, minutes, seconds, tenths);
    }

    public String zero() {
        return "00:00:00.0";
    }
}