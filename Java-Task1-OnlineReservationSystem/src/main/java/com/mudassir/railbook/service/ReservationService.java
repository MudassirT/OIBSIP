package com.mudassir.railbook.service;

import com.mudassir.railbook.config.Constants;
import com.mudassir.railbook.db.ReservationDao;
import com.mudassir.railbook.db.TrainDao;
import com.mudassir.railbook.model.Reservation;
import com.mudassir.railbook.model.Train;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service managing train reservations, PNR generation with collision retries,
 * ticket cancellation workflows, and ticket file export.
 *
 * @author Mudassir Ahmed
 */
public class ReservationService {

    private static final Logger LOGGER = Logger.getLogger(ReservationService.class.getName());
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(Constants.TIMESTAMP_FORMAT);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ReservationDao reservationDao;
    private final TrainDao trainDao;

    public ReservationService(ReservationDao reservationDao, TrainDao trainDao) {
        this.reservationDao = reservationDao;
        this.trainDao = trainDao;
    }

    /**
     * Books a ticket after thorough validation and PNR generation.
     *
     * @param userId        ID of logged-in user
     * @param passengerName passenger's full name
     * @param trainNumberStr train number as string
     * @param classType     selected travel class
     * @param journeyDate   journey date in yyyy-MM-dd
     * @param source        source station
     * @param destination   destination station
     * @return the saved {@link Reservation}
     * @throws ValidationException if any input violates domain rules
     * @throws SQLException        on persistent database failure
     */
    public Reservation bookTicket(int userId, String passengerName, String trainNumberStr,
                                  String classType, String journeyDate,
                                  String source, String destination) throws ValidationException, SQLException {
        // Trim all inputs
        String trimmedName = passengerName != null ? passengerName.trim() : "";
        String trimmedTrainNumber = trainNumberStr != null ? trainNumberStr.trim() : "";
        String trimmedClass = classType != null ? classType.trim() : "";
        String trimmedDate = journeyDate != null ? journeyDate.trim() : "";
        String trimmedSource = source != null ? source.trim() : "";
        String trimmedDestination = destination != null ? destination.trim() : "";

        // Validate all fields
        ValidationResult validationResult = ValidationUtil.validateReservation(
                trimmedName, trimmedTrainNumber, trimmedClass, trimmedDate, trimmedSource, trimmedDestination);

        if (!validationResult.isValid()) {
            LOGGER.warning("Validation failed for reservation booking: " + validationResult.getFirstErrorMessage());
            throw new ValidationException(validationResult.getFirstErrorMessage());
        }

        int trainNumber = Integer.parseInt(trimmedTrainNumber);

        // Verify train exists in database
        Optional<Train> trainOpt = trainDao.findByTrainNumber(trainNumber);
        if (trainOpt.isEmpty()) {
            throw new ValidationException("Train number " + trainNumber + " does not exist in the database.");
        }
        Train train = trainOpt.get();

        String bookedAt = LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        // Generate unique PNR with retry mechanism
        for (int attempt = 1; attempt <= Constants.MAX_PNR_GENERATION_RETRIES; attempt++) {
            String pnr = generate10DigitPnr();

            if (reservationDao.isPnrExists(pnr)) {
                LOGGER.warning("PNR collision detected (" + pnr + "), retrying... Attempt " + attempt);
                continue;
            }

            Reservation reservation = new Reservation(
                    pnr,
                    userId,
                    trimmedName,
                    trainNumber,
                    train.getTrainName(),
                    trimmedClass,
                    trimmedDate,
                    trimmedSource,
                    trimmedDestination,
                    bookedAt
            );

            try {
                reservationDao.save(reservation);
                return reservation;
            } catch (SQLException e) {
                if (attempt == Constants.MAX_PNR_GENERATION_RETRIES) {
                    LOGGER.log(Level.SEVERE, "Failed to save reservation after " + attempt + " attempts", e);
                    throw e;
                }
            }
        }

        throw new SQLException("Unable to generate a unique PNR after " + Constants.MAX_PNR_GENERATION_RETRIES + " attempts. Please try again.");
    }

    /**
     * Generates a 10-digit numeric string (1000000000 to 9999999999).
     *
     * @return 10-digit PNR string
     */
    public String generate10DigitPnr() {
        // Ensure first digit is 1-9 to guarantee 10 digits
        long min = 1_000_000_000L;
        long max = 9_999_999_999L;
        long randomValue = min + (long) (RANDOM.nextDouble() * (max - min + 1));
        return String.valueOf(randomValue);
    }

    /**
     * Looks up an existing reservation by its 10-digit PNR.
     *
     * @param pnr 10-digit PNR string
     * @return Optional of Reservation
     * @throws ValidationException if PNR format is invalid
     */
    public Optional<Reservation> fetchBookingByPnr(String pnr) throws ValidationException {
        if (pnr == null || pnr.trim().isEmpty()) {
            throw new ValidationException("PNR cannot be empty.");
        }
        String trimmedPnr = pnr.trim();
        String error = ValidationUtil.validatePnr(trimmedPnr);
        if (error != null) {
            throw new ValidationException(error);
        }

        return reservationDao.findByPnr(trimmedPnr);
    }

    /**
     * Cancels an existing booking by PNR and logs the action into cancellation audit log.
     *
     * @param pnr                10-digit PNR string
     * @param cancelledByUserId  ID of user performing cancellation
     * @return true if successfully cancelled, false if booking was not found
     * @throws ValidationException if PNR format is invalid
     * @throws SQLException        on database failure
     */
    public boolean cancelBooking(String pnr, int cancelledByUserId) throws ValidationException, SQLException {
        if (pnr == null || pnr.trim().isEmpty()) {
            throw new ValidationException("PNR cannot be empty.");
        }
        String trimmedPnr = pnr.trim();
        String error = ValidationUtil.validatePnr(trimmedPnr);
        if (error != null) {
            throw new ValidationException(error);
        }

        return reservationDao.cancelReservation(trimmedPnr, cancelledByUserId);
    }

    /**
     * Retrieves all active bookings for a given user.
     *
     * @param userId user ID
     * @return list of reservations
     */
    public List<Reservation> getUserBookings(int userId) {
        return reservationDao.findByUserId(userId);
    }

    /**
     * Exports a booking confirmation as a formatted text ticket file.
     *
     * @param reservation reservation details
     * @param targetFile  file destination
     * @throws IOException on write error
     */
    public void exportTicketToFile(Reservation reservation, File targetFile) throws IOException {
        if (reservation == null || targetFile == null) {
            throw new IllegalArgumentException("Reservation and target file cannot be null.");
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile, StandardCharsets.UTF_8))) {
            writer.println("================================================================================");
            writer.println("                      RAILBOOK ELECTRONIC RESERVATION SLIP                      ");
            writer.println("                         Oasis Infobyte - Java Task 1                           ");
            writer.println("================================================================================");
            writer.println();
            writer.printf("  PNR NUMBER         : %s%n", reservation.getPnr());
            writer.printf("  BOOKING TIMESTAMP  : %s%n", reservation.getBookedAt());
            writer.println("--------------------------------------------------------------------------------");
            writer.printf("  PASSENGER NAME     : %s%n", reservation.getPassengerName());
            writer.printf("  TRAIN NUMBER & NAME: %d - %s%n", reservation.getTrainNumber(),
                    reservation.getTrainName() != null ? reservation.getTrainName() : "N/A");
            writer.printf("  TRAVEL CLASS       : %s%n", reservation.getClassType());
            writer.printf("  JOURNEY DATE       : %s%n", reservation.getJourneyDate());
            writer.println("--------------------------------------------------------------------------------");
            writer.printf("  ORIGIN STATION     : %s%n", reservation.getSource());
            writer.printf("  DESTINATION STATION: %s%n", reservation.getDestination());
            writer.println("================================================================================");
            writer.println("  STATUS             : CONFIRMED");
            writer.println("  PAYMENT MODE       : SYSTEM AUTHORIZED (TEST / DEMO)");
            writer.println("--------------------------------------------------------------------------------");
            writer.println("  IMPORTANT INSTRUCTIONS:");
            writer.println("  1. Please carry a valid original Government photo ID along with this ticket.");
            writer.println("  2. Arrive at the boarding station at least 30 minutes before scheduled departure.");
            writer.println("  3. For cancellations, use your 10-digit PNR in the RailBook Cancellation portal.");
            writer.println("  4. Timetables and schedules are sample demonstration data for Oasis Infobyte.");
            writer.println("================================================================================");
            writer.println("           Thank you for choosing RailBook. Wishing you a pleasant journey!     ");
            writer.println("================================================================================");
        }
    }
}
