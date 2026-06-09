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
            Area area = data.areas.get(i);
            if (!area.AreaType.equalsIgnoreCase("ROOM")) continue;

            String classification = area.classification != null ? area.classification : "-";

            if (area.currentOccupants.isEmpty()) {
                tableModel.addRow(new Object[]{area.id, classification, "Vrij", "-"});
            } else {
                for (int j = 0; j < area.currentOccupants.size(); j++) {
                    int guestId = area.currentOccupants.get(j);
                    Guest guest = data.guests.get(guestId);
                    String status = guest != null ? guest.state.toString() : "Vertrokken";
                    tableModel.addRow(new Object[]{area.id, classification, "Gast " + guestId, status});
                }
            }
        }
    }
}