package view;

import controller.CleanerController; // NIEUWE IMPORT
import model.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import model.CleanerState;

/**
 * DEZE KLASSE: SimulationRenderer
 * Verantwoordelijk voor het visueel uittekenen (renderen) van de hele hotelsimulatie.
 * Het vertaalt de logica en data (Model) naar plaatjes en kleuren op het scherm (View).
 */
public class SimulationRenderer {
    // BEHEERDERS VAN DE DATA:
    // data: Bevat alle informatie over de kamers, de lift en de gasten.
    private final SimulationData data;
    // cleanerController: Geeft toegang tot de logica en data van de schoonmakers (de pool).
    private final CleanerController cleanerController; // KOPPELING: Nu hebben we toegang tot de pool!

    // OPSLAG: Bewaart ingeladen afbeeldingen in het geheugen zodat we ze niet elke frame opnieuw van de harde schijf hoeven te lezen.
    private final Map<String, BufferedImage> assetCache = new HashMap<>();

    // CONFIGURATIE: Area types that get the occupied overlay and guest count badge
    // Bepaalt welke type kamers interactief zijn en dus een gastenteller en rode gloed krijgen als ze bezet zijn.
    private static final Set<String> ACTIVITY_AREAS = Set.of(
            "ROOM", "RESTAURANT", "CINEMA", "FITNESS"
    );

    /*
     * CONFIGURATIE: Deze areas worden NA de elevator getekend.
     * Daardoor liggen ze visueel bovenop de elevator (zodat de lift achter de lobby/receptie lijkt te verdwijnen).
     */
    private static final Set<String> FRONT_LAYER_AREAS = Set.of(
            "LOBBY", "RECEPTION"
    );

    // DE BOUWER (Constructor): Wordt aangeroepen bij de opstart om de Renderer klaar te maken.
    // AANPASSING: De constructor verwacht nu ook de cleanerController
    public SimulationRenderer(SimulationData data, CleanerController cleanerController) {
        loadAssets(); // Zorgt dat de plaatjes direct in het geheugen staan
        this.data = data;
        this.cleanerController = cleanerController;
    }

    // DE LADER: Verantwoordelijk voor het inlezen van .png bestanden uit de mappenstructuur.
    private void loadAssets() {
        String[] typesToLoad = {
                "ROOM", "CINEMA", "RESTAURANT", "FITNESS",
                "LOBBY", "RECEPTION", "STAIRS", "ELEVATOR-SHAFT", "ELEVATOR",
                "BACKROOMS"
        };

        for (int i = 0; i < typesToLoad.length; i++) {
            String type = typesToLoad[i];
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

    // DE HOOFDREGISSEUR (Main Render Loop): Bepaalt de volgorde waarin alles over elkaar heen getekend wordt.
    // Wordt continu aangeroepen om het scherm te updaten.
    public void render(Graphics2D g2, SimulationData data) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Achtergrondkleur van de hele applicatie tekenen
        g2.setColor(new Color(30, 30, 30));
        g2.fillRect(0, 0, 2000, 2000);

        // 1. Teken alle areas behalve LOBBY en RECEPTION (De basis van het hotel)
        drawAreas(g2, data, false);

        // 2. Teken elevator (Wordt over de basis getekend)
        drawElevator(g2, data);

        // 3. Teken LOBBY en RECEPTION bovenop de elevator (Zodat de lift hier 'achter' valt)
        drawAreas(g2, data, true);

        // 4. Teken gasten (Poppetjes die door het hotel bewegen)
        drawGuests(g2, data);

        // 5. Teken schoonmakers (NU MEERVOUD) (Schoonmakers worden als laatste getekend zodat ze altijd zichtbaar zijn)
        drawCleaners(g2);
    }

    // DE KAMER-VERDELER: Loopt door de lijst met alle kamers en bepaalt op basis van 'frontLayerOnly' of
    // de kamer op dit moment aan de beurt is om getekend te worden.
    private void drawAreas(Graphics2D g2, SimulationData data, boolean frontLayerOnly) {
        if (data.areas == null) return;

        for (int i = 0; i < data.areas.size(); i++) {
            Area area = data.areas.get(i);
            boolean isFrontLayer = FRONT_LAYER_AREAS.contains(area.AreaType.toUpperCase());

            // Check of deze kamer in de huidige teken-laag thuishoort
            if (frontLayerOnly == isFrontLayer) {
                int count = countGuests(area, data.guests);
                drawArea(g2, area, count); // Roept de specifieke tekenaar aan voor deze kamer
            }
        }
    }

    // DE LIFT-TEKENAAR: Berekent waar de lift is en tekent het lift-plaatje (of een blauw blokje als fallback).
    private void drawElevator(Graphics2D g2, SimulationData data) {
        if (data.elevator == null) return;

        BufferedImage elevatorImg = assetCache.get("ELEVATOR");
        int elevatorWidth = 46;
        int elevatorX = data.horizontalOffset + (data.tileSize / 2) - (elevatorWidth / 2);
        int curY = (int) data.elevator.curY;

        if (elevatorImg != null) {
            g2.drawImage(elevatorImg, elevatorX, curY, elevatorWidth, data.tileSize - 10, null);
        } else {
            g2.setColor(new Color(60, 120, 255));
            g2.fillRoundRect(elevatorX, curY, elevatorWidth, data.tileSize - 10, 10, 10);
        }
    }

    // DE GASTEN-TEKENAAR: Haalt alle gasten op, pakt er een veilige kopie van (synchronized) en
    // vraagt de 'GuestRenderer' om elke individuele gast op het scherm te zetten (behalve als ze in de lift staan).
    private void drawGuests(Graphics2D g2, SimulationData data) {
        if (data.guests == null) return;

        List<Guest> guestSnapshot;
        synchronized (data.guests) {
            guestSnapshot = new ArrayList<>(data.guests.values());
        }

        for (int i = 0; i < guestSnapshot.size(); i++) {
            Guest guest = guestSnapshot.get(i);
            if (guest.state != GuestState.IN_LIFT) {
                GuestRenderer.draw(g2, guest, data.horizontalOffset);
            }
        }
    }

    // DE SCHOONMAKER-TEKENAAR: Haalt de actieve schoonmakers op uit de CleanerController.
    // Tekent ze als groene stipjes met een naampje (bijv. "C1") die door het gebouw bewegen.
    // GEFIXT: Loopt nu door alle schoonmakers uit de pool heen!
    private void drawCleaners(Graphics2D g2) {
        if (cleanerController == null) return;

        List<Cleaner> cleaners = cleanerController.getActiveCleaners();
        for (int i = 0; i < cleaners.size(); i++) {
            Cleaner cleaner = cleaners.get(i);
            // Als de schoonmaker in een kamer aan het poetsen is, verbergen we de stip (de kamer wordt immers al groen)
            if (cleaner.state == CleanerState.CLEANING) continue;

            int drawX = (int) cleaner.x + data.horizontalOffset;
            int drawY = (int) cleaner.y;

            // Teken de groene stip voor de schoonmaker
            g2.setColor(new Color(50, 205, 50));
            g2.fillOval(drawX - 10, drawY - 10, 20, 20);

            // Teken "C1", "C2", etc. boven de stip zodat je ziet wie wie is
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            g2.drawString("C" + cleaner.id, drawX - 6, drawY - 12);
        }
    }

    /**
     * DE GASTEN-TELLER:
     * Telt het aantal gasten (IDLE) binnen de ruimtelijke grenzen (X en Y coördinaten) van een specifieke kamer.
     */
    private int countGuests(Area area, Map<Integer, Guest> guests) {
        if (guests == null) return 0;

        int areaX = area.getPos()[0] * data.tileSize;
        int areaY = area.getPos()[1] * data.tileSize;
        int areaW = area.getDim()[0] * data.tileSize;
        int areaH = area.getDim()[1] * data.tileSize;

        return (int) guests.values().stream().filter(guest ->
                guest.state == GuestState.IDLE &&
                        guest.x >= areaX - 10 &&
                        guest.x <= areaX + areaW + 10 &&
                        guest.y >= areaY &&
                        guest.y <= areaY + areaH
        ).count();
    }

    // DE KAMER-DETAILS TEKENAAR: Verantwoordelijk voor het effectief uittekenen van één enkele kamer.
    // Hij tekent het plaatje (of kleur), de rode gloed (als de kamer bezet is), en de groene gloed (als er gepoetst wordt).
    private void drawArea(Graphics2D g2, Area area, int guestCount) {
        int[] pos = area.getPos();
        int[] dim = area.getDim();

        int x = (pos[0] * data.tileSize) + data.horizontalOffset;
        int y = pos[1] * data.tileSize;
        int w = dim[0] * data.tileSize;
        int h = dim[1] * data.tileSize;

        String assetKey = area.AreaType.toUpperCase();

        if (assetKey.contains("SCHACHT")) assetKey = "ELEVATOR-SHAFT";
        if (assetKey.contains("TRAP")) assetKey = "STAIRS";

        BufferedImage img = assetCache.get(assetKey);

        /*
         * Als LOBBY of RECEPTION transparante pixels heeft,
         * kan de elevator anders nog zichtbaar blijven.
         * Daarom tekenen we eerst een donkere basislaag.
         */
        if (FRONT_LAYER_AREAS.contains(area.AreaType.toUpperCase())) {
            g2.setColor(new Color(30, 30, 30));
            g2.fillRect(x, y, w, h);
        }

        // Teken het echte plaatje van de kamer
        if (img != null) {
            g2.drawImage(img, x, y, w, h, null);
        } else {
            // Als er geen plaatje is, gebruiken we reservekleuren
            if (assetKey.equals("RECEPTION")) {
                g2.setColor(new Color(255, 218, 170));
            } else if (assetKey.equals("LOBBY")) {
                g2.setColor(new Color(45, 45, 45));
            } else if (assetKey.equals("BACKROOMS")) {
                g2.setColor(new Color(75, 70, 45));
            } else {
                g2.setColor(Color.DARK_GRAY);
            }

            g2.fillRect(x, y, w, h);
        }

        // DE BEZET-INDICATOR: Tekent een rode transparante overlay als er gasten in een activiteitenruimte zijn.
        if (guestCount > 0 && ACTIVITY_AREAS.contains(area.AreaType.toUpperCase())) {
            Composite original = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
            g2.setColor(new Color(220, 50, 50));
            g2.fillRect(x, y, w, h);
            g2.setComposite(original);
        }

        // NAAMLABEL: Zet de naam van de kamer linksboven in de hoek.
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.setColor(new Color(255, 255, 255, 120));
        g2.drawString(area.AreaType, x + 5, y + 15);

        // TELLERTJE: Laat zien hoeveel gasten er binnen zijn.
        if (ACTIVITY_AREAS.contains(area.AreaType.toUpperCase())) {
            drawGuestCountBadge(g2, x, y, w, guestCount);
        }

        // DE SCHOONMAAK-INDICATOR: Vraagt aan de controller of er momenteel schoonmakers zijn in DEZE kamer.
        // GEFIXT: Groene overlay — Kamer wordt nu groen als EEN VAN DE schoonmakers hier poetst
        if (cleanerController != null) {
            List<Cleaner> activeCleaners = cleanerController.getActiveCleaners();
            for (int i = 0; i < activeCleaners.size(); i++) {
                Cleaner cleaner = activeCleaners.get(i);
                if (cleaner.assignedRoomId == area.id && cleaner.state == CleanerState.CLEANING) {
                    Composite original = g2.getComposite();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
                    g2.setColor(new Color(50, 220, 50));
                    g2.fillRect(x, y, w, h);
                    g2.setComposite(original);
                    break; // Eén groene overlay is genoeg als er gepoetst wordt
                }
            }
        }
    }

    /**
     * DE BADGE-TEKENAAR:
     * Tekent een klein badge-cirkel met het aantal gasten in de rechterbovenhoek van de area.
     * Dit zorgt voor een nette visuele weergave in plaats van alleen een los getal.
     */
    private void drawGuestCountBadge(Graphics2D g2, int x, int y, int w, int guestCount) {
        String text = String.valueOf(guestCount);

        int badgeSize = 18;
        int badgeX = x + w - badgeSize - 4;
        int badgeY = y + 4;

        // Achtergrond: donker met lichte rand
        g2.setColor(new Color(20, 20, 20, 200));
        g2.fillOval(badgeX, badgeY, badgeSize, badgeSize);

        g2.setColor(new Color(255, 255, 255, 160));
        g2.setStroke(new BasicStroke(1f));
        g2.drawOval(badgeX, badgeY, badgeSize, badgeSize);

        // Getal mooi in het midden van de badge centreren
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();

        int textX = badgeX + (badgeSize - fm.stringWidth(text)) / 2;
        int textY = badgeY + (badgeSize - fm.getHeight()) / 2 + fm.getAscent();

        g2.setColor(Color.WHITE);
        g2.drawString(text, textX, textY);
    }
}