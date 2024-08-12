package edu.virginia.sde.hw5;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class BusLineService {
    private final DatabaseDriver databaseDriver;

    public BusLineService(DatabaseDriver databaseDriver) {
        this.databaseDriver = databaseDriver;
    }

    public void addStops(List<Stop> stops) {
        try {
            databaseDriver.connect();
            databaseDriver.addStops(stops);
            databaseDriver.disconnect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addBusLines(List<BusLine> busLines) {
        try {
            databaseDriver.connect();
            databaseDriver.addBusLines(busLines);
            databaseDriver.disconnect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<BusLine> getBusLines() {
        try {
            databaseDriver.connect();
            var busLines = databaseDriver.getBusLines();
            databaseDriver.disconnect();
            return busLines;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Stop> getStops() {
        try {
            databaseDriver.connect();
            var stops = databaseDriver.getAllStops();
            databaseDriver.disconnect();
            return stops;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Route getRoute(BusLine busLine) {
        try {
            databaseDriver.connect();
            var stops = databaseDriver.getRouteForBusLine(busLine);
            databaseDriver.disconnect();
            return stops;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return the closest stop to a given coordinate (using Euclidean distance, not great circle distance)
     * @param latitude - North/South coordinate (positive is North, Negative is South) in degrees
     * @param longitude - East/West coordinate (negative is West, Positive is East) in degrees
     * @return the closest Stop
     */

    public Stop getClosestStop(double latitude, double longitude) {
        List<Stop> stopsList = getStops();
        if (stopsList.isEmpty()) {
            throw new IllegalArgumentException("No stops found");
        }

        Stop closeStop = stopsList.get(0);
        double closestDistance = closeStop.distanceTo(latitude, longitude);

        for (Stop stop : stopsList) {
            double currentDistance = stop.distanceTo(latitude, longitude);
            if (currentDistance < closestDistance) {
                closestDistance = currentDistance;
                closeStop = stop;
            }
        }

        return closeStop;
    }

    /**
     * Given two stop, a source and a destination, find the shortest (by distance) BusLine that starts
     * from source and ends at Destination.
     * @return Optional.empty() if no bus route visits both points
     * @throws IllegalArgumentException if either stop doesn't exist in the database
     */
    public Optional<BusLine> getRecommendedBusLine(Stop source, Stop destination) {
        List<BusLine> busLines = getBusLines();
        BusLine recBusLine = null;
        double bestDist = Double.MAX_VALUE;

        for (BusLine busLine : busLines) {
            if (busLine.getRoute().getStops().contains(source) && busLine.getRoute().getStops().contains(destination)) {
                double dist = calculateRouteDistance(busLine.getRoute(), source, destination);
                if (dist < bestDist) {
                    bestDist = dist;
                    recBusLine = busLine;
                }
            }
        }

        return Optional.ofNullable(recBusLine);
    }

    private double calculateRouteDistance(Route route, Stop source, Stop destination) {
        List<Stop> stops = route.getStops();
        int sIndex = stops.indexOf(source);
        int dIndex = stops.indexOf(destination);
        double totalDistance = 0.0;

        for (int i = sIndex; i != dIndex; i = (i + 1) % stops.size()) {
            if (i == stops.size() - 1 && dIndex != 0) {
                i = -1;
            }
            else {
                Stop curStop = stops.get(i);
                Stop nextStop = stops.get((i + 1) % stops.size());
                totalDistance += curStop.distanceTo(nextStop.getLatitude(), nextStop.getLongitude());
                if (i == dIndex - 1) break;
            }
        }

        return totalDistance;
    }
}
