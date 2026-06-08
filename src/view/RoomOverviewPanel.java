package view;

import model.Area;
import model.Guest;
import model.SimulationData;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RoomOverviewPanel extends JPanel {

    private final SimulationData data;
    private final DefaultTableModel tableModel;

    public RoomOverviewPanel(SimulationData data) {
        this.data = data;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(400, 300));

        String[] columns = {"Kamer", "Classificatie", "Gast ID", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Ververs de tabel elke 500ms terwijl het venster open is
        new Timer(500, e -> refresh()).start();

        refresh();
    }

    public void refresh() {
        tableModel.setRowCount(0);

        for (int i = 0; i < data.areas.size(); i++) {
            Area a = data.areas.get(i);
            if (!a.AreaType.equalsIgnoreCase("ROOM")) continue;

            String classification = a.classification != null ? a.classification : "-";

            if (a.currentOccupants.isEmpty()) {
                tableModel.addRow(new Object[]{a.id, classification, "Vrij", "-"});
            } else {
                for (int j = 0; j < a.currentOccupants.size(); j++) {
                    int guestId = a.currentOccupants.get(j);
                    Guest g = data.guests.get(guestId);
                    String status = g != null ? g.state.toString() : "Vertrokken";
                    tableModel.addRow(new Object[]{a.id, classification, "Gast " + guestId, status});
                }
            }
        }
    }
}