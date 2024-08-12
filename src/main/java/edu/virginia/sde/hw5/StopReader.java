package edu.virginia.sde.hw5;

import java.net.URL;
import java.util.List;
import org.json.*;
import java.util.ArrayList;

public class StopReader {

    private final URL busStopsApiUrl;

    public StopReader(Configuration configuration) {
        this.busStopsApiUrl = configuration.getBusStopsURL();
    }

    /**
     * Read all the stops from the "stops" json URL from Configuration Reader
     * @return List of stops
     */
    public List<Stop> getStops() {
        WebServiceReader webServiceReader = new WebServiceReader(busStopsApiUrl);
        JSONObject stopsJson = webServiceReader.getJSONObject();

        // Extract stops from the JSON array
        JSONArray stopsArray = stopsJson.getJSONArray("stops");
        List<Stop> stops = new ArrayList<>();
        for (int i = 0; i < stopsArray.length(); i++) {
            JSONObject stopJson = stopsArray.getJSONObject(i);
            int id = stopJson.getInt("id");
            String name = stopJson.getString("name");

            // Extract position array
            JSONArray positionArray = stopJson.getJSONArray("position");
            double latitude = positionArray.getDouble(0);
            double longitude = positionArray.getDouble(1);

            stops.add(new Stop(id, name, latitude, longitude));
        }

        return stops;
    }

}
