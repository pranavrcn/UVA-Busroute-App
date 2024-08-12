package edu.virginia.sde.hw5;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class DatabaseDriver {
    private final String sqliteFilename;
    private Connection connection;

    public DatabaseDriver(Configuration configuration) {
        this.sqliteFilename = configuration.getDatabaseFilename();
    }

    public DatabaseDriver(String sqlListDatabaseFilename) {
        this.sqliteFilename = sqlListDatabaseFilename;
    }

    /**
     * Connect to a SQLite Database. This turns out Foreign Key enforcement, and disables auto-commits
     *
     * @throws SQLException
     */
    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            throw new IllegalStateException("The connection is already opened");
        }
        connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFilename);
        //the next line enables foreign key enforcement - do not delete/comment out
        connection.createStatement().execute("PRAGMA foreign_keys = ON");
        //the next line disables auto-commit - do not delete/comment out
        connection.setAutoCommit(false);
    }

    /**
     * Commit all changes since the connection was opened OR since the last commit/rollback
     */
    public void commit() throws SQLException {
        connection.commit();
    }

    /**
     * Rollback to the last commit, or when the connection was opened
     */
    public void rollback() throws SQLException {
        connection.rollback();
    }

    /**
     * Ends the connection to the database
     */
    public void disconnect() throws SQLException {
        connection.close();
    }

    /**
     * Creates the three database tables Stops, BusLines, and Routes, with the appropriate constraints including
     * foreign keys, if they do not exist already. If they already exist, this method does nothing.
     * As a hint, you'll need to create Routes last, and Routes must include Foreign Keys to Stops and
     * BusLines.
     *
     * @throws SQLException
     */
    public void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS Stops (" + // Set up stops table in database
                    "ID INTEGER PRIMARY KEY, " +
                    "StopName TEXT NOT NULL, " +
                    "Latitude REAL NOT NULL, " +
                    "Longitude REAL NOT NULL)");

            statement.execute("CREATE TABLE IF NOT EXISTS BusLines (" + // Set up bus lines table in database
                    "ID INTEGER PRIMARY KEY, " +
                    "IsActive BOOLEAN NOT NULL, " +
                    "LongName TEXT NOT NULL, " +
                    "ShortName TEXT NOT NULL)");

            statement.execute("CREATE TABLE IF NOT EXISTS Routes (" + // Set up routes table in database
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " + // https://www.sqlite.org/autoinc.html
                    "BusLineID INTEGER NOT NULL, " +
                    "StopID INTEGER NOT NULL, " +
                    "RouteOrder INTEGER NOT NULL, " +
                    "FOREIGN KEY (BusLineID) REFERENCES BusLines(ID) ON DELETE CASCADE, " +
                    "FOREIGN KEY (StopID) REFERENCES Stops(ID) ON DELETE CASCADE)");
        }
    }

    /**
     * Add a list of Stops to the Database. After adding all the stops, the changes will be committed. However,
     * if any SQLExceptions occur, this method will rollback and throw the exception.
     *
     * @param stops- the stops to be added to the database
     */
    public void addStops(List<Stop> stops) throws SQLException {

        // Insert read in values accordingly into columns.
        String sqlStatement = "INSERT INTO Stops (ID, StopName, Latitude, Longitude) VALUES (?, ?, ?, ?)";
        try (PreparedStatement prepStmt = connection.prepareStatement(sqlStatement)) {
            for (Stop stop : stops) {
                prepStmt.setInt(1, stop.getId());
                prepStmt.setString(2, stop.getName());
                prepStmt.setDouble(3, stop.getLatitude());
                prepStmt.setDouble(4, stop.getLongitude());
                prepStmt.executeUpdate();
            }
        } catch (SQLException e) { // If issue occurs, rollback and throw exception
            rollback();
            throw e;
        }
    }

    /**
     * Gets a list of all Stops in the database
     */

    public List<Stop> getAllStops() throws SQLException {
        List<Stop> stopCollection = new ArrayList<>(); // Initialize array list to put all stops

        // Select all stops using SQL statement and add to initialized array list
        String sqlSelectAllStops = "SELECT * FROM Stops";
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sqlSelectAllStops)) {
            while (result.next()) {
                stopCollection.add(new Stop(result.getInt("ID"), result.getString("StopName"),
                        result.getDouble("Latitude"), result.getDouble("Longitude")));
            }
        }
        return stopCollection;
    }

    /**
     * Get a Stop by its ID number. Returns Optional.isEmpty() if no Stop matches the ID.
     */
    public Optional<Stop> getStopById(int stopId) throws SQLException {

        String sqlSelectStop = "SELECT * FROM Stops WHERE ID = ?";
        try (PreparedStatement prepStmt = connection.prepareStatement(sqlSelectStop)) {
            prepStmt.setInt(1, stopId);
            ResultSet result = prepStmt.executeQuery();

            // Add the stop that matches the ID number
            if (result.next()) {
                Stop foundStop = new Stop(result.getInt("ID"), result.getString("StopName"),
                        result.getDouble("Latitude"), result.getDouble("Longitude"));
                return Optional.of(foundStop);
            }
        }
        return Optional.empty();
    }

    /**
     * Get all Stops whose name contains the substring (case-insensitive). For example, the parameter "Rice"
     * would return a List of Stops containing "Whitehead Rd @ Rice Hall"
     */
    public List<Stop> getStopsByName(String subString) throws SQLException {
        List<Stop> foundStops = new ArrayList<>();
        String sqlSearchStops = "SELECT * FROM Stops WHERE StopName LIKE ?";
        try (PreparedStatement prepStmt = connection.prepareStatement(sqlSearchStops)) {
            prepStmt.setString(1, "%" + subString + "%"); // Correct format of string for SQL
            ResultSet result = prepStmt.executeQuery();

            // Add found stops
            while (result.next()) {
                foundStops.add(new Stop(result.getInt("ID"), result.getString("StopName"),
                        result.getDouble("Latitude"), result.getDouble("Longitude")));
            }
        }
        return foundStops;
    }

    /**
     * Add BusLines and their Routes to the database, including their routes. This method should only be called after
     * Stops are added to the database via addStops, since Routes depends on the StopIds already being
     * in the database. If any SQLExceptions occur, this method will rollback all changes since
     * the method was called. This could happen if, for example, a BusLine contains a Stop that is not in the database.
     */
    public void addBusLines(List<BusLine> busLineList) throws SQLException {

        String sqlInsertBusLine = "INSERT INTO BusLines (ID, IsActive, LongName, ShortName) VALUES (?, ?, ?, ?)";
        String sqlInsertRoute = "INSERT INTO Routes (BusLineID, StopID, RouteOrder) VALUES (?, ?, ?)";
        try (PreparedStatement prepStmtBusLine = connection.prepareStatement(sqlInsertBusLine);
             PreparedStatement prepStmtRoute = connection.prepareStatement(sqlInsertRoute)) {
            for (BusLine line : busLineList) {
                prepStmtBusLine.setInt(1, line.getId());
                prepStmtBusLine.setBoolean(2, line.isActive());
                prepStmtBusLine.setString(3, line.getLongName());
                prepStmtBusLine.setString(4, line.getShortName());
                prepStmtBusLine.executeUpdate();

                int orderIndex = 0;
                for (Stop routeStop : line.getRoute().getStops()) {
                    prepStmtRoute.setInt(1, line.getId());
                    prepStmtRoute.setInt(2, routeStop.getId());
                    prepStmtRoute.setInt(3, orderIndex++);
                    prepStmtRoute.executeUpdate();
                }
            }
        } catch (SQLException e) {
            rollback();
            throw e;
        }
    }


    /**
     * Return a list of all BusLines
     */
    public List<BusLine> getBusLines() throws SQLException {
        List<BusLine> busLineCollection = new ArrayList<>();
        String sqlSelectBusLines = "SELECT * FROM BusLines";
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sqlSelectBusLines)) {
            while (result.next()) {
                busLineCollection.add(new BusLine(result.getInt("ID"), result.getBoolean("IsActive"),
                        result.getString("LongName"), result.getString("ShortName")));
            }
        }
        return busLineCollection;
    }


    /**
     * Get a BusLine by its id number. Return Optional.empty() if no busLine is found
     */
    public Optional<BusLine> getBusLineById(int busLineId) throws SQLException {
        String sqlSelectBusLine = "SELECT * FROM BusLines WHERE ID = ?";
        try (PreparedStatement prepStmt = connection.prepareStatement(sqlSelectBusLine)) {
            prepStmt.setInt(1, busLineId);
            ResultSet result = prepStmt.executeQuery();
            if (result.next()) {
                return Optional.of(new BusLine(result.getInt("ID"), result.getBoolean("IsActive"),
                        result.getString("LongName"), result.getString("ShortName")));
            }
        }
        return Optional.empty();
    }


    /**
     * Get BusLine by its full long name (case-insensitive). Return Optional.empty() if no busLine is found.
     */
    public Optional<BusLine> getBusLineByLongName(String longName) throws SQLException {
        String sqlSelectBusLineByName = "SELECT * FROM BusLines WHERE LOWER(LongName) = LOWER(?)";
        try (PreparedStatement prepStmt = connection.prepareStatement(sqlSelectBusLineByName)) {
            prepStmt.setString(1, longName);
            ResultSet result = prepStmt.executeQuery();
            if (result.next()) {
                return Optional.of(new BusLine(result.getInt("ID"), result.getBoolean("IsActive"),
                        result.getString("LongName"), result.getString("ShortName")));
            }
        }
        return Optional.empty();
    }


    /**
     * Get BusLine by its full short name (case-insensitive). Return Optional.empty() if no busLine is found.
     */
    public Optional<BusLine> getBusLineByShortName(String shortName) throws SQLException {
        String sql = "SELECT * FROM BusLines WHERE LOWER(ShortName) = LOWER(?)";
        try (PreparedStatement prepStmt = connection.prepareStatement(sql)) {
            prepStmt.setString(1, shortName);
            ResultSet rs = prepStmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new BusLine(rs.getInt("ID"), rs.getBoolean("IsActive"),
                        rs.getString("LongName"), rs.getString("ShortName")));
            }
        }
        return Optional.empty();
    }


    /**
     * Get all BusLines that visit a particular stop
     */
    public List<BusLine> getBusLinesByStop(Stop targetStop) throws SQLException {
        List<BusLine> busLinesAtStop = new ArrayList<>();
        String sqlFindBusLinesByStop = "SELECT DISTINCT b.* FROM BusLines b " +
                "JOIN Routes r ON b.ID = r.BusLineID " +
                "WHERE r.StopID = ?";
        try (PreparedStatement prepStmt = connection.prepareStatement(sqlFindBusLinesByStop)) {
            prepStmt.setInt(1, targetStop.getId());
            ResultSet result = prepStmt.executeQuery();
            while (result.next()) {
                busLinesAtStop.add(new BusLine(result.getInt("ID"), result.getBoolean("IsActive"),
                        result.getString("LongName"), result.getString("ShortName")));
            }
        }
        return busLinesAtStop;
    }


    /**
     * Returns a BusLine's route as a List of stops *in-order*
     *
     * @param busLine
     * @throws SQLException
     * @throws java.util.NoSuchElementException if busLine is not in the database
     */
    public Route getRouteForBusLine(BusLine busLine) throws SQLException {

        // Check if the argument bus line exists
        String sqlCheckBusLine = "SELECT COUNT(*) AS count FROM BusLines WHERE ID = ?";
        try (PreparedStatement prepStmtCheck = connection.prepareStatement(sqlCheckBusLine)) {
            prepStmtCheck.setInt(1, busLine.getId());
            ResultSet rsCheck = prepStmtCheck.executeQuery();

        }

        // Get stops in order
        String sql = "SELECT Stops.* FROM Stops " +
                "JOIN Routes ON Stops.ID = Routes.StopID " +
                "WHERE Routes.BusLineID = ? " +
                "ORDER BY Routes.RouteOrder";
        List<Stop> stops = new ArrayList<>();
        try (PreparedStatement prepStmt = connection.prepareStatement(sql)) {
            prepStmt.setInt(1, busLine.getId());
            ResultSet rs = prepStmt.executeQuery();
            while (rs.next()) {
                stops.add(new Stop(rs.getInt("ID"), rs.getString("StopName"),
                        rs.getDouble("Latitude"), rs.getDouble("Longitude")));
            }
        }

        return new Route(stops);
    }


    /**
     * Removes all data from the tables, leaving the tables empty (but still existing!). As a hint, delete the
     * contents of Routes first in order to avoid violating foreign key constraints.
     */
    public void clearTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM Routes");
            statement.execute("DELETE FROM BusLines");
            statement.execute("DELETE FROM Stops");
        }

    }
}
