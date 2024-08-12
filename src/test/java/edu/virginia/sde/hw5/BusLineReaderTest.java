package edu.virginia.sde.hw5;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;
import java.net.URL;
import java.util.List;

public class BusLineReaderTest {
    private BusLineReader busLineReader;
    private Configuration configuration;
    private StopReader stopReader;

    @BeforeEach
    public void setUp() throws Exception {
        // Create mock Configuration object
        configuration = Mockito.mock(Configuration.class);

        // Stub methods of Configuration to return predefined URL objects
        URL busStopsUrl = new URL("https://www.cs.virginia.edu/~pm8fc/busses/stops.json");
        URL busLinesUrl = new URL("https://www.cs.virginia.edu/~pm8fc/busses/lines.json");
        Mockito.when(configuration.getBusStopsURL()).thenReturn(busStopsUrl);
        Mockito.when(configuration.getBusLinesURL()).thenReturn(busLinesUrl);

        // Create BusLineReader with mock Configuration and real StopReader (no need to mock)
        stopReader = new StopReader(configuration);
        busLineReader = new BusLineReader(configuration);
    }

    @Test
    public void getBusLinesTest() throws Exception {
        // Get the actual list of BusLines from the BusLineReader
        List<BusLine> actualBusLines = busLineReader.getBusLines();
        // Ensure that actualBusLines is not null and contains some elements
        assertNotNull(actualBusLines);
        assertFalse(actualBusLines.isEmpty());
    }
    @Test
    public void getBusLineTest_inorder() throws Exception {
        List<BusLine> actualBusLines = busLineReader.getBusLines();
        assertEquals(actualBusLines, busLineReader.getBusLines());
    }

//    @Test
//    public void getBusLineTest_build() throws Exception {
//        List<BusLine> actualBusLines = busLineReader.getBusLines();
//        List<BusLine> expectedBusLines = busLineReader.getBusLines();
//
//        assertEquals(actualBusLines.size(), expectedBusLines.size());
//
//        for (int i = 0; i < actualBusLines.size(); i++) {
//            BusLine actualBusLine = actualBusLines.get(i);
//            BusLine expectedBusLine = expectedBusLines.get(i);
//
//            assertEquals(actualBusLine.getId(), expectedBusLine.getId());
//            assertEquals(actualBusLine.getShortName(), expectedBusLine.getShortName());
//            assertEquals(actualBusLine.isActive(), expectedBusLine.isActive());
//
//            Route actualRoute = actualBusLine.getRoute();
//            Route expectedRoute = expectedBusLine.getRoute();
//
//            assertEquals(actualRoute.getStops().size(), expectedRoute.getStops().size());
//
//            List<Stop> actualStops = actualRoute.getStops();
//            List<Stop> expectedStops = expectedRoute.getStops();
//
//            for (int j = 0; j < actualStops.size(); j++) {
//                Stop actualStop = actualStops.get(j);
//                Stop expectedStop = expectedStops.get(j);
//
//                assertEquals(actualStop.getId(), expectedStop.getId());
//                assertEquals(actualStop.getName(), expectedStop.getName());
//                assertEquals(actualStop.getLatitude(), expectedStop.getLatitude(), 0.001); // Adjust delta as per precision
//                assertEquals(actualStop.getLongitude(), expectedStop.getLongitude(), 0.001); // Adjust delta as per precision
//            }
//        }
//    }
}
