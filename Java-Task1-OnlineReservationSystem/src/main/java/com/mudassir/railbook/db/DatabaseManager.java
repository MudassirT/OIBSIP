package com.mudassir.railbook.db;

import com.mudassir.railbook.config.Constants;
import com.mudassir.railbook.model.Train;
import com.mudassir.railbook.service.PasswordUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages database connectivity, schema creation, PRAGMA configuration,
 * and automatic seed data population.
 *
 * @author Mudassir Ahmed
 */
public class DatabaseManager {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());

    private final String jdbcUrl;

    public DatabaseManager() {
        this(Constants.JDBC_SQLITE_PREFIX + Constants.DEFAULT_DB_PATH);
    }

    public DatabaseManager(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    /**
     * Obtains a connection to the SQLite database with foreign keys enabled.
     *
     * @return active {@link Connection}
     * @throws SQLException if a connection cannot be established
     */
    public Connection getConnection() throws SQLException {
        ensureDatabaseDirectory();
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return connection;
    }

    /**
     * Ensures parent directories exist if connecting to a file-based database.
     */
    private void ensureDatabaseDirectory() {
        if (jdbcUrl.startsWith(Constants.JDBC_SQLITE_PREFIX)) {
            String filePath = jdbcUrl.substring(Constants.JDBC_SQLITE_PREFIX.length());
            if (!filePath.equals(":memory:") && !filePath.startsWith(":memory:")) {
                File dbFile = new File(filePath);
                File parentDir = dbFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    boolean created = parentDir.mkdirs();
                    if (created) {
                        LOGGER.info("Created database directory: " + parentDir.getAbsolutePath());
                    }
                }
            }
        }
    }

    /**
     * Initializes all database tables and seeds sample data if tables are empty.
     */
    public void initializeDatabase() {
        LOGGER.info("Initializing database schema and seed data...");
        createTables();
        seedInitialData();
        LOGGER.info("Database initialization completed successfully.");
    }

    /**
     * Creates application tables using PreparedStatement / Statement with DDL.
     */
    private void createTables() {
        String createUsersSql = "CREATE TABLE IF NOT EXISTS " + Constants.TABLE_USERS + " (" +
                Constants.COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Constants.COL_USER_USERNAME + " TEXT UNIQUE NOT NULL, " +
                Constants.COL_USER_PASSWORD_HASH + " TEXT NOT NULL, " +
                Constants.COL_USER_SALT + " TEXT NOT NULL" +
                ");";

        String createStationsSql = "CREATE TABLE IF NOT EXISTS " + Constants.TABLE_STATIONS + " (" +
                Constants.COL_STATION_NAME + " TEXT PRIMARY KEY" +
                ");";

        String createTrainsSql = "CREATE TABLE IF NOT EXISTS " + Constants.TABLE_TRAINS + " (" +
                Constants.COL_TRAIN_NUMBER + " INTEGER PRIMARY KEY, " +
                Constants.COL_TRAIN_NAME + " TEXT NOT NULL, " +
                Constants.COL_TRAIN_SOURCE + " TEXT NOT NULL, " +
                Constants.COL_TRAIN_DESTINATION + " TEXT NOT NULL" +
                ");";

        String createReservationsSql = "CREATE TABLE IF NOT EXISTS " + Constants.TABLE_RESERVATIONS + " (" +
                Constants.COL_RES_PNR + " TEXT PRIMARY KEY, " +
                Constants.COL_RES_USER_ID + " INTEGER NOT NULL, " +
                Constants.COL_RES_PASSENGER_NAME + " TEXT NOT NULL, " +
                Constants.COL_RES_TRAIN_NUMBER + " INTEGER NOT NULL, " +
                Constants.COL_RES_CLASS_TYPE + " TEXT NOT NULL, " +
                Constants.COL_RES_JOURNEY_DATE + " TEXT NOT NULL, " +
                Constants.COL_RES_SOURCE + " TEXT NOT NULL, " +
                Constants.COL_RES_DESTINATION + " TEXT NOT NULL, " +
                Constants.COL_RES_BOOKED_AT + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + Constants.COL_RES_USER_ID + ") REFERENCES " + Constants.TABLE_USERS + "(" + Constants.COL_USER_ID + "), " +
                "FOREIGN KEY (" + Constants.COL_RES_TRAIN_NUMBER + ") REFERENCES " + Constants.TABLE_TRAINS + "(" + Constants.COL_TRAIN_NUMBER + ")" +
                ");";

        String createCancellationLogSql = "CREATE TABLE IF NOT EXISTS " + Constants.TABLE_CANCELLATION_LOG + " (" +
                Constants.COL_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Constants.COL_LOG_PNR + " TEXT NOT NULL, " +
                Constants.COL_LOG_CANCELLED_BY + " INTEGER NOT NULL, " +
                Constants.COL_LOG_CANCELLED_AT + " TEXT NOT NULL" +
                ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersSql);
            stmt.execute(createStationsSql);
            stmt.execute(createTrainsSql);
            stmt.execute(createReservationsSql);
            stmt.execute(createCancellationLogSql);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create database schema: " + e.getMessage(), e);
            throw new RuntimeException("Database schema creation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Seeds initial sample users, stations, and trains if not already present.
     */
    private void seedInitialData() {
        seedStations();
        seedTrains();
        seedUsers();
    }

    private void seedStations() {
        String countSql = "SELECT COUNT(*) FROM " + Constants.TABLE_STATIONS;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(countSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) {
                return; // already seeded
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking stations count", e);
            return;
        }

        List<String> stations = Arrays.asList(
                "Karachi Cantt",
                "Lahore Junction",
                "Rawalpindi",
                "Islamabad",
                "Peshawar Cantt",
                "Quetta",
                "Multan Cantt",
                "Faisalabad",
                "Bahawalpur",
                "Hyderabad Junction",
                "Rohri Junction",
                "Sialkot Junction"
        );

        String insertSql = "INSERT OR IGNORE INTO " + Constants.TABLE_STATIONS + " (" + Constants.COL_STATION_NAME + ") VALUES (?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (String station : stations) {
                    ps.setString(1, station);
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
                LOGGER.info("Seeded " + stations.size() + " sample stations.");
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error seeding stations", e);
        }
    }

    private void seedTrains() {
        String countSql = "SELECT COUNT(*) FROM " + Constants.TABLE_TRAINS;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(countSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) {
                return; // already seeded
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking trains count", e);
            return;
        }

        List<Train> sampleTrains = Arrays.asList(
                new Train(101, "Green Line Express", "Karachi Cantt", "Islamabad"),
                new Train(102, "Tezgam Express", "Karachi Cantt", "Rawalpindi"),
                new Train(103, "Karakoram Express", "Karachi Cantt", "Lahore Junction"),
                new Train(104, "Awam Express", "Karachi Cantt", "Peshawar Cantt"),
                new Train(105, "Jaffar Express", "Quetta", "Peshawar Cantt"),
                new Train(106, "Pak Business Express", "Karachi Cantt", "Lahore Junction"),
                new Train(107, "Subak Raftar", "Lahore Junction", "Rawalpindi"),
                new Train(108, "Bolan Mail", "Karachi Cantt", "Quetta"),
                new Train(109, "Millat Express", "Karachi Cantt", "Faisalabad"),
                new Train(110, "Rehman Baba Express", "Karachi Cantt", "Peshawar Cantt")
        );

        String insertSql = "INSERT OR IGNORE INTO " + Constants.TABLE_TRAINS + " (" +
                Constants.COL_TRAIN_NUMBER + ", " +
                Constants.COL_TRAIN_NAME + ", " +
                Constants.COL_TRAIN_SOURCE + ", " +
                Constants.COL_TRAIN_DESTINATION + ") VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (Train train : sampleTrains) {
                    ps.setInt(1, train.getTrainNumber());
                    ps.setString(2, train.getTrainName());
                    ps.setString(3, train.getSourceStation());
                    ps.setString(4, train.getDestinationStation());
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
                LOGGER.info("Seeded " + sampleTrains.size() + " sample trains.");
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error seeding trains", e);
        }
    }

    private void seedUsers() {
        String countSql = "SELECT COUNT(*) FROM " + Constants.TABLE_USERS;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(countSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) {
                return; // already seeded
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking users count", e);
            return;
        }

        String insertSql = "INSERT OR IGNORE INTO " + Constants.TABLE_USERS + " (" +
                Constants.COL_USER_USERNAME + ", " +
                Constants.COL_USER_PASSWORD_HASH + ", " +
                Constants.COL_USER_SALT + ") VALUES (?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                // Seed admin user
                String adminSalt = PasswordUtil.generateSalt();
                String adminHash = PasswordUtil.hashPassword("Admin@123", adminSalt);
                ps.setString(1, "admin");
                ps.setString(2, adminHash);
                ps.setString(3, adminSalt);
                ps.addBatch();

                // Seed demo user
                String demoSalt = PasswordUtil.generateSalt();
                String demoHash = PasswordUtil.hashPassword("Demo@123", demoSalt);
                ps.setString(1, "demo");
                ps.setString(2, demoHash);
                ps.setString(3, demoSalt);
                ps.addBatch();

                ps.executeBatch();
                conn.commit();
                LOGGER.info("Seeded demo users: 'admin' and 'demo'.");
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error seeding demo users", e);
        }
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }
}
