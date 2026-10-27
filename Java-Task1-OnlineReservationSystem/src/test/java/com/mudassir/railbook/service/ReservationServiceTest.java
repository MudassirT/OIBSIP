package com.mudassir.railbook.service;

import com.mudassir.railbook.db.DatabaseManager;
import com.mudassir.railbook.db.ReservationDao;
import com.mudassir.railbook.db.TrainDao;
import com.mudassir.railbook.model.CancellationAudit;
import com.mudassir.railbook.model.Reservation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for booking creation, PNR generation, cancellation audit logging, and queries.
 *
 * @author Mudassir Ahmed
 */
class ReservationServiceTest {

    private static final String TEST_DB_PATH = "target/test_reservation.db";
    private DatabaseManager dbManager;
    private ReservationDao reservationDao;
    private TrainDao trainDao;
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        File dbFile = new File(TEST_DB_PATH);
        if (dbFile.exists()) {
            dbFile.delete();
        }
        dbManager = new DatabaseManager("jdbc:sqlite:" + TEST_DB_PATH);
        dbManager.initializeDatabase();

        reservationDao = new ReservationDao(dbManager);
        trainDao = new TrainDao(dbManager);
        reservationService = new ReservationService(reservationDao, trainDao);
    }

    @AfterEach
    void tearDown() {
        File dbFile = new File(TEST_DB_PATH);
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    @Test
    @DisplayName("PNR generation produces 10-digit numeric identifiers without collisions across 1,000 samples")
    void testPnrUniqueness() {
        Set<String> generatedPnrs = new HashSet<>();
        int count = 1000;

        for (int i = 0; i < count; i++) {
            String pnr = reservationService.generate10DigitPnr();
            assertNotNull(pnr);
            assertEquals(10, pnr.length(), "PNR must be exactly 10 digits");
            assertTrue(pnr.matches("^[0-9]{10}$"), "PNR must be purely numeric");
            generatedPnrs.add(pnr);
        }

        assertEquals(count, generatedPnrs.size(), "All 1,000 generated PNRs should be unique");
    }

    @Test
    @DisplayName("Booking ticket saves row to database and returns reservation with 10-digit PNR")
    void testBookTicketSuccess() throws Exception {
        String journeyDate = LocalDate.now().plusDays(5).toString();
        Reservation reservation = reservationService.bookTicket(
                1,
                "Mudassir Ahmed",
                "101",
                "AC First",
                journeyDate,
                "Karachi Cantt",
                "Islamabad"
        );

        assertNotNull(reservation);
        assertNotNull(reservation.getPnr());
        assertEquals(10, reservation.getPnr().length());
        assertEquals("Mudassir Ahmed", reservation.getPassengerName());
        assertEquals(101, reservation.getTrainNumber());
        assertEquals("Green Line Express", reservation.getTrainName());
        assertEquals("AC First", reservation.getClassType());
        assertEquals(journeyDate, reservation.getJourneyDate());
        assertEquals("Karachi Cantt", reservation.getSource());
        assertEquals("Islamabad", reservation.getDestination());
        assertNotNull(reservation.getBookedAt());
    }

    @Test
    @DisplayName("Booking with non-existent train number throws ValidationException")
    void testBookingWithInvalidTrainNumber() {
        String journeyDate = LocalDate.now().plusDays(5).toString();
        assertThrows(ValidationException.class, () ->
                reservationService.bookTicket(
                        1,
                        "Mudassir Ahmed",
                        "99999", // non-existent train
                        "Economy",
                        journeyDate,
                        "Karachi Cantt",
                        "Lahore Junction"
                ));
    }

    @Test
    @DisplayName("Fetch booking by valid PNR returns complete reservation details")
    void testFetchBookingByPnr() throws Exception {
        String journeyDate = LocalDate.now().plusDays(10).toString();
        Reservation booked = reservationService.bookTicket(
                1,
                "Sara Khan",
                "102",
                "AC Business",
                journeyDate,
                "Karachi Cantt",
                "Rawalpindi"
        );

        Optional<Reservation> fetched = reservationService.fetchBookingByPnr(booked.getPnr());
        assertTrue(fetched.isPresent());
        assertEquals(booked.getPnr(), fetched.get().getPnr());
        assertEquals("Sara Khan", fetched.get().getPassengerName());
        assertEquals(102, fetched.get().getTrainNumber());
        assertEquals("Tezgam Express", fetched.get().getTrainName());
    }

    @Test
    @DisplayName("Fetch booking by unknown or invalid PNR returns empty or throws ValidationException")
    void testFetchUnknownOrInvalidPnr() throws Exception {
        Optional<Reservation> nonExistent = reservationService.fetchBookingByPnr("9999999999");
        assertTrue(nonExistent.isEmpty(), "Unknown PNR should return empty Optional");

        assertThrows(ValidationException.class, () -> reservationService.fetchBookingByPnr("123"),
                "Short PNR should throw ValidationException");
        assertThrows(ValidationException.class, () -> reservationService.fetchBookingByPnr(""),
                "Empty PNR should throw ValidationException");
    }

    @Test
    @DisplayName("Cancellation removes reservation from database and records audit in cancellation_log")
    void testCancelBookingTransaction() throws Exception {
        String journeyDate = LocalDate.now().plusDays(7).toString();
        Reservation booked = reservationService.bookTicket(
                1,
                "Zain Malik",
                "103",
                "Economy",
                journeyDate,
                "Karachi Cantt",
                "Lahore Junction"
        );

        String pnr = booked.getPnr();

        // Verify booking exists before cancellation
        assertTrue(reservationService.fetchBookingByPnr(pnr).isPresent());

        // Perform cancellation
        boolean cancelled = reservationService.cancelBooking(pnr, 1);
        assertTrue(cancelled, "Cancellation should succeed");

        // Verify booking is no longer in reservations table
        Optional<Reservation> afterCancel = reservationService.fetchBookingByPnr(pnr);
        assertTrue(afterCancel.isEmpty(), "Cancelled booking must no longer exist in reservations table");

        // Verify audit row in cancellation_log
        List<CancellationAudit> audits = reservationDao.getCancellationAudits(pnr);
        assertEquals(1, audits.size(), "Exactly one cancellation audit row should be recorded");
        assertEquals(pnr, audits.get(0).getPnr());
        assertEquals(1, audits.get(0).getCancelledBy());
        assertNotNull(audits.get(0).getCancelledAt());

        // Attempting to cancel again should return false (already cancelled)
        boolean secondCancel = reservationService.cancelBooking(pnr, 1);
        assertFalse(secondCancel, "Cancelling already cancelled booking must return false");
    }

    @Test
    @DisplayName("Exporting ticket to text file creates a formatted printable document")
    void testExportTicketToFile() throws Exception {
        String journeyDate = LocalDate.now().plusDays(12).toString();
        Reservation booked = reservationService.bookTicket(
                1,
                "Ayesha Noor",
                "101",
                "AC First",
                journeyDate,
                "Karachi Cantt",
                "Islamabad"
        );

        File tempFile = new File("target/test_ticket_" + booked.getPnr() + ".txt");
        try {
            reservationService.exportTicketToFile(booked, tempFile);
            assertTrue(tempFile.exists(), "Export file should exist");
            assertTrue(tempFile.length() > 0, "Export file should not be empty");

            String content = Files.readString(tempFile.toPath());
            assertTrue(content.contains(booked.getPnr()));
            assertTrue(content.contains("Ayesha Noor"));
            assertTrue(content.contains("Green Line Express"));
            assertTrue(content.contains("AC First"));
            assertTrue(content.contains("Karachi Cantt"));
            assertTrue(content.contains("Islamabad"));
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
