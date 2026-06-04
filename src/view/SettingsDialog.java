package view;

import javax.swing.*;
import java.awt.*;

public class SettingsDialog extends JDialog {
    private final JSpinner cleaningSpinner;
    private final JSpinner cinemaSpinner;
    private final JSpinner restaurantSpinner;
    private final JSpinner fitnessSpinner;
    private final JComboBox<String> scenarioComboBox;
    private boolean confirmed = false;

    public SettingsDialog(JFrame parent, int currentCleaningSeconds, int currentScenario) {
        super(parent, "Instellingen", true);

        setSize(350, 380);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Schoonmaaktijd
        cleaningSpinner = new JSpinner(new SpinnerNumberModel(currentCleaningSeconds, 1, 300, 1));
        content.add(createRow("Schoonmaaktijd (HTE):", cleaningSpinner));
        content.add(Box.createVerticalStrut(10));

        // Cinema duur
        cinemaSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 300, 1));
        content.add(createRow("Filmduur cinema (HTE):", cinemaSpinner));
        content.add(Box.createVerticalStrut(10));

        // Restaurant duur
        restaurantSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 300, 1));
        content.add(createRow("Restaurantduur (HTE):", restaurantSpinner));
        content.add(Box.createVerticalStrut(10));

        // Fitness duur
        fitnessSpinner = new JSpinner(new SpinnerNumberModel(15, 1, 300, 1));
        content.add(createRow("Fitnessduur (HTE):", fitnessSpinner));
        content.add(Box.createVerticalStrut(10));

        // Scenario
        scenarioComboBox = new JComboBox<>(new String[]{"Scenario 1", "Scenario 2", "Scenario 3", "Scenario 4"});
        if (currentScenario >= 1 && currentScenario <= 4) scenarioComboBox.setSelectedIndex(currentScenario - 1);
        content.add(createRow("Scenario:", scenarioComboBox));
        content.add(Box.createVerticalStrut(20));

        // Opslaan knop
        JButton confirmBtn = new JButton("Opslaan");
        confirmBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        confirmBtn.addActionListener(e -> { confirmed = true; dispose(); });
        content.add(confirmBtn);

        add(content, BorderLayout.CENTER);
    }

    private JPanel createRow(String label, JComponent component) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel(label));
        panel.add(component);
        return panel;
    }

    public int getCleaningSeconds() { return (int) cleaningSpinner.getValue(); }
    public int getCinemaDurationSeconds() { return (int) cinemaSpinner.getValue(); }
    public int getRestaurantDurationSeconds() { return (int) restaurantSpinner.getValue(); }
    public int getFitnessDurationSeconds() { return (int) fitnessSpinner.getValue(); }
    public int getSelectedScenario() { return scenarioComboBox.getSelectedIndex() + 1; }
    public boolean isConfirmed() { return confirmed; }
}