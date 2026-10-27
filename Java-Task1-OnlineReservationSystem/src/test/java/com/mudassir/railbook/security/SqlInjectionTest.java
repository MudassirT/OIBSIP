package com.mudassir.railbook.security;

import com.mudassir.railbook.db.DatabaseManager;
import com.mudassir.railbook.db.ReservationDao;
import com.mudassir.railbook.db.TrainDao;
import com.mudassir.railbook.db.UserDao;
import com.mudassir.railbook.model.Reservation;
import com.mudassir.railbook.model.User;
import com.mudassir.railbook.service.AuthService;
import com.mudassir.railbook.service.AuthenticationException;
import com.mudassir.railbook.service.ReservationService;
import com.mudassir.railbook.service.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Security tests verifying that SQL Injection attacks are completely mitigated
 * across all layers via parameterized PreparedStatements and strict validation.
 *
 * @author Mudassir Ahmed
 */
class SqlInjectionTest {

    private static final String TEST_DB_PATH = "target/test_sqli.db";
    private DatabaseManager dbManager;
    private UserDao userDao;
    private ReservationDao reservationDao;
    private AuthService authService;
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        File dbFile = new File(TEST_DB_PATH);
        if (dbFile.exists()) {
            dbFile.delete();
        }
        dbManager = new DatabaseManager("jdbc:sqlite:" + TEST_DB_PATH);
        dbManager.initializeDatabase();

        userDao = new UserDao(dbManager);
        TrainDao trainDao = new TrainDao(dbManager);
        reservationDao = new ReservationDao(dbManager);

        authService = new AuthService(userDao);
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
    @DisplayName("SQL Injection in username (' OR '1'='1) does not bypass authentication")
    void testSqlInjectionInUsername() {
        String maliciousUsername = "' OR '1'='1";
        String dummyPassword = "password";

        // Querying DAO directly with injection string must safely treat it as a literal string
        Optional<User> userOpt = userDao.findByUsername(maliciousUsername);
        assertTrue(userOpt.isEmpty(), "No user should match literal injection string");

        // Attempting login with SQL injection username must be rejected
        assertThrows(AuthenticationException.class, () ->
                authService.login(maliciousUsername, dummyPassword));
    }

    @Test
    @DisplayName("SQL Injection in username with comment syntax (' OR 1=1 --) is safely rejected")
    void testSqlInjectionWithComment() {
        String maliciousUsername = "admin' --";
        assertThrows(AuthenticationException.class, () ->
                authService.login(maliciousUsername, "wrong"));
    }

    @Test
    @DisplayName("SQL Injection in PNR search (' OR '1'='1) is rejected or treated as literal without syntax error")
    void testSqlInjectionInPnr() {
        String maliciousPnr = "' OR '1'='1";

        // Validation rejects because PNR must be 10 numeric digits
        assertThrows(ValidationException.class, () ->
                reservationService.fetchBookingByPnr(maliciousPnr));

        // Direct DAO query treats malicious PNR as bound parameter without SQL syntax error
        Optional<Reservation> res = reservationDao.findByPnr(maliciousPnr);
        assertTrue(res.isEmpty(), "Literal SQL injection PNR does not return any rows");

        // PNR exists check also treats it as a bound literal
        assertFalse(reservationDao.isPnrExists(maliciousPnr));
    }

    @Test
    @DisplayName("SQL Injection in cancellation PNR (' OR '1'='1) does not delete any rows")
    void testSqlInjectionInCancellation() throws Exception {
        String maliciousPnr = "' OR '1'='1";

        // Validation rejection
        assertThrows(ValidationException.class, () ->
                reservationService.cancelBooking(maliciousPnr, 1));

        // Direct DAO call safely deletes zero rows and rolls back
        boolean result = reservationDao.cancelReservation(maliciousPnr, 1);
        assertFalse(result, "Cancelling malicious PNR should return false without altering data");
    }
}
