package view;

import javax.swing.*;
import java.awt.*;

public class SettingsDialog extends JDialog {
    private final JSpinner cleaningSpinner;
    private boolean confirmed = false;

    public SettingsDialog(JFrame parent, int currentCleaningSeconds) {
        super(parent, "Instellingen", true);
        setSize(300, 200);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Cleaning time setting
        cleaningSpinner = new JSpinner(new SpinnerNumberModel(currentCleaningSeconds, 1, 300, 1));
        JPanel cleaningPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cleaningPanel.add(new JLabel("Schoonmaaktijd (seconden):"));
        cleaningPanel.add(cleaningSpinner);

        // Add more settings here later!

        JButton confirmBtn = new JButton("Opslaan");
        confirmBtn.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        content.add(cleaningPanel);
        content.add(Box.createVerticalStrut(20));
        content.add(confirmBtn);

        add(content, BorderLayout.CENTER);
    }

    public int getCleaningSeconds() {
        return (int) cleaningSpinner.getValue();
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}