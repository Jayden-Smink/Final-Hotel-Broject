package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CleanerSettingsTest {

    @Test
    void constructor_storesSeconds() {
        CleanerSettings s = new CleanerSettings(30);
        assertEquals(30, s.getCleaningDurationSeconds());
    }

    @Test
    void getCleaningDurationFrames_convertsAt60fps() {
        CleanerSettings s = new CleanerSettings(10);
        assertEquals(600, s.getCleaningDurationFrames());
    }

    @Test
    void getCleaningDurationFrames_zeroSecondsGivesZeroFrames() {
        assertEquals(0, new CleanerSettings(0).getCleaningDurationFrames());
    }

    @Test
    void getCleaningDurationFrames_oneSecondIsSixtyFrames() {
        assertEquals(60, new CleanerSettings(1).getCleaningDurationFrames());
    }

    @Test
    void setCleaningDurationSeconds_updatesValue() {
        CleanerSettings s = new CleanerSettings(10);
        s.setCleaningDurationSeconds(45);
        assertEquals(45, s.getCleaningDurationSeconds());
    }

    @Test
    void setCleaningDurationSeconds_framesUpdateToo() {
        CleanerSettings s = new CleanerSettings(10);
        s.setCleaningDurationSeconds(5);
        assertEquals(300, s.getCleaningDurationFrames());
    }

    @Test
    void constructor_largeValue() {
        CleanerSettings s = new CleanerSettings(3600);
        assertEquals(3600 * 60, s.getCleaningDurationFrames());
    }
}
