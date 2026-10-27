package com.mudassir.railbook.ui;

import com.mudassir.railbook.config.Constants;
import com.mudassir.railbook.model.Reservation;
import com.mudassir.railbook.model.User;
import com.mudassir.railbook.service.ReservationService;
import com.mudassir.railbook.service.ValidationException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.Optional;

/**
 * Panel handling the ticket cancellation flow.
 * Requires fetching a valid reservation before enabling confirmation,
 * asks for user consent via confirmation dialog, executes inside a transaction,
 * and audits the cancellation.
 *
 * @author Mudassir Ahmed
 */
public class CancellationPanel extends JPanel {

    private final ReservationService reservationService;
    private final User currentUser;
    private final Runnable onCancellationSuccessCallback;

    private JTextField pnrInputField;
    private JButton fetchButton;
    private JButton cancelButton;
    private JLabel statusLabel;

    // Read-only detail labels
    private JPanel detailsCard;
    private JLabel pnrValueLabel;
    private JLabel passengerValueLabel;
    private JLabel trainValueLabel;
    private JLabel classValueLabel;
    private JLabel dateValueLabel;
    private JLabel routeValueLabel;
    private JLabel bookedAtValueLabel;

    private Reservation currentFetchedReservation;

    public CancellationPanel(ReservationService reservationService, User currentUser,
                             Runnable onCancellationSuccessCallback) {
        this.reservationService = reservationService;
        this.currentUser = currentUser;
        this.onCancellationSuccessCallback = onCancellationSuccessCallback;

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);

        JPanel scrollContent = new JPanel(new GridBagLayout());
        scrollContent.setBackground(Color.WHITE);
        scrollContent.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);

        // Header
        JLabel sectionHeader = new JLabel("Ticket Cancellation Portal");
        sectionHeader.setFont(ThemeManager.FONT_HEADER);
        sectionHeader.setForeground(ThemeManager.COLOR_PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        scrollContent.add(sectionHeader, gbc);

        JLabel sectionSub = new JLabel("Enter your 10-digit PNR to retrieve booking details and initiate cancellation.");
        sectionSub.setFont(ThemeManager.FONT_SMALL);
        sectionSub.setForeground(ThemeManager.COLOR_TEXT_MUTED);
        gbc.gridy = 1;
        gbc.insets = new Insets(2, 8, 16, 8);
        scrollContent.add(sectionSub, gbc);

        // PNR Input Row
        JPanel pnrRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnrRow.setBackground(Color.WHITE);

        JLabel pnrLabel = new JLabel("Enter PNR Number *");
        pnrLabel.setFont(ThemeManager.FONT_LABEL);
        pnrLabel.setDisplayedMnemonic(KeyEvent.VK_P);

        pnrInputField = new JTextField(14);
        pnrInputField.setFont(ThemeManager.FONT_BOLD);
        pnrInputField.setPreferredSize(new Dimension(180, 36));
        pnrLabel.setLabelFor(pnrInputField);

        // Pressing Enter in PNR field fetches booking
        pnrInputField.addActionListener(e -> fetchBooking());

        fetchButton = new JButton("Fetch Booking Details");
        fetchButton.setMnemonic(KeyEvent.VK_F);
        ThemeManager.styleSecondaryButton(fetchButton);
        fetchButton.setPreferredSize(new Dimension(180, 36));
        fetchButton.addActionListener(e -> fetchBooking());

        pnrRow.add(pnrLabel);
        pnrRow.add(pnrInputField);
        pnrRow.add(fetchButton);

        gbc.gridy = 2;
        gbc.insets = new Insets(6, 8, 12, 8);
        scrollContent.add(pnrRow, gbc);

        // Status / Error message label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(ThemeManager.FONT_SMALL);
        statusLabel.setForeground(ThemeManager.COLOR_DANGER);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 8, 8, 8);
        scrollContent.add(statusLabel, gbc);

        // Details Card (initially empty / hidden until fetched)
        detailsCard = new JPanel(new GridBagLayout());
        detailsCard.setBackground(ThemeManager.COLOR_CARD_BG);
        detailsCard.setBorder(ThemeManager.createCardBorder());
        detailsCard.setVisible(false);

        GridBagConstraints cardGbc = new GridBagConstraints();
        cardGbc.fill = GridBagConstraints.HORIZONTAL;
        cardGbc.insets = new Insets(5, 12, 5, 12);

        // PNR
        cardGbc.gridx = 0;
        cardGbc.gridy = 0;
        cardGbc.weightx = 0.35;
        JLabel cardPnrLabel = new JLabel("PNR NUMBER:");
        cardPnrLabel.setFont(ThemeManager.FONT_LABEL);
        cardPnrLabel.setForeground(ThemeManager.COLOR_PRIMARY);
        detailsCard.add(cardPnrLabel, cardGbc);

        cardGbc.gridx = 1;
        cardGbc.weightx = 0.65;
        pnrValueLabel = new JLabel("-");
        pnrValueLabel.setFont(ThemeManager.FONT_PNR);
        pnrValueLabel.setForeground(ThemeManager.COLOR_PRIMARY);
        detailsCard.add(pnrValueLabel, cardGbc);

        // Passenger Name
        passengerValueLabel = addDetailRow(detailsCard, cardGbc, 1, "Passenger Name:");
        trainValueLabel = addDetailRow(detailsCard, cardGbc, 2, "Train:");
        classValueLabel = addDetailRow(detailsCard, cardGbc, 3, "Class:");
        dateValueLabel = addDetailRow(detailsCard, cardGbc, 4, "Journey Date:");
        routeValueLabel = addDetailRow(detailsCard, cardGbc, 5, "Route:");
        bookedAtValueLabel = addDetailRow(detailsCard, cardGbc, 6, "Booked At:");

        gbc.gridy = 4;
        gbc.insets = new Insets(8, 8, 16, 8);
        scrollContent.add(detailsCard, gbc);

        // Confirm Cancellation Button
        cancelButton = new JButton("Confirm Cancellation");
        cancelButton.setMnemonic(KeyEvent.VK_C);
        ThemeManager.styleDangerButton(cancelButton);
        cancelButton.setPreferredSize(new Dimension(220, 40));
        cancelButton.setEnabled(false); // Stays disabled until booking is fetched!
        cancelButton.addActionListener(e -> confirmAndCancel());

        gbc.gridy = 5;
        gbc.insets = new Insets(8, 8, 12, 8);
        scrollContent.add(cancelButton, gbc);

        add(new JScrollPane(scrollContent), BorderLayout.CENTER);
    }

    private JLabel addDetailRow(JPanel card, GridBagConstraints gbc, int row, String label) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(ThemeManager.FONT_REGULAR);
        lbl.setForeground(ThemeManager.COLOR_TEXT_MUTED);
        card.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        JLabel val = new JLabel("-");
        val.setFont(ThemeManager.FONT_BOLD);
        val.setForeground(ThemeManager.COLOR_TEXT_DARK);
        card.add(val, gbc);
        return val;
    }

    private void fetchBooking() {
        String pnr = pnrInputField.getText().trim();
        statusLabel.setText(" ");
        cancelButton.setEnabled(false);
        currentFetchedReservation = null;

        if (pnr.isEmpty()) {
            statusLabel.setText("• Please enter a PNR number to fetch.");
            detailsCard.setVisible(false);
            return;
        }

        fetchButton.setEnabled(false);
        fetchButton.setText("Fetching...");

        SwingWorker<Optional<Reservation>, Void> worker = new SwingWorker<>() {
            @Override
            protected Optional<Reservation> doInBackground() throws Exception {
                return reservationService.fetchBookingByPnr(pnr);
            }

            @Override
            protected void done() {
                fetchButton.setEnabled(true);
                fetchButton.setText("Fetch Booking Details");

                try {
                    Optional<Reservation> resOpt = get();
                    if (resOpt.isEmpty()) {
                        detailsCard.setVisible(false);
                        cancelButton.setEnabled(false);
                        currentFetchedReservation = null;
                        statusLabel.setForeground(ThemeManager.COLOR_DANGER);
                        statusLabel.setText("• No booking found for this PNR: " + pnr);
                    } else {
                        Reservation res = resOpt.get();
                        currentFetchedReservation = res;
                        populateDetails(res);
                        statusLabel.setForeground(ThemeManager.COLOR_SUCCESS);
                        statusLabel.setText("Active booking found. Click 'Confirm Cancellation' below to proceed.");
                        cancelButton.setEnabled(true); // Enable button once fetched!
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    String message = cause instanceof ValidationException ?
                            cause.getMessage() : "Error fetching booking: " + cause.getMessage();
                    detailsCard.setVisible(false);
                    cancelButton.setEnabled(false);
                    statusLabel.setForeground(ThemeManager.COLOR_DANGER);
                    statusLabel.setText("• " + message);
                }
            }
        };
        worker.execute();
    }

    private void populateDetails(Reservation res) {
        pnrValueLabel.setText(res.getPnr());
        passengerValueLabel.setText(res.getPassengerName());
        trainValueLabel.setText(res.getTrainNumber() + " - " +
                (res.getTrainName() != null ? res.getTrainName() : "N/A"));
        classValueLabel.setText(res.getClassType());
        dateValueLabel.setText(res.getJourneyDate());
        routeValueLabel.setText(res.getSource() + " -> " + res.getDestination());
        bookedAtValueLabel.setText(res.getBookedAt());
        detailsCard.setVisible(true);
        revalidate();
        repaint();
    }

    private void confirmAndCancel() {
        if (currentFetchedReservation == null) {
            return;
        }

        String pnr = currentFetchedReservation.getPnr();

        // "Are you sure?" confirmation dialog
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel the reservation for PNR " + pnr + "?\n" +
                        "Passenger: " + currentFetchedReservation.getPassengerName() + "\n" +
                        "This action cannot be undone.",
                "Confirm Ticket Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            // User clicked No or closed dialog: do nothing
            return;
        }

        cancelButton.setEnabled(false);
        cancelButton.setText("Cancelling...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return reservationService.cancelBooking(pnr, currentUser.getId());
            }

            @Override
            protected void done() {
                cancelButton.setText("Confirm Cancellation");
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(
                                CancellationPanel.this,
                                "Booking for PNR " + pnr + " has been successfully cancelled.\n" +
                                        "An audit record has been logged in the cancellation register.",
                                "Cancellation Successful",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        // Clear form after cancelling
                        clearForm();

                        // Notify parent to refresh bookings table
                        if (onCancellationSuccessCallback != null) {
                            onCancellationSuccessCallback.run();
                        }
                    } else {
                        JOptionPane.showMessageDialog(
                                CancellationPanel.this,
                                "Unable to cancel: Booking may have already been cancelled.",
                                "Cancellation Failed",
                                JOptionPane.WARNING_MESSAGE
                        );
                        clearForm();
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(
                            CancellationPanel.this,
                            "Cancellation failed: " + cause.getMessage(),
                            "Cancellation Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    cancelButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    public void clearForm() {
        pnrInputField.setText("");
        currentFetchedReservation = null;
        detailsCard.setVisible(false);
        cancelButton.setEnabled(false);
        statusLabel.setText(" ");
        pnrInputField.requestFocusInWindow();
        revalidate();
        repaint();
    }

    /**
     * Programmatically sets PNR and fetches (e.g. from My Bookings tab).
     */
    public void setPnrAndFetch(String pnr) {
        pnrInputField.setText(pnr);
        fetchBooking();
    }
}
