package view;

import javax.swing.*;
import java.awt.*;

public class SettingsDialog extends JDialog {
    private final JSpinner cleaningSpinner;
    private final JComboBox<String> scenarioComboBox;

    private boolean confirmed = false;

    public SettingsDialog(JFrame parent, int currentCleaningSeconds, int currentScenario) {
        super(parent, "Instellingen", true);

        setSize(350, 230);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Schoonmaaktijd instelling
        cleaningSpinner = new JSpinner(
                new SpinnerNumberModel(currentCleaningSeconds, 1, 300, 1)
        );

        JPanel cleaningPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cleaningPanel.add(new JLabel("Cleaningtime (HTE):"));
        cleaningPanel.add(cleaningSpinner);

        // Scenario instelling
        scenarioComboBox = new JComboBox<>(new String[]{
                "Scenario 1",
                "Scenario 2",
                "Scenario 3",
                "Scenario 4"
        });

        if (currentScenario >= 1 && currentScenario <= 4) {
            scenarioComboBox.setSelectedIndex(currentScenario - 1);
        } else {
            scenarioComboBox.setSelectedIndex(0);
        }

        JPanel scenarioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        scenarioPanel.add(new JLabel("Scenario:"));
        scenarioPanel.add(scenarioComboBox);

        // Opslaan knop
        JButton confirmBtn = new JButton("Opslaan");
        confirmBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        confirmBtn.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        content.add(cleaningPanel);
        content.add(Box.createVerticalStrut(10));
        content.add(scenarioPanel);
        content.add(Box.createVerticalStrut(20));
        content.add(confirmBtn);

        add(content, BorderLayout.CENTER);
    }

    public int getCleaningSeconds() {
        return (int) cleaningSpinner.getValue();
    }

    public int getSelectedScenario() {
        return scenarioComboBox.getSelectedIndex() + 1;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}