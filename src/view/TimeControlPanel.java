package view;

import controller.HotelTimeEngine;
import model.SimulationData;

import javax.swing.*;
import java.awt.*;

public class TimeControlPanel extends JPanel {

    private final HotelTimeEngine hte;
    private final SimulationData data;
    private final JLabel statusLabel;
    private final JButton pauseBtn;

    public TimeControlPanel(HotelTimeEngine hte, SimulationData data) {
        this.hte = hte;
        this.data = data;

        setLayout(new FlowLayout(FlowLayout.LEFT));

        statusLabel = new JLabel("Hotel Status: Initialiseren...");

        pauseBtn = new JButton("⏸ Pause");
        pauseBtn.addActionListener(e -> {
            hte.togglePause();
            pauseBtn.setText(hte.isPaused() ? "▶ Resume" : "⏸ Pause");
        });

        JButton slowerBtn = new JButton("🐢 Slower");
        JButton fasterBtn = new JButton("⚡ Faster");

        slowerBtn.addActionListener(e -> hte.setSpeed(hte.getSpeed() - 1));
        fasterBtn.addActionListener(e -> hte.setSpeed(hte.getSpeed() + 1));

        add(statusLabel);
        add(pauseBtn);
        add(slowerBtn);
        add(fasterBtn);
    }

    // Called by GameLoop's onTick to refresh the label
    public void refresh() {
        long active = data.guests.values().stream()
                .filter(g -> !g.isCheckingOut)
                .count();
        String pause = hte.isPaused() ? " | ⏸ GEPAUZEERD" : "";
        statusLabel.setText("Gasten: " + active + " | Snelheid: " + hte.getSpeed() + "x" + pause);
    }
}