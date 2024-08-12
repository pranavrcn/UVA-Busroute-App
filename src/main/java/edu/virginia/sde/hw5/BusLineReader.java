package edu.virginia.sde.hw5;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class BusLineReader {
    private final URL busLinesApiUrl;
    private final URL busStopsApiUrl;

    /* You'll need this to get the Stop objects when building the Routes object */
    private final StopReader stopReader;

    /**
     * Returns a list of BusLine objects. This is a "deep" list, meaning all the BusLine objects
     * already have their Route objects fully populated with that line's Stops.
     */
    public BusLineReader(Configuration configuration) {
        this.busStopsApiUrl = configuration.getBusStopsURL();
        this.busLinesApiUrl = configuration.getBusLinesURL();
        stopReader = new StopReader(configuration);
    }

    /**
     * This method returns the BusLines from the API service, including their
     * complete Routes.
     */
    public List<BusLine> getBusLines() throws SQLException {
        List<BusLine> busLines = new ArrayList<>();
        try {
            WebServiceReader linesWebServiceReader = new WebServiceReader(busLinesApiUrl);
            JSONObject linesJson = linesWebServiceReader.getJSONObject();
            JSONArray linesArray = linesJson.getJSONArray("lines");

            for (int i = 0; i < linesArray.length(); i++) {
                JSONObject lineJson = linesArray.getJSONObject(i);
                int id = lineJson.getInt("id");
                String name = lineJson.getString("short_name");
                boolean isActive = lineJson.getBoolean("is_active");

                // Fetch stops for this line from the "routes" endpoint
                URL routesUrl = new URL(busStopsApiUrl.toString());
                WebServiceReader routesWebServiceReader = new WebServiceReader(routesUrl);
                JSONObject routeJson = routesWebServiceReader.getJSONObject();
                JSONArray routeStopsArray = routeJson.getJSONArray("stops");

                // Construct Route object for this line
                List<Stop> routeStops = new ArrayList<>();
                for (int j = 0; j < routeStopsArray.length(); j++) {
                        JSONObject stopJson = routeStopsArray.getJSONObject(j);
                        int stopId = stopJson.getInt("id");
                    try {
                        for (Stop stop : stopReader.getStops()) {
                                if (stop.getId() == stopId) {
                                    routeStops.add(stop);
                                }
                            }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                Route route = new Route(routeStops);

                // Create BusLine object and add to the list
                busLines.add(new BusLine(id, isActive, name, "", route));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return busLines;
    }
}