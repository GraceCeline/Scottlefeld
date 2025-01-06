package de.techfak.se.gflorensia;
import junit.framework.TestCase;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import javax.xml.validation.Validator;

public class ValidateMoveTest extends TestCase {
    private final String BUS = "bus";
    private final String TRAM = "tram";
    private final String SCOOTER = "escooter";
    private PointOfInterest currentPOI = new PointOfInterest("Hauptbahnhof Bielefeld",
            BigDecimal.valueOf(52.02909),
            BigDecimal.valueOf(8.533033));
    private PointOfInterest destinationValid = new PointOfInterest("Stadthalle Bielefeld",
            BigDecimal.valueOf(52.028626),
            BigDecimal.valueOf(8.535477));
    private PointOfInterest destinationInvalid = new PointOfInterest("Hauptbahnhof Bielefeld",
            BigDecimal.valueOf(52.02909),
            BigDecimal.valueOf(8.533033));
    private PointOfInterest destinationConnectionFail = new PointOfInterest("Neumarkt",
            BigDecimal.valueOf(52.026007),
            BigDecimal.valueOf(8.536613));
    private PointOfInterest destinationTram = new PointOfInterest("Jahnplatz",
            BigDecimal.valueOf(52.022881),
            BigDecimal.valueOf(8.532977));
    private Player player;

    @Before
    public void setUp() {

        player = new Player();
        player.setBusTickets(2);
        player.setTramTickets(0);
        player.setScooterTickets(8);

        currentPOI.addConnection(new Connection(SCOOTER, destinationValid));
        currentPOI.addConnection(new Connection(TRAM, destinationTram));
    }

    /**
    * 1) Test: AC 2.1 - A move is only valid if the destination is different from the current location.
            *    Equivalence Class: "Different POI selected" vs. "Same POI selected"
            *    Initial State: Current location and valid destination are distinct.
     */
    @Test
    public void testDifferentDestination() {

        boolean moveValid = !currentPOI.equals(destinationValid);
        assertTrue(moveValid);
    }

    /**
     * 2) Test: AC 2.1 (Positive case) - Destination must be different from current location.
     *    Equivalence Class: "Same POI selected"
     *    Initial State: Attempting to move from currentPOI to an identical POI.
     */
    @Test
    public void testEqualDestination() {

        boolean moveInvalid = currentPOI.equals(destinationInvalid);
        assertTrue(moveInvalid);
    }

    /**
     * 3) Test: AC 2.2 (Positive Case) - There must be a direct connection from current location to the destination.
     *    Equivalence Class: "Direct connection exists"
     *    Initial State: currentPOI -> destinationValid has a bus/tram/scooter connection
     */
    @Test
    public void testConnectionExist() {
        try {
            StartActivity.validateMove(currentPOI, destinationValid, SCOOTER, player);
            assertTrue(true);
        } catch (Exception | InvalidConnectionException | ZeroTicketException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    /**
     * 4) Test: AC 2.2 (Negative case) - No direct connection from current location to the destination.
     *    Equivalence Class: "No direct connection"
     *    Initial State: currentPOI -> destinationConnectionFail has no connections
     */
    @Test
    public void testConnectionDoesNotExist() {
        try {
            StartActivity.validateMove(currentPOI, destinationConnectionFail, SCOOTER, player);
            fail("Should throw InvalidConnectionException because there's no direct connection.");
        } catch (InvalidConnectionException e) {
            // Expected
            assertTrue(true);
        } catch (Exception | ZeroTicketException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    /**
     * 5) Test: AC 2.3 - The selected means of transport is permitted for the connection.
     *    Equivalence Class: "Connection has the transport mode"
     *    Initial State: currentPOI -> destinationValid is connected with a scooter
     */
    @Test
    public void testTransportModeExist() {
        try {
            StartActivity.validateMove(currentPOI, destinationValid, SCOOTER, player);
            assertTrue(true);
        } catch (Exception | InvalidConnectionException | ZeroTicketException e) {
            fail("Should not throw exception if the transport mode is permitted.");
        }
    }

    /**
     * 6) Test: AC 2.3 (Negative case) - The selected means of transport is NOT permitted for the connection.
     *    Equivalence Class: "Connection doesn't have that transport mode"
     *    Initial State: currentPOI -> destinationValid is not connected with a bus
     */
    @Test
    public void testTransportModeDoesNotExist() {
        try {
            StartActivity.validateMove(currentPOI, destinationValid, BUS, player);
            fail("Should throw InvalidConnectionException");
        } catch (InvalidConnectionException  e) {
            assertTrue(true);
        } catch (Exception | ZeroTicketException e) {
            fail("Should not be thrown");
        }
    }

    /**
     * 7) Test: AC 2.4 - The detective has at least one ticket for the selected transport mode.
     *    Equivalence Class: "Enough tickets"
     *    Initial State: detectivePlayer has bus tickets
     */
    @Test
    public void testEnoughTickets() {
        try {
            StartActivity.validateMove(currentPOI,
                    destinationValid,
                    SCOOTER,
                    player);
            assertTrue(true);
        } catch (Exception | InvalidConnectionException | ZeroTicketException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    /**
     * 8) Test: AC 2.4 (Negative case) - The detective does NOT have any ticket for the selected transport mode.
     *    Equivalence Class: "Zero tickets"
     *    Initial State: detectivePlayer has no scooter tickets
     */
    @Test
    public void testNotEnoughTickets() {
        try {
            StartActivity.validateMove(
                    currentPOI,
                    destinationTram,
                    TRAM,
                    player
            );
            fail("Should throw ZeroTicketException because detective has 0 scooter tickets.");
        } catch (ZeroTicketException e) {
            assertTrue(true);
        } catch (Exception | InvalidConnectionException e) {
            fail("Unexpected exception: " + e.getMessage());
        }

    }

    /**
     * 9) Test: InvalidConnectionException thrown for an invalid connection.
     *    Equivalence Class: "Invalid connection attempt"
     *    Initial State: currentPOI -> invalidDestination is not in the connections list
     */
    @Test
    public void testInvalidConnectionExceptionThrown() {
        try {
            StartActivity.validateMove(
                    currentPOI,
                    destinationInvalid,
                    "bus",
                    player
            );
            fail("Expected InvalidConnectionException for a non-existent route.");
        } catch (InvalidConnectionException e) {
            assertTrue(true);
        } catch (Exception | ZeroTicketException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    /**
     * 10) Test: ZeroTicketException thrown for an unavailable ticket type.
     *     Equivalence Class: "No tickets for the selected mode"
     *     Initial State: detectivePlayer has 0 tram tickets
     */
    @Test
    public void testZeroTicketExceptionThrown() {
        try {
            StartActivity.validateMove(
                    currentPOI,
                    destinationTram,
                    TRAM,
                    player
            );
            fail("Expected ZeroTicketException when detective has no scooter tickets.");
        } catch (ZeroTicketException e) {
            assertTrue(true);
        } catch (Exception | InvalidConnectionException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

}