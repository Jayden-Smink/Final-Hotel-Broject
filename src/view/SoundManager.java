package view;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class SoundManager {

    private Clip backgroundClip;

    /**
     * Start de achtergrondmuziek en herhaalt deze oneindig.
     * @param resourcePath Het pad naar het .wav bestand vanaf de resources map (bijv. "/music/background.wav")
     */
    public void playBackgroundMusic(String resourcePath) {
        try {
            // Laad het bestand via de ClassLoader (werkt ook als je er later een JAR van maakt)
            InputStream audioSrc = getClass().getResourceAsStream(resourcePath);
            if (audioSrc == null) {
                System.err.println("Muziekbestand niet gevonden op pad: " + resourcePath);
                return;
            }

            // Buffer de inputstream om ondersteuning voor mark/reset te garanderen
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);

            // Haal de audio-clip lijn op
            backgroundClip = AudioSystem.getClip();
            backgroundClip.open(audioStream);

            // Loop de muziek oneindig
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundClip.start();

            System.out.println("🎵 Achtergrondmuziek succesvol gestart!");
        } catch (Exception e) {
            System.err.println("Fout bij het afspelen van muziek: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Stopt de muziek netjes (handig voor als de simulatie stopt of sluit)
     */
    public void stopMusic() {
        if (backgroundClip != null && backgroundClip.isRunning()) {
            backgroundClip.stop();
            backgroundClip.close();
        }
    }
}