package view;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AssetLoader {
    private final Map<String, BufferedImage> assetCache = new HashMap<>();

    // Asset key suffixes for the three destruction states
    public static final String NORMAL  = "";
    public static final String BURNED  = "-BURNED";
    public static final String BROKEN  = "-BROKEN";

    public AssetLoader() {
        loadAssets();
    }

    private void loadAssets() {
        // Base types → filename stem (lowercase)
        String[][] types = {
            {"ROOM",          "room"},
            {"ROOM3STAR",     "room3star"},
            {"ROOM4STAR",     "room4star"},
            {"CINEMA",        "cinema"},
            {"RESTAURANT",    "restaurant"},
            {"FITNESS",       "fitness"},
            {"LOBBY",         "lobby"},
            {"RECEPTION",     "reception"},
            {"STAIRS",        "stairs"},
            {"ELEVATOR-SHAFT","elevator-shaft"},
            {"ELEVATOR",      "elevator"},
        };

        String[] suffixes = {"", "-burned", "-broken"};
        String[] cacheKeys = {NORMAL, BURNED, BROKEN};

        for (String[] type : types) {
            String cacheBase = type[0];
            String fileBase  = type[1];

            for (int i = 0; i < suffixes.length; i++) {
                String filename = "src/view/Picture/" + fileBase + suffixes[i] + ".png";
                String cacheKey = cacheBase + cacheKeys[i];
                try {
                    File file = new File(filename);
                    if (file.exists()) {
                        assetCache.put(cacheKey.toUpperCase(), ImageIO.read(file));
                        System.out.println("Geladen: " + cacheKey);
                    }
                } catch (IOException e) {
                    System.err.println("Fout bij laden: " + filename);
                }
            }
        }

        // Dead person images
        loadImage("DEAD-GUEST",   "src/view/Picture/dead-guest.png");
        loadImage("DEAD-CLEANER", "src/view/Picture/dead-cleaner.png");
    }

    private void loadImage(String key, String path) {
        try {
            File f = new File(path);
            if (f.exists()) {
                assetCache.put(key, ImageIO.read(f));
                System.out.println("Geladen: " + key);
            }
        } catch (IOException e) {
            System.err.println("Fout bij laden: " + path);
        }
    }

    public BufferedImage get(String key) {
        return assetCache.get(key == null ? null : key.toUpperCase());
    }

    /** Returns the correct image for an area based on its destruction state. */
    public BufferedImage getForArea(String baseKey) {
        return get(baseKey);
    }

    public BufferedImage getDestroyed(String baseKey) {
        BufferedImage img = get(baseKey + BROKEN);
        return img != null ? img : get(baseKey); // fallback to normal if no broken image
    }

    public BufferedImage getBurning(String baseKey) {
        BufferedImage img = get(baseKey + BURNED);
        return img != null ? img : get(baseKey); // fallback to normal if no burned image
    }
}
