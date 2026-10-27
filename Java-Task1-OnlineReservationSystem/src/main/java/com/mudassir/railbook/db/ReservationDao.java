package com.mudassir.railbook.db;

import com.mudassir.railbook.config.Constants;
import com.mudassir.railbook.model.CancellationAudit;
import com.mudassir.railbook.model.Reservation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Reservations and Cancellation Audits.
 * Implements strict ACID transactional cancellation with rollback on failure.
 *
 * @author Mudassir Ahmed
 */
public class ReservationDao {

    private static final Logger LOGGER = Logger.getLogger(ReservationDao.class.getName());
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(Constants.TIMESTAMP_FORMAT);

    private final DatabaseManager databaseManager;

    public ReservationDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Persists a new reservation record.
     *
     * @param reservation reservation to persist
     * @throws SQLException on database failure or constraint violation (e.g. duplicate PNR)
     */
    public void save(Reservation reservation) throws SQLException {
        String sql = "INSERT INTO " + Constants.TABLE_RESERVATIONS + " (" +
                Constants.COL_RES_PNR + ", " +
                Constants.COL_RES_USER_ID + ", " +
                Constants.COL_RES_PASSENGER_NAME + ", " +
                Constants.COL_RES_TRAIN_NUMBER + ", " +
                Constants.COL_RES_CLASS_TYPE + ", " +
                Constants.COL_RES_JOURNEY_DATE + ", " +
                Constants.COL_RES_SOURCE + ", " +
                Constants.COL_RES_DESTINATION + ", " +
                Constants.COL_RES_BOOKED_AT + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reservation.getPnr());
            ps.setInt(2, reservation.getUserId());
            ps.setString(3, reservation.getPassengerName());
            ps.setInt(4, reservation.getTrainNumber());
            ps.setString(5, reservation.getClassType());
            ps.setString(6, reservation.getJourneyDate());
            ps.setString(7, reservation.getSource());
            ps.setString(8, reservation.getDestination());
            ps.setString(9, reservation.getBookedAt());
            ps.executeUpdate();
            LOGGER.info("Successfully saved reservation with PNR: " + reservation.getPnr());
        }
    }

    /**
     * Finds a reservation by its 10-digit PNR, joining with trains table for the train name.
     *
     * @param pnr 10-digit PNR string
     * @return Optional of Reservation if found, empty otherwise
     */
    public Optional<Reservation> findByPnr(String pnr) {
        if (pnr == null || pnr.isBlank()) {
            return Optional.empty();
        }

        String sql = "SELECT r." + Constants.COL_RES_PNR + ", " +
                "r." + Constants.COL_RES_USER_ID + ", " +
                "r." + Constants.COL_RES_PASSENGER_NAME + ", " +
                "r." + Constants.COL_RES_TRAIN_NUMBER + ", " +
                "t." + Constants.COL_TRAIN_NAME + ", " +
                "r." + Constants.COL_RES_CLASS_TYPE + ", " +
                "r." + Constants.COL_RES_JOURNEY_DATE + ", " +
                "r." + Constants.COL_RES_SOURCE + ", " +
                "r." + Constants.COL_RES_DESTINATION + ", " +
                "r." + Constants.COL_RES_BOOKED_AT +
                " FROM " + Constants.TABLE_RESERVATIONS + " r " +
                " LEFT JOIN " + Constants.TABLE_TRAINS + " t " +
                " ON r." + Constants.COL_RES_TRAIN_NUMBER + " = t." + Constants.COL_TRAIN_NUMBER +
                " WHERE r." + Constants.COL_RES_PNR + " = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pnr.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Reservation reservation = mapResultSetToReservation(rs);
                    return Optional.of(reservation);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding reservation by PNR: " + pnr, e);
        }
        return Optional.empty();
    }

    /**
     * Checks if a PNR already exists in the reservations table.
     *
     * @param pnr PNR to check
     * @return true if exists, false otherwise
     */
    public boolean isPnrExists(String pnr) {
        String sql = "SELECT 1 FROM " + Constants.TABLE_RESERVATIONS +
                " WHERE " + Constants.COL_RES_PNR + " = ? LIMIT 1";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pnr);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking PNR existence: " + pnr, e);
            return false;
        }
    }

    /**
     * Retrieves all reservations booked by a specific user.
     *
     * @param userId user ID
     * @return list of reservations, newest first
     */
    public List<Reservation> findByUserId(int userId) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT r." + Constants.COL_RES_PNR + ", " +
                "r." + Constants.COL_RES_USER_ID + ", " +
                "r." + Constants.COL_RES_PASSENGER_NAME + ", " +
                "r." + Constants.COL_RES_TRAIN_NUMBER + ", " +
                "t." + Constants.COL_TRAIN_NAME + ", " +
                "r." + Constants.COL_RES_CLASS_TYPE + ", " +
                "r." + Constants.COL_RES_JOURNEY_DATE + ", " +
                "r." + Constants.COL_RES_SOURCE + ", " +
                "r." + Constants.COL_RES_DESTINATION + ", " +
                "r." + Constants.COL_RES_BOOKED_AT +
                " FROM " + Constants.TABLE_RESERVATIONS + " r " +
                " LEFT JOIN " + Constants.TABLE_TRAINS + " t " +
                " ON r." + Constants.COL_RES_TRAIN_NUMBER + " = t." + Constants.COL_TRAIN_NUMBER +
                " WHERE r." + Constants.COL_RES_USER_ID + " = ? " +
                " ORDER BY r." + Constants.COL_RES_BOOKED_AT + " DESC";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToReservation(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving reservations for user id: " + userId, e);
        }
        return list;
    }

    /**
     * Cancels a reservation inside a transaction.
     * Deletes the reservation row and writes an audit row to the cancellation_log table.
     *
     * @param pnr                PNR to cancel
     * @param cancelledByUserId  user ID executing the cancellation
     * @return true if successfully cancelled, false if reservation did not exist
     * @throws SQLException on database transaction failure
     */
    public boolean cancelReservation(String pnr, int cancelledByUserId) throws SQLException {
        if (pnr == null || pnr.isBlank()) {
            return false;
        }

        String deleteSql = "DELETE FROM " + Constants.TABLE_RESERVATIONS +
                " WHERE " + Constants.COL_RES_PNR + " = ?";

        String logSql = "INSERT INTO " + Constants.TABLE_CANCELLATION_LOG + " (" +
                Constants.COL_LOG_PNR + ", " +
                Constants.COL_LOG_CANCELLED_BY + ", " +
                Constants.COL_LOG_CANCELLED_AT + ") VALUES (?, ?, ?)";

        try (Connection conn = databaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int affectedRows;
                try (PreparedStatement psDelete = conn.prepareStatement(deleteSql)) {
                    psDelete.setString(1, pnr.trim());
                    affectedRows = psDelete.executeUpdate();
                }

                if (affectedRows == 0) {
                    conn.rollback();
                    LOGGER.warning("Cancellation attempted on non-existent PNR: " + pnr);
                    return false;
                }

                try (PreparedStatement psLog = conn.prepareStatement(logSql)) {
                    psLog.setString(1, pnr.trim());
                    psLog.setInt(2, cancelledByUserId);
                    psLog.setString(3, LocalDateTime.now().format(TIMESTAMP_FORMATTER));
                    psLog.executeUpdate();
                }

                conn.commit();
                LOGGER.info("Cancelled reservation and logged audit for PNR: " + pnr);
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                LOGGER.log(Level.SEVERE, "Transaction rollback during cancellation of PNR: " + pnr, ex);
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Retrieves cancellation audit logs for tests and auditing.
     *
     * @param pnr PNR to check
     * @return list of audit records
     */
    public List<CancellationAudit> getCancellationAudits(String pnr) {
        List<CancellationAudit> audits = new ArrayList<>();
        String sql = "SELECT " + Constants.COL_LOG_ID + ", " +
                Constants.COL_LOG_PNR + ", " +
                Constants.COL_LOG_CANCELLED_BY + ", " +
                Constants.COL_LOG_CANCELLED_AT +
                " FROM " + Constants.TABLE_CANCELLATION_LOG +
                " WHERE " + Constants.COL_LOG_PNR + " = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pnr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    audits.add(new CancellationAudit(
                            rs.getInt(Constants.COL_LOG_ID),
                            rs.getString(Constants.COL_LOG_PNR),
                            rs.getInt(Constants.COL_LOG_CANCELLED_BY),
                            rs.getString(Constants.COL_LOG_CANCELLED_AT)
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying cancellation audits for PNR: " + pnr, e);
        }
        return audits;
    }

    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        return new Reservation(
                rs.getString(Constants.COL_RES_PNR),
                rs.getInt(Constants.COL_RES_USER_ID),
                rs.getString(Constants.COL_RES_PASSENGER_NAME),
                rs.getInt(Constants.COL_RES_TRAIN_NUMBER),
                rs.getString(Constants.COL_TRAIN_NAME),
                rs.getString(Constants.COL_RES_CLASS_TYPE),
                rs.getString(Constants.COL_RES_JOURNEY_DATE),
                rs.getString(Constants.COL_RES_SOURCE),
                rs.getString(Constants.COL_RES_DESTINATION),
                rs.getString(Constants.COL_RES_BOOKED_AT)
        );
    }
}
