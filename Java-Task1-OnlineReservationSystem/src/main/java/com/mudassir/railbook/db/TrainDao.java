package com.mudassir.railbook.db;

import com.mudassir.railbook.config.Constants;
import com.mudassir.railbook.model.Train;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Trains and Stations.
 *
 * @author Mudassir Ahmed
 */
public class TrainDao {

    private static final Logger LOGGER = Logger.getLogger(TrainDao.class.getName());
    private final DatabaseManager databaseManager;

    public TrainDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Looks up a train by its unique train number.
     *
     * @param trainNumber numeric train identifier
     * @return Optional containing the Train if found, empty otherwise
     */
    public Optional<Train> findByTrainNumber(int trainNumber) {
        String sql = "SELECT " + Constants.COL_TRAIN_NUMBER + ", " +
                Constants.COL_TRAIN_NAME + ", " +
                Constants.COL_TRAIN_SOURCE + ", " +
                Constants.COL_TRAIN_DESTINATION +
                " FROM " + Constants.TABLE_TRAINS +
                " WHERE " + Constants.COL_TRAIN_NUMBER + " = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trainNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Train train = new Train(
                            rs.getInt(Constants.COL_TRAIN_NUMBER),
                            rs.getString(Constants.COL_TRAIN_NAME),
                            rs.getString(Constants.COL_TRAIN_SOURCE),
                            rs.getString(Constants.COL_TRAIN_DESTINATION)
                    );
                    return Optional.of(train);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying train by number: " + trainNumber, e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves all available trains sorted by train number.
     *
     * @return list of trains
     */
    public List<Train> getAllTrains() {
        List<Train> trains = new ArrayList<>();
        String sql = "SELECT " + Constants.COL_TRAIN_NUMBER + ", " +
                Constants.COL_TRAIN_NAME + ", " +
                Constants.COL_TRAIN_SOURCE + ", " +
                Constants.COL_TRAIN_DESTINATION +
                " FROM " + Constants.TABLE_TRAINS +
                " ORDER BY " + Constants.COL_TRAIN_NUMBER + " ASC";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                trains.add(new Train(
                        rs.getInt(Constants.COL_TRAIN_NUMBER),
                        rs.getString(Constants.COL_TRAIN_NAME),
                        rs.getString(Constants.COL_TRAIN_SOURCE),
                        rs.getString(Constants.COL_TRAIN_DESTINATION)
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all trains", e);
        }
        return trains;
    }

    /**
     * Retrieves all station names sorted alphabetically.
     *
     * @return list of station names
     */
    public List<String> getAllStationNames() {
        List<String> stations = new ArrayList<>();
        String sql = "SELECT " + Constants.COL_STATION_NAME +
                " FROM " + Constants.TABLE_STATIONS +
                " ORDER BY " + Constants.COL_STATION_NAME + " ASC";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                stations.add(rs.getString(Constants.COL_STATION_NAME));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving stations", e);
        }
        return stations;
    }

    /**
     * Persists a train entity.
     *
     * @param train train to save
     * @throws SQLException on database failure
     */
    public void save(Train train) throws SQLException {
        String sql = "INSERT INTO " + Constants.TABLE_TRAINS + " (" +
                Constants.COL_TRAIN_NUMBER + ", " +
                Constants.COL_TRAIN_NAME + ", " +
                Constants.COL_TRAIN_SOURCE + ", " +
                Constants.COL_TRAIN_DESTINATION + ") VALUES (?, ?, ?, ?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, train.getTrainNumber());
            ps.setString(2, train.getTrainName());
            ps.setString(3, train.getSourceStation());
            ps.setString(4, train.getDestinationStation());
            ps.executeUpdate();
        }
    }

    /**
     * Persists a new station name.
     *
     * @param stationName station name
     * @throws SQLException on database failure
     */
    public void saveStation(String stationName) throws SQLException {
        String sql = "INSERT INTO " + Constants.TABLE_STATIONS + " (" +
                Constants.COL_STATION_NAME + ") VALUES (?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stationName);
            ps.executeUpdate();
        }
    }
}
