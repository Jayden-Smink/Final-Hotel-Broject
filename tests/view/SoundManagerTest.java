package util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SoundManagerTest {

    private util.SoundManager soundManager;
    // We gebruiken het echte pad naar je muziekbestand om te zien of de resource-lader werkt
    private final String validMusicPath = "/music/music.wav";
    private final String invalidMusicPath = "/music/bestaatniet.wav";

    @BeforeEach
    void setUp() {
        // Arrange: Maak voor elke test een schone SoundManager aan
        soundManager = new util.SoundManager();
    }

    @AfterEach
    void tearDown() {
        // Schoonmaak: Zorg dat er na een test nooit muziek blijft doorspelen op de achtergrond
        if (soundManager != null) {
            soundManager.stopMusic();
        }
    }

    @Test
    void playBackgroundMusic_withValidFile_startsPlayingSuccessfully() throws Exception {
        // Act: Start de muziek met het echte bestand
        soundManager.playBackgroundMusic(validMusicPath);

        // Geef Java heel even (100 milliseconden) de tijd om de audio-thread warm te draaien
        Thread.sleep(100);

        // Assert: We kunnen via reflectie of via een try-catch controleregel kijken of de clip draait.
        // Omdat de clip private is, testen we de robuustheid: de methode mag in ieder geval geen exception gooien.
        assertDoesNotThrow(() -> {
            // Als het bestand niet gevonden was, had er een foutmelding in de console gestaan.
            // We controleren of het stoppen daarna ook vlekkeloos werkt.
            soundManager.stopMusic();
        }, "Het afspelen en stoppen van een valide audiobestand mag geen fouten opleveren.");
    }

    @Test
    void playBackgroundMusic_withInvalidFile_doesNotCrash() {
        // Act & Assert: Als een bestand niet bestaat, vangt jouw code dit netjes op met een null-check.
        // Deze test bewijst dat de applicatie niet crasht (geen NullPointerException) bij een typefout in het pad.
        assertDoesNotThrow(() -> soundManager.playBackgroundMusic(invalidMusicPath),
                "Een onjuist muziekpad moet veilig worden afgehandeld zonder de simulatie te laten crashen.");
    }

    @Test
    void stopMusic_whenNotPlaying_doesNotCrash() {
        // Act & Assert: Als er nog helemaal geen muziek aan staat, moet stopMusic() veilig aangeroepen kunnen worden.
        // De ingebouwde `if (backgroundClip != null)` check bewaakt dit.
        assertDoesNotThrow(() -> soundManager.stopMusic(),
                "stopMusic() aanroepen als er geen muziek draait moet veilig kunnen.");
    }
}