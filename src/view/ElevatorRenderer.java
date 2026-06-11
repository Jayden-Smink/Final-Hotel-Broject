package view;

import model.SimulationData;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ElevatorRenderer {
    private final AssetLoader assetLoader;

    public ElevatorRenderer(AssetLoader assetLoader) {
        this.assetLoader = assetLoader;
    }

    public void drawElevator(Graphics2D g2, SimulationData data) {
        if (data.elevator == null) return;

        BufferedImage elevatorImg = assetLoader.get("ELEVATOR");
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
}