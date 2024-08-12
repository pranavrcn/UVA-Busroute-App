package edu.virginia.sde.hw5;
import org.junit.jupiter.api.Test;
import java.net.URL;
import static org.junit.jupiter.api.Assertions.*;

public class ConfigurationTest {
    @Test
    public void testGetters_pass() {
        // Create Configuration instance
        Configuration configuration = new Configuration();

        // Invoke the parseJsonConfigFile method
        configuration.parseJsonConfigFile();

        // Invoke the getter methods
        URL busStopsURL = configuration.getBusStopsURL();
        URL busLinesURL = configuration.getBusLinesURL();
        String databaseFilename = configuration.getDatabaseFilename();

        // Assertions
        assertEquals("https://www.cs.virginia.edu/~pm8fc/busses/stops.json", busStopsURL.toString());
        assertEquals("https://www.cs.virginia.edu/~pm8fc/busses/lines.json", busLinesURL.toString());
        assertEquals("bus_stops.sqlite", databaseFilename);
    }
    @Test
    public void testGetters() {
        // Create Configuration instance
        Configuration configuration = new Configuration();

        // Invoke the parseJsonConfigFile method
        configuration.parseJsonConfigFile();

        // Invoke the getter methods
        URL busStopsURL = configuration.getBusStopsURL();
        URL busLinesURL = configuration.getBusLinesURL();
        String databaseFilename = configuration.getDatabaseFilename();

        // Assertions
        assertNotEquals("https://example.com/stops", busStopsURL.toString());
        assertNotEquals("https://example.com/lines", busLinesURL.toString());
        assertNotEquals("test.db", databaseFilename);
    }
}
