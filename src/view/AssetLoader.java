package view;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AssetLoader {
    private final Map<String, BufferedImage> assetCache = new HashMap<>();

    public AssetLoader() {
        loadAssets();
    }

    private void loadAssets() {
        String[] typesToLoad = {
                "ROOM", "CINEMA", "RESTAURANT", "FITNESS",
                "LOBBY", "RECEPTION", "STAIRS", "ELEVATOR-SHAFT", "ELEVATOR"
        };

        for (String type : typesToLoad) {
            try {
                File file = new File("src/view/Picture/" + type.toLowerCase() + ".png");
                if (file.exists()) {
                    assetCache.put(type.toUpperCase(), ImageIO.read(file));
                    System.out.println("Geladen: " + type);
                }
            } catch (IOException e) {
                System.err.println("Fout bij laden: " + type);
            }
        }
    }

    public BufferedImage get(String key) {
        return assetCache.get(key);
    }
}