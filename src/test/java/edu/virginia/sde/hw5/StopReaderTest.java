package edu.virginia.sde.hw5;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class StopReaderTest {
    @Test
    public void readStopsTest() {
        // Create Configuration instance
        Configuration configuration = new Configuration();
        configuration.parseJsonConfigFile();

        // Create StopReader with real Configuration
        StopReader stopReader = new StopReader(configuration);

        // Call getStops() method
        List<Stop> stops = stopReader.getStops();
        System.out.println(stops.size());
        // Verify the returned list of stops
        assertEquals(114, stops.size()); // Assuming you have 68 stops in the list

        // Check the first stop
        assertEquals(4235106, stops.get(0).getId());
        assertEquals("Alderman Rd @ Gooch/Dillard (Southbound)", stops.get(0).getName());
        assertEquals(38.029305, stops.get(0).getLatitude());
        assertEquals(-78.516414, stops.get(0).getLongitude());

        // Check the second stop
        assertEquals(4235108, stops.get(1).getId());
        assertEquals("Alderman Rd @ O-Hill Dining Hall", stops.get(1).getName());
        assertEquals(38.033937, stops.get(1).getLatitude());
        assertEquals(-78.51424, stops.get(1).getLongitude());

        // Add assertions for other stops as needed
    }
}
