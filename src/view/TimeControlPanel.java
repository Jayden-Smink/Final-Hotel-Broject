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
    private final JLabel timerLabel;

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

        JButton slowerBtn  = new JButton("🐢 Slower");
        JButton fasterBtn  = new JButton("⚡ Faster");
        JButton minus50Btn = new JButton("⏪ -50");
        JButton plus50Btn  = new JButton("⏩ +50");

        // Lager interval = sneller events, hoger interval = trager events
        slowerBtn.addActionListener(e  -> hte.setHteInterval(hte.getHteInterval() + 10));
        fasterBtn.addActionListener(e  -> hte.setHteInterval(hte.getHteInterval() - 10));
        minus50Btn.addActionListener(e -> hte.setHteInterval(hte.getHteInterval() + 50));
        plus50Btn.addActionListener(e  -> hte.setHteInterval(hte.getHteInterval() - 50));

        timerLabel = new JLabel("⏱ HTE: 0");
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        timerLabel.setForeground(new Color(0, 180, 100));

        add(statusLabel);
        add(pauseBtn);
        add(slowerBtn);
        add(fasterBtn);
        add(minus50Btn);
        add(plus50Btn);
        add(timerLabel);
    }

    public void refresh() {
        long active = data.guests.values().stream()
                .filter(g -> !g.isCheckingOut)
                .count();
        String pause = hte.isPaused() ? " | ⏸ GEPAUZEERD" : "";
        statusLabel.setText("Gasten: " + active + " | HTE: " + hte.getHteInterval() + "ms" + pause);
        timerLabel.setText("⏱ HTE: " + data.hteTicks);
    }
}