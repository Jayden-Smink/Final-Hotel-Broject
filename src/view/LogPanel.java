package view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LogPanel extends JPanel {
    private final JTextArea logArea;
    private final JScrollPane scrollPane;

    public LogPanel() {
        setLayout(new BorderLayout());
        logArea = new JTextArea(8, 30);
        logArea.setEditable(false);
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(new Color(0, 255, 0)); // Matrix groen
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(new TitledBorder("Hotel Events Log"));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void addLog(String message) {
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.append("[" + timestamp + "] " + message + "\n");
        // Automatisch naar beneden scrollen
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}