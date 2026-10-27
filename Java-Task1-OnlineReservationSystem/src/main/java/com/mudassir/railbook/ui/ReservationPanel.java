package com.mudassir.railbook.ui;

import com.mudassir.railbook.config.Constants;
import com.mudassir.railbook.db.TrainDao;
import com.mudassir.railbook.model.Reservation;
import com.mudassir.railbook.model.Train;
import com.mudassir.railbook.model.User;
import com.mudassir.railbook.service.ReservationService;
import com.mudassir.railbook.service.ValidationException;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Reservation form panel allowing users to book train tickets.
 * Includes instant train lookup, validation feedback, and booking confirmation.
 *
 * @author Mudassir Ahmed
 */
public class ReservationPanel extends JPanel {

    private final ReservationService reservationService;
    private final TrainDao trainDao;
    private final User currentUser;
    private final Runnable onBookingSuccessCallback;

    private JTextField passengerNameField;
    private JTextField trainNumberField;
    private JTextField trainNameField;
    private JComboBox<String> classTypeCombo;
    private JTextField journeyDateField;
    private JComboBox<String> sourceStationCombo;
    private JComboBox<String> destinationStationCombo;
    private JButton bookButton;
    private JButton clearButton;
    private JLabel statusLabel;

    public ReservationPanel(ReservationService reservationService, TrainDao trainDao,
                            User currentUser, Runnable onBookingSuccessCallback) {
        this.reservationService = reservationService;
        this.trainDao = trainDao;
        this.currentUser = currentUser;
        this.onBookingSuccessCallback = onBookingSuccessCallback;

        initComponents();
        loadStations();
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

        // Section Header
        JLabel sectionHeader = new JLabel("Book a New Train Ticket");
        sectionHeader.setFont(ThemeManager.FONT_HEADER);
        sectionHeader.setForeground(ThemeManager.COLOR_PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        scrollContent.add(sectionHeader, gbc);

        JLabel sectionSub = new JLabel("Enter passenger and journey details. Train name is auto-filled upon entering a valid train number.");
        sectionSub.setFont(ThemeManager.FONT_SMALL);
        sectionSub.setForeground(ThemeManager.COLOR_TEXT_MUTED);
        gbc.gridy = 1;
        gbc.insets = new Insets(2, 8, 16, 8);
        scrollContent.add(sectionSub, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(6, 8, 6, 8);

        // 1. Passenger Name
        JLabel nameLabel = new JLabel("Passenger Name *");
        nameLabel.setFont(ThemeManager.FONT_LABEL);
        nameLabel.setDisplayedMnemonic(KeyEvent.VK_N);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        scrollContent.add(nameLabel, gbc);

        passengerNameField = new JTextField(24);
        passengerNameField.setFont(ThemeManager.FONT_REGULAR);
        passengerNameField.setPreferredSize(new Dimension(280, 34));
        nameLabel.setLabelFor(passengerNameField);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        scrollContent.add(passengerNameField, gbc);

        // 2. Train Number
        JLabel trainNumLabel = new JLabel("Train Number (Numeric) *");
        trainNumLabel.setFont(ThemeManager.FONT_LABEL);
        trainNumLabel.setDisplayedMnemonic(KeyEvent.VK_T);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        scrollContent.add(trainNumLabel, gbc);

        JPanel trainNumContainer = new JPanel(new BorderLayout(8, 0));
        trainNumContainer.setBackground(Color.WHITE);

        trainNumberField = new JTextField(12);
        trainNumberField.setFont(ThemeManager.FONT_REGULAR);
        trainNumberField.setPreferredSize(new Dimension(160, 34));
        trainNumLabel.setLabelFor(trainNumberField);

        JLabel trainHint = new JLabel("(e.g. 101, 102, 103)");
        trainHint.setFont(ThemeManager.FONT_SMALL);
        trainHint.setForeground(ThemeManager.COLOR_TEXT_MUTED);

        trainNumContainer.add(trainNumberField, BorderLayout.WEST);
        trainNumContainer.add(trainHint, BorderLayout.CENTER);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        scrollContent.add(trainNumContainer, gbc);

        // 3. Train Name (Auto-populated and read-only)
        JLabel trainNameLabel = new JLabel("Train Name (Auto-populated)");
        trainNameLabel.setFont(ThemeManager.FONT_LABEL);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.3;
        scrollContent.add(trainNameLabel, gbc);

        trainNameField = new JTextField(24);
        trainNameField.setFont(ThemeManager.FONT_BOLD);
        trainNameField.setEditable(false);
        trainNameField.setBackground(new Color(241, 245, 249)); // light slate read-only
        trainNameField.setForeground(ThemeManager.COLOR_PRIMARY);
        trainNameField.setPreferredSize(new Dimension(280, 34));
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        scrollContent.add(trainNameField, gbc);

        // Auto-lookup listener on Train Number field
        trainNumberField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                lookupTrain();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                lookupTrain();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                lookupTrain();
            }
        });

        // 4. Class Type
        JLabel classLabel = new JLabel("Class Type *");
        classLabel.setFont(ThemeManager.FONT_LABEL);
        classLabel.setDisplayedMnemonic(KeyEvent.VK_C);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.3;
        scrollContent.add(classLabel, gbc);

        classTypeCombo = new JComboBox<>(Constants.ALL_CLASS_TYPES);
        classTypeCombo.setFont(ThemeManager.FONT_REGULAR);
        classTypeCombo.setPreferredSize(new Dimension(280, 34));
        classLabel.setLabelFor(classTypeCombo);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        scrollContent.add(classTypeCombo, gbc);

        // 5. Date of Journey
        JLabel dateLabel = new JLabel("Date of Journey (YYYY-MM-DD) *");
        dateLabel.setFont(ThemeManager.FONT_LABEL);
        dateLabel.setDisplayedMnemonic(KeyEvent.VK_D);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0.3;
        scrollContent.add(dateLabel, gbc);

        JPanel dateContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        dateContainer.setBackground(Color.WHITE);

        journeyDateField = new JTextField(12);
        journeyDateField.setFont(ThemeManager.FONT_REGULAR);
        journeyDateField.setPreferredSize(new Dimension(140, 34));
        journeyDateField.setText(LocalDate.now().plusDays(1).toString()); // default tomorrow
        dateLabel.setLabelFor(journeyDateField);

        JButton tomorrowBtn = new JButton("Tomorrow");
        ThemeManager.styleSecondaryButton(tomorrowBtn);
        tomorrowBtn.setPreferredSize(new Dimension(90, 32));
        tomorrowBtn.addActionListener(e -> journeyDateField.setText(LocalDate.now().plusDays(1).toString()));

        JButton weekBtn = new JButton("+7 Days");
        ThemeManager.styleSecondaryButton(weekBtn);
        weekBtn.setPreferredSize(new Dimension(80, 32));
        weekBtn.addActionListener(e -> journeyDateField.setText(LocalDate.now().plusDays(7).toString()));

        dateContainer.add(journeyDateField);
        dateContainer.add(tomorrowBtn);
        dateContainer.add(weekBtn);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        scrollContent.add(dateContainer, gbc);

        // 6. Source Station
        JLabel sourceLabel = new JLabel("Source Station *");
        sourceLabel.setFont(ThemeManager.FONT_LABEL);
        sourceLabel.setDisplayedMnemonic(KeyEvent.VK_S);
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.weightx = 0.3;
        scrollContent.add(sourceLabel, gbc);

        sourceStationCombo = new JComboBox<>();
        sourceStationCombo.setFont(ThemeManager.FONT_REGULAR);
        sourceStationCombo.setPreferredSize(new Dimension(280, 34));
        sourceLabel.setLabelFor(sourceStationCombo);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        scrollContent.add(sourceStationCombo, gbc);

        // 7. Destination Station
        JLabel destLabel = new JLabel("Destination Station *");
        destLabel.setFont(ThemeManager.FONT_LABEL);
        destLabel.setDisplayedMnemonic(KeyEvent.VK_E);
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.weightx = 0.3;
        scrollContent.add(destLabel, gbc);

        destinationStationCombo = new JComboBox<>();
        destinationStationCombo.setFont(ThemeManager.FONT_REGULAR);
        destinationStationCombo.setPreferredSize(new Dimension(280, 34));
        destLabel.setLabelFor(destinationStationCombo);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        scrollContent.add(destinationStationCombo, gbc);

        // Status Label (Inline message)
        statusLabel = new JLabel(" ");
        statusLabel.setFont(ThemeManager.FONT_SMALL);
        statusLabel.setForeground(ThemeManager.COLOR_DANGER);
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(8, 8, 8, 8);
        scrollContent.add(statusLabel, gbc);

        // Action Buttons Row
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonPanel.setBackground(Color.WHITE);

        bookButton = new JButton("Book Ticket Now");
        bookButton.setMnemonic(KeyEvent.VK_B);
        ThemeManager.stylePrimaryButton(bookButton);
        bookButton.setPreferredSize(new Dimension(170, 38));
        bookButton.addActionListener(e -> performBooking());

        clearButton = new JButton("Reset Form");
        clearButton.setMnemonic(KeyEvent.VK_R);
        ThemeManager.styleSecondaryButton(clearButton);
        clearButton.setPreferredSize(new Dimension(120, 38));
        clearButton.addActionListener(e -> resetForm());

        buttonPanel.add(bookButton);
        buttonPanel.add(clearButton);

        gbc.gridy = 10;
        gbc.insets = new Insets(12, 8, 12, 8);
        scrollContent.add(buttonPanel, gbc);

        add(new JScrollPane(scrollContent), BorderLayout.CENTER);
    }

    private void loadStations() {
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() {
                return trainDao.getAllStationNames();
            }

            @Override
            protected void done() {
                try {
                    List<String> stations = get();
                    DefaultComboBoxModel<String> srcModel = new DefaultComboBoxModel<>();
                    DefaultComboBoxModel<String> destModel = new DefaultComboBoxModel<>();
                    for (String station : stations) {
                        srcModel.addElement(station);
                        destModel.addElement(station);
                    }
                    sourceStationCombo.setModel(srcModel);
                    destinationStationCombo.setModel(destModel);
                    if (destModel.getSize() > 1) {
                        destinationStationCombo.setSelectedIndex(1);
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private void lookupTrain() {
        String input = trainNumberField.getText().trim();
        if (input.isEmpty()) {
            trainNameField.setText("");
            return;
        }
        try {
            int trainNum = Integer.parseInt(input);
            Optional<Train> trainOpt = trainDao.findByTrainNumber(trainNum);
            if (trainOpt.isPresent()) {
                Train train = trainOpt.get();
                trainNameField.setText(train.getTrainName());
                trainNameField.setForeground(ThemeManager.COLOR_PRIMARY);

                // Auto-suggest train's route in combos if available
                if (train.getSourceStation() != null) {
                    sourceStationCombo.setSelectedItem(train.getSourceStation());
                }
                if (train.getDestinationStation() != null) {
                    destinationStationCombo.setSelectedItem(train.getDestinationStation());
                }
            } else {
                trainNameField.setText("(Train not found)");
                trainNameField.setForeground(ThemeManager.COLOR_DANGER);
            }
        } catch (NumberFormatException e) {
            trainNameField.setText("(Invalid train number)");
            trainNameField.setForeground(ThemeManager.COLOR_DANGER);
        }
    }

    private void performBooking() {
        statusLabel.setText(" ");
        bookButton.setEnabled(false);
        bookButton.setText("Booking...");

        String passengerName = passengerNameField.getText();
        String trainNumber = trainNumberField.getText();
        String classType = (String) classTypeCombo.getSelectedItem();
        String journeyDate = journeyDateField.getText();
        String source = (String) sourceStationCombo.getSelectedItem();
        String destination = (String) destinationStationCombo.getSelectedItem();

        SwingWorker<Reservation, Void> worker = new SwingWorker<>() {
            @Override
            protected Reservation doInBackground() throws Exception {
                return reservationService.bookTicket(
                        currentUser.getId(),
                        passengerName,
                        trainNumber,
                        classType,
                        journeyDate,
                        source,
                        destination
                );
            }

            @Override
            protected void done() {
                bookButton.setEnabled(true);
                bookButton.setText("Book Ticket Now");

                try {
                    Reservation reservation = get();
                    // Booking succeeded: show confirmation dialog
                    JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(ReservationPanel.this);
                    ConfirmationDialog dialog = new ConfirmationDialog(parentFrame, reservation, reservationService);
                    dialog.setVisible(true);

                    // Reset form and notify callback
                    resetForm();
                    if (onBookingSuccessCallback != null) {
                        onBookingSuccessCallback.run();
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    String message = cause instanceof ValidationException ?
                            cause.getMessage() : "Booking failed: " + cause.getMessage();
                    statusLabel.setText("<html><font color='#dc2626'>• " + message + "</font></html>");
                    JOptionPane.showMessageDialog(ReservationPanel.this,
                            message, "Booking Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public void resetForm() {
        passengerNameField.setText("");
        trainNumberField.setText("");
        trainNameField.setText("");
        classTypeCombo.setSelectedIndex(0);
        journeyDateField.setText(LocalDate.now().plusDays(1).toString());
        if (sourceStationCombo.getItemCount() > 0) sourceStationCombo.setSelectedIndex(0);
        if (destinationStationCombo.getItemCount() > 1) destinationStationCombo.setSelectedIndex(1);
        statusLabel.setText(" ");
        passengerNameField.requestFocusInWindow();
    }
}
