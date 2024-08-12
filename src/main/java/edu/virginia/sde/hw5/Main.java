package edu.virginia.sde.hw5;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            Configuration config = new Configuration();
            URL stopsUrl = config.getBusStopsURL();
            URL busLinesUrl = config.getBusLinesURL();
            String databaseFilename = config.getDatabaseFilename();

            WebServiceReader stopsReader = new WebServiceReader(stopsUrl);
            WebServiceReader busLinesReader = new WebServiceReader(busLinesUrl);

            JSONObject stopsJson = stopsReader.getJSONObject();
            JSONObject busLinesJson = busLinesReader.getJSONObject();

            List<Stop> stops = parseStops(stopsJson);
            List<BusLine> busLines = parseBusLines(busLinesJson, stopsJson);

            DatabaseDriver dbDriver = new DatabaseDriver(databaseFilename);
            dbDriver.connect();
            dbDriver.clearTables();
            dbDriver.createTables();

            dbDriver.addStops(stops);
            dbDriver.addBusLines(busLines);
            dbDriver.commit();
            dbDriver.disconnect();

            System.out.println("Database initialized and data inserted successfully.");
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<Stop> parseStops(JSONObject stopsJson) {
        List<Stop> stops = new ArrayList<>();
        JSONArray stopsArray = stopsJson.getJSONArray("stops");
        for (int i = 0; i < stopsArray.length(); i++) {
            JSONObject stop = stopsArray.getJSONObject(i);
            JSONArray position = stop.getJSONArray("position");
            double latitude = position.getDouble(0);
            double longitude = position.getDouble(1);

            stops.add(new Stop(
                    stop.getInt("id"),
                    stop.getString("name"),
                    latitude,
                    longitude
            ));
        }
        return stops;
    }

    private static List<BusLine> parseBusLines(JSONObject busLinesJson, JSONObject routesJson) {
        List<BusLine> busLines = new ArrayList<>();
        JSONArray busLinesArray = busLinesJson.getJSONArray("lines");
        Map<Integer, List<Integer>> routeMap = new HashMap<>();

        JSONArray routesArray = routesJson.getJSONArray("routes");
        for (int i = 0; i < routesArray.length(); i++) {
            JSONObject route = routesArray.getJSONObject(i);
            int busLineId = route.getInt("id");
            JSONArray stopIds = route.getJSONArray("stops");
            List<Integer> stops = new ArrayList<>();
            for (int j = 0; j < stopIds.length(); j++) {
                stops.add(stopIds.getInt(j));
            }
            routeMap.put(busLineId, stops);
        }

        for (int i = 0; i < busLinesArray.length(); i++) {
            JSONObject busLine = busLinesArray.getJSONObject(i);
            int id = busLine.getInt("id");
            boolean isActive = busLine.getBoolean("is_active");
            String longName = busLine.getString("long_name");
            String shortName = busLine.getString("short_name");

            List<Integer> stopsIds = routeMap.getOrDefault(id, new ArrayList<>());
            List<Stop> lineStops = new ArrayList<>();
            for (Integer stopId : stopsIds) {
                lineStops.add(new Stop(stopId, "", 0, 0)); // Dummy Stop objects
            }
            Route route = new Route(lineStops);

            busLines.add(new BusLine(
                    id,
                    isActive,
                    longName,
                    shortName,
                    route
            ));
        }
        return busLines;
    }
}
