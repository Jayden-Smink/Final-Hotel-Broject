package controller;

import java.io.File;
import java.nio.file.Files;

/**
 * Verantwoordelijkheid: Bestanden van schijf lezen.
 * Weet niets over het formaat, de inhoud, of wat er daarna mee moet gebeuren.
 */
public class LayoutFileReader {

    /**
     * Leest de volledige inhoud van een bestand als String.
     * Zoekt eerst in de src-map, daarna in de root.
     *
     * @throws IllegalArgumentException als het bestand niet gevonden kan worden.
     */
    public String read(String fileName) {
        try {
            File file = new File("src/" + fileName);
            if (!file.exists()) file = new File(fileName);
            if (!file.exists()) {
                throw new IllegalArgumentException("Bestand niet gevonden: " + fileName);
            }
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Fout bij lezen van bestand: " + fileName, e);
        }
    }
}