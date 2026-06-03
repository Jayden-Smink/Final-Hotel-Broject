package view;

import controller.*;
import factory.UIFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class StartScreen extends JFrame {
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JSpinner capSpinner = new JSpinner(new SpinnerNumberModel(50, 1, 500, 5));
    private final StartScreenController controller = new StartScreenController(listModel);

    private int cleaningSeconds = 10;
    private int selectedScenario = 1;

    public StartScreen() {
        setUndecorated(true);
        setSize(620, 700);
        setLocationRelativeTo(null);
        setShape(new RoundRectangle2D.Double(0, 0, 620, 700, 20, 20));

        JPanel root = StartScreenComponents.createBackgroundPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // Afbeelding
        JLabel imageLabel = new JLabel();
        try {
            ImageIcon originalIcon = new ImageIcon("src/view/Picture/hotel.png");
            Image scaledImage = originalIcon.getImage().getScaledInstance(520, 400, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        } catch (Exception e) {
            System.err.println("Kon startscherm afbeelding niet laden: " + e.getMessage());
        }

        // Knoppen configureren
        JButton settingsBtn = UIFactory.createStyledButton("Settings", true);
        JButton startBtn = UIFactory.createStyledButton("Start Simulation", true);
        JButton quitBtn = UIFactory.createStyledButton("Quit", false);

        settingsBtn.addActionListener(e -> {
            SettingsDialog dialog = new SettingsDialog(this, cleaningSeconds, selectedScenario);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                cleaningSeconds = dialog.getCleaningSeconds();
                selectedScenario = dialog.getSelectedScenario();
            }
        });

        startBtn.addActionListener(e -> controller.handleStart(
                this,
                (int) capSpinner.getValue(),
                cleaningSeconds,
                selectedScenario
        ));

        quitBtn.addActionListener(e -> System.exit(0));

        // Alles toevoegen
        root.add(Box.createVerticalStrut(20));
        root.add(imageLabel);
        root.add(Box.createVerticalStrut(25));
        root.add(settingsBtn);
        root.add(Box.createVerticalStrut(10));
        root.add(startBtn);
        root.add(Box.createVerticalStrut(10));
        root.add(quitBtn);

        setContentPane(root);
        setVisible(true);
    }
}