package com.mudassir.railbook.ui;

import com.mudassir.railbook.model.Reservation;
import com.mudassir.railbook.model.User;
import com.mudassir.railbook.service.ReservationService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panel displaying all active reservations booked by the current user in a JTable.
 * Supports refreshing, exporting selected tickets to file, and navigating to cancellation.
 *
 * @author Mudassir Ahmed
 */
public class BookingsPanel extends JPanel {

    private final ReservationService reservationService;
    private final User currentUser;
    private final Consumer<String> onSelectTicketForCancellation;

    private JTable bookingsTable;
    private DefaultTableModel tableModel;
    private List<Reservation> currentReservations = new ArrayList<>();
    private JLabel summaryLabel;

    public BookingsPanel(ReservationService reservationService, User currentUser,
                         Consumer<String> onSelectTicketForCancellation) {
        this.reservationService = reservationService;
        this.currentUser = currentUser;
        this.onSelectTicketForCancellation = onSelectTicketForCancellation;

        initComponents();
        loadBookings();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Top Toolbar
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);

        JPanel titlePanel = new JPanel(new BorderLayout(0, 4));
        titlePanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("My Active Bookings");
        titleLabel.setFont(ThemeManager.FONT_HEADER);
        titleLabel.setForeground(ThemeManager.COLOR_PRIMARY);

        summaryLabel = new JLabel("Loading your bookings...");
        summaryLabel.setFont(ThemeManager.FONT_SMALL);
        summaryLabel.setForeground(ThemeManager.COLOR_TEXT_MUTED);

        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(summaryLabel, BorderLayout.SOUTH);
        topPanel.add(titlePanel, BorderLayout.WEST);

        // Buttons
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonBar.setBackground(Color.WHITE);

        JButton refreshBtn = new JButton("Refresh");
        ThemeManager.styleSecondaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> loadBookings());

        JButton exportBtn = new JButton("Save Selected Ticket (.txt)");
        ThemeManager.styleSecondaryButton(exportBtn);
        exportBtn.addActionListener(e -> exportSelectedTicket());

        JButton cancelSelectedBtn = new JButton("Cancel Selected Ticket");
        ThemeManager.styleDangerButton(cancelSelectedBtn);
        cancelSelectedBtn.addActionListener(e -> cancelSelectedTicket());

        buttonBar.add(refreshBtn);
        buttonBar.add(exportBtn);
        buttonBar.add(cancelSelectedBtn);

        topPanel.add(buttonBar, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Table setup
        String[] columns = {
                "PNR",
                "Passenger Name",
                "Train",
                "Class",
                "Journey Date",
                "Origin",
                "Destination",
                "Booked At"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only table
            }
        };

        bookingsTable = new JTable(tableModel);
        bookingsTable.setRowHeight(32);
        bookingsTable.setFont(ThemeManager.FONT_REGULAR);
        bookingsTable.getTableHeader().setFont(ThemeManager.FONT_BOLD);
        bookingsTable.getTableHeader().setBackground(new Color(241, 245, 249));
        bookingsTable.getTableHeader().setForeground(ThemeManager.COLOR_TEXT_DARK);
        bookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingsTable.setShowGrid(true);
        bookingsTable.setGridColor(new Color(226, 232, 240));

        // Center align PNR, Date, Class
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        bookingsTable.getColumnModel().getColumn(0).setCellRenderer(new PnrCellRenderer());
        bookingsTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        bookingsTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        bookingsTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(bookingsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER, 1));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadBookings() {
        summaryLabel.setText("Refreshing active reservations...");
        SwingWorker<List<Reservation>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Reservation> doInBackground() {
                return reservationService.getUserBookings(currentUser.getId());
            }

            @Override
            protected void done() {
                try {
                    currentReservations = get();
                    tableModel.setRowCount(0);
                    for (Reservation res : currentReservations) {
                        String trainDisplay = res.getTrainNumber() + " - " +
                                (res.getTrainName() != null ? res.getTrainName() : "N/A");
                        tableModel.addRow(new Object[]{
                                res.getPnr(),
                                res.getPassengerName(),
                                trainDisplay,
                                res.getClassType(),
                                res.getJourneyDate(),
                                res.getSource(),
                                res.getDestination(),
                                res.getBookedAt()
                        });
                    }
                    summaryLabel.setText("Showing " + currentReservations.size() + " active ticket(s) for user: " + currentUser.getUsername());
                } catch (Exception ex) {
                    summaryLabel.setText("Failed to load reservations.");
                }
            }
        };
        worker.execute();
    }

    private void exportSelectedTicket() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentReservations.size()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a reservation row from the table first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Reservation res = currentReservations.get(selectedRow);
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Ticket to File");
        fileChooser.setSelectedFile(new File("Ticket_" + res.getPnr() + ".txt"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));

        int choice = fileChooser.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }
            try {
                reservationService.exportTicketToFile(res, file);
                JOptionPane.showMessageDialog(this,
                        "Ticket saved successfully to:\n" + file.getAbsolutePath(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error saving ticket: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cancelSelectedTicket() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentReservations.size()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a reservation row to cancel.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Reservation res = currentReservations.get(selectedRow);
        if (onSelectTicketForCancellation != null) {
            onSelectTicketForCancellation.accept(res.getPnr());
        }
    }

    /**
     * Custom renderer to highlight PNR with monospace font.
     */
    private static class PnrCellRenderer extends DefaultTableCellRenderer {
        private static final Font PNR_FONT = new Font("Consolas", Font.BOLD, 13);

        public PnrCellRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setFont(PNR_FONT);
            if (!isSelected) {
                c.setForeground(ThemeManager.COLOR_PRIMARY);
            }
            return c;
        }
    }
}
