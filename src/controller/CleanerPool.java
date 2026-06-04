package controller;

import model.Cleaner;
import java.util.ArrayList;
import java.util.List;

/**
 * SRP Klasse: Beheert de pool van schoonmakers.
 * Koppelt direct met het Cleaner-model en breidt dit uit naar het gewenste aantal.
 */
public class CleanerPool {
    private final List<Cleaner> workers;
    private static final int DEFAULT_CLEANER_COUNT = 2;

    public CleanerPool() {
        this.workers = new ArrayList<>();
    }

    /**
     * Initialiseert de pool. De eerste cleaner is al via de hoofdstructuur geleverd.
     * De tweede wordt direct vanuit het model aangemaakt via de juiste constructor.
     */
    public void setupWorkers(Cleaner baseCleaner) {
        // Als de pool al gevuld is of de basis-cleaner ontbreekt, doen we niks
        if (baseCleaner == null || !workers.isEmpty()) return;

        // Werknemer 1: De originele schoonmaker uit de data configureren
        baseCleaner.id = 1;
        workers.add(baseCleaner);

        // Werknemer 2: Aanmaken met de verplichte constructor-argumenten (int, double, double)
        for (int i = 2; i <= DEFAULT_CLEANER_COUNT; i++) {

            // GEFIXT: We geven nu netjes (id, x, y) mee zoals jouw model vereist
            Cleaner extraCleaner = new Cleaner(i, baseCleaner.x, baseCleaner.y);

            // Kopieer de overige benodigde eigenschappen over
            extraCleaner.speed = baseCleaner.speed;
            extraCleaner.targetX = baseCleaner.targetX;
            extraCleaner.targetY = baseCleaner.targetY;
            extraCleaner.state = baseCleaner.state;
            extraCleaner.assignedRoomId = -1;

            workers.add(extraCleaner);
        }
    }

    public List<Cleaner> getWorkers() {
        return workers;
    }
}