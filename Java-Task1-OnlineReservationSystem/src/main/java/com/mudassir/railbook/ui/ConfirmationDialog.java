package com.mudassir.railbook.ui;

import com.mudassir.railbook.model.Reservation;
import com.mudassir.railbook.service.ReservationService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;

/**
 * Modal confirmation dialog presented upon a successful ticket booking.
 * Displays all reservation details, provides one-click PNR copying,
 * and allows exporting the ticket as a formatted text file.
 *
 * @author Mudassir Ahmed
 */
public class ConfirmationDialog extends JDialog {

    private final Reservation reservation;
    private final ReservationService reservationService;

    public ConfirmationDialog(JFrame parent, Reservation reservation, ReservationService reservationService) {
        super(parent, "Booking Confirmed - RailBook", true);
        this.reservation = reservation;
        this.reservationService = reservationService;

        initComponents();
    }

    private void initComponents() {
        setSize(560, 520);
        setMinimumSize(new Dimension(500, 480));
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header Success Banner
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);

        JLabel successTitle = new JLabel("Ticket Booked Successfully!", SwingConstants.CENTER);
        successTitle.setFont(ThemeManager.FONT_TITLE);
        successTitle.setForeground(ThemeManager.COLOR_SUCCESS);
        successTitle.setAlignmentX(CENTER_ALIGNMENT);

        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(successTitle);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center Details Card
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(ThemeManager.COLOR_CARD_BG);
        card.setBorder(ThemeManager.createCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 12, 6, 12);

        // PNR row (highlighted)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.35;
        JLabel pnrLabel = new JLabel("PNR NUMBER:");
        pnrLabel.setFont(ThemeManager.FONT_BOLD);
        pnrLabel.setForeground(ThemeManager.COLOR_PRIMARY);
        card.add(pnrLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        JLabel pnrVal = new JLabel(reservation.getPnr());
        pnrVal.setFont(ThemeManager.FONT_PNR);
        pnrVal.setForeground(ThemeManager.COLOR_PRIMARY);
        card.add(pnrVal, gbc);

        // Passenger Name
        addDetailRow(card, gbc, 1, "Passenger Name:", reservation.getPassengerName());

        // Train
        String trainDisplay = reservation.getTrainNumber() + " - " +
                (reservation.getTrainName() != null ? reservation.getTrainName() : "N/A");
        addDetailRow(card, gbc, 2, "Train:", trainDisplay);

        // Class
        addDetailRow(card, gbc, 3, "Travel Class:", reservation.getClassType());

        // Journey Date
        addDetailRow(card, gbc, 4, "Journey Date:", reservation.getJourneyDate());

        // Route
        String routeDisplay = reservation.getSource() + "  ->  " + reservation.getDestination();
        addDetailRow(card, gbc, 5, "Route:", routeDisplay);

        // Booking Time
        addDetailRow(card, gbc, 6, "Booked At:", reservation.getBookedAt());

        mainPanel.add(card, BorderLayout.CENTER);

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton copyButton = new JButton("Copy PNR");
        ThemeManager.styleSecondaryButton(copyButton);
        copyButton.addActionListener(e -> {
            StringSelection selection = new StringSelection(reservation.getPnr());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            copyButton.setText("Copied!");
        });

        JButton exportButton = new JButton("Save Ticket (.txt)");
        ThemeManager.styleSecondaryButton(exportButton);
        exportButton.addActionListener(e -> exportTicket());

        JButton closeButton = new JButton("Done");
        ThemeManager.stylePrimaryButton(closeButton);
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(copyButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(closeButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void addDetailRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(ThemeManager.FONT_REGULAR);
        lbl.setForeground(ThemeManager.COLOR_TEXT_MUTED);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        JLabel val = new JLabel(value);
        val.setFont(ThemeManager.FONT_BOLD);
        val.setForeground(ThemeManager.COLOR_TEXT_DARK);
        panel.add(val, gbc);
    }

    private void exportTicket() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save E-Ticket");
        fileChooser.setSelectedFile(new File("Ticket_" + reservation.getPnr() + ".txt"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Documents (*.txt)", "txt"));

        int userChoice = fileChooser.showSaveDialog(this);
        if (userChoice == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".txt");
            }
            try {
                reservationService.exportTicketToFile(reservation, selectedFile);
                JOptionPane.showMessageDialog(this,
                        "Ticket saved successfully:\n" + selectedFile.getAbsolutePath(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to save ticket: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
