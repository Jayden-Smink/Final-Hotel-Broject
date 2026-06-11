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

    private final StopwatchTimer stopwatchTimer;
    private final StopwatchDisplay stopwatchDisplay;
    private final JLabel timerLabel;

    public TimeControlPanel(HotelTimeEngine hte, SimulationData data) {
        this.hte = hte;
        this.data = data;

        this.stopwatchTimer = new StopwatchTimer();
        this.stopwatchDisplay = new StopwatchDisplay();

        setLayout(new FlowLayout(FlowLayout.LEFT));

        statusLabel = new JLabel("Hotel Status: Initialiseren...");

        pauseBtn = new JButton("⏸ Pause");
        pauseBtn.addActionListener(e -> {
            hte.togglePause();
            pauseBtn.setText(hte.isPaused() ? "▶ Resume" : "⏸ Pause");
            if (hte.isPaused()) {
                stopwatchTimer.pause();
            } else {
                stopwatchTimer.start();
            }
        });

        JButton slowerBtn = new JButton("🐢 Slower");
        JButton fasterBtn = new JButton("⚡ Faster");

        slowerBtn.addActionListener(e -> {
            hte.setSpeed(hte.getSpeed() - 1);
            stopwatchTimer.setSpeed(hte.getSpeed());
        });

        fasterBtn.addActionListener(e -> {
            hte.setSpeed(hte.getSpeed() + 1);
            stopwatchTimer.setSpeed(hte.getSpeed());
        });

        timerLabel = new JLabel(stopwatchDisplay.zero());
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        timerLabel.setForeground(new Color(0, 180, 100));

        add(statusLabel);
        add(pauseBtn);
        add(slowerBtn);
        add(fasterBtn);
        add(timerLabel);

        stopwatchTimer.start();
    }

    public void refresh() {
        long active = data.guests.values().stream()
                .filter(g -> !g.isCheckingOut)
                .count();
        String pause = hte.isPaused() ? " | ⏸ GEPAUZEERD" : "";
        statusLabel.setText("Gasten: " + active + " | Snelheid: " + hte.getSpeed() + "x" + pause);
        timerLabel.setText("⏱ " + stopwatchDisplay.format(stopwatchTimer.getElapsedMillis()));
    }
}