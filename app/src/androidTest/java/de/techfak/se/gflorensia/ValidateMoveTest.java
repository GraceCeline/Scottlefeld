package de.techfak.se.gflorensia;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import java.math.BigDecimal;

import de.techfak.gse24.botlib.PlayerFactory;
import de.techfak.se.gflorensia.model.Connection;
import de.techfak.se.gflorensia.model.Player;
import de.techfak.se.gflorensia.model.PointOfInterest;
import de.techfak.se.gflorensia.model.GameModel;

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
    GameModel gameModel =  new GameModel();

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
     * 2)
     * Akzeptanzkriterium: (2.1) Zug ist valid, weil ein Zielort wird gewählt, der nicht zu aktuellem Standort identisch ist
     * <p>
     * Äquivalenzklasse: Neuer (unidentische zum aktuellen Standort) Standort wird gewählt
     * <p>
     * Ausgangszustand:
     * <br>
     * Aktueller eigener Standort: Stadthalle Bielefeld
     * <p>
     * Benutztes Verkehrsmittel : eScooter
     * <p>
     * Aktuelle Ticketanzahl: (Bus:2, Tram:0, eScooter:8)
     * <p>
     * Zug: (Zielort: Hauptbahnhof Bielefeld, Stadthalle Bielefeld, Verkehrsmittel: eScooter)
     * <p>
     * Erwartete Exception: -
     */
    @Test
    public void testDifferentDestination() {

        try {
            gameModel.validateMove(currentPOI, destinationValid, SCOOTER, player);
            player.decScooterTickets();
            assertTrue(true);
        } catch (InvalidConnectionException | ZeroTicketException e) {
            fail("Unexpected exception" + e.getMessage());
        }
    }

    /**
     * 2)
     * Akzeptanzkriterium: (2.1) Das Ziel ist zu aktuellen Standort identisch (Negative case)
     * <p>
     * Äquivalenzklasse: Aktueller Standort gewählt
     * <p>
     * Ausgangszustand:
     * <br>
     * Aktueller eigener Standort: Hauptbahnhof Bielefeld
     * <p>
     * Benutztes Verkehrsmittel : -
     * <p>
     * Aktuelle Ticketanzahl: (Bus:2, Tram:0, eScooter:8)
     * <p>
     * Zug: -
     * <p>
     * Erwartete Exception: InvalidConnectionException
     */
    @Test
    public void testEqualDestination() {

        try {
            gameModel.validateMove(currentPOI, destinationInvalid, SCOOTER, player);
            fail("Should throw Exception because destination is identical to currentPOI.");
        } catch (InvalidConnectionException e) {
            assertTrue(true);
        } catch (Exception | ZeroTicketException e) {
            fail();
        }
    }


    /**
     * 3)
     * Akzeptanzkriterium: (2.3) Es gibt eine Verbindung vom aktuellen Standort zum Ziel (Positive case)
     * <p>
     * Äquivalenzklasse: Direkte Verbindung existiert
     * <p>
     * Ausgangszustand:
     * <br>
     * Aktueller eigener Standort: Stadthalle Bielefeld
     * <p>
     * Benutztes Verkehrsmittel : escooter
     * <p>
     * Aktuelle Ticketanzahl: (Bus:2, Tram:0, eScooter:8)
     * <p>
     * Zug: (Zielort: Hauptbahnhof Bielefeld, Stadthalle Bielefeld, Verkehrsmittel: eScooter)
     * <p>
     * Erwartete Exception: -
     */
    @Test
    public void testConnectionExist() {
        try {
            gameModel.validateMove(currentPOI, destinationValid, SCOOTER, player);
            assertTrue(true);
        } catch (Exception | InvalidConnectionException | ZeroTicketException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    /**
     * 4)
     * Akzeptanzkriterium: (2.2) Keine Verbindung von aktuellem Standort zum Ziel (Negative case)
     * <p>
     * Äquivalenzklasse: Keine Verbindung
     * <p>
     * Ausgangszustand:
     * <br>
     * Aktueller eigener Standort: -
     * <p>
     * Benutztes Verkehrsmittel : -
     * <p>
     * Aktuelle Ticketanzahl: (Bus:2, Tram:0, eScooter:8)
     * <p>
     * Zug: (Zielort: Hauptbahnhof Bielefeld, Neumarkt, Verkehrsmittel: eScooter)
     * <p>
     * Erwartete Exception: InvalidConnectionException
     */
    @Test
    public void testConnectionDoesNotExist() {
        try {
            gameModel.validateMove(currentPOI, destinationConnectionFail, SCOOTER, player);
            fail("Should throw InvalidConnectionException because there's no direct connection.");
        } catch (InvalidConnectionException e) {
            // Expected
            assertTrue(true);
        } catch (Exception | ZeroTicketException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    /**
     * 5)
     * Akzeptanzkriterium: (2.3) Für die Verbindung ist das benutzte Verkehrsmittel zulässig (Positive case)
     * <p>
     * Äquivalenzklasse: Verbindung hat das benutztes Verkehrsmittel
     * <p>
     * Ausgangszustand:
     * <br>
     * Aktueller eigener Standort: Stadthalle Bielefeld
     * <p>
     * Benutztes Verkehrsmittel : escooter
     * <p>
     * Aktuelle Ticketanzahl: (Bus:2, Tram:0, eScooter:8)
     * <p>
     * Zug: (Zielort: Hauptbahnhof Bielefeld, Stadthalle Bielefeld, Verkehrsmittel: eScooter)
     * <p>
     * Erwartete Exception: -
     */
    @Test
    public void testTransportModeExist() {
        try {
            gameModel.validateMove(currentPOI, destinationValid, SCOOTER, player);
            assertTrue(true);
        } catch (Exception | InvalidConnectionException | ZeroTicketException e) {
            fail("Should not throw exception if the transport mode is permitted.");
        }
    }

    /**
     * 6)
     * Akzeptanzkriterium: (2.3) Das benutzte Verkehrsmittel ist für den Zug unerlaubt (Negative case)
     * <p>
     * Äquivalenzklasse: Verbindung mit falschem Verkehrsmittel
     * <p>
     * Ausgangszustand:
     * <br>
     * Aktueller eigener Standort: Hauptbahnhof Bielefeld
     * <p>
     * Benutztes Verkehrsmittel : -
     * <p>
     * Aktuelle Ticketanzahl: (Bus:2, Tram:0, eScooter:8)
     * <p>
     * Zug: (Zielort: Hauptbahnhof Bielefeld, Stadthalle Bielefeld, Verkehrsmittel: Bus)
     * <p>
     * Erwartete Exception: InvalidConnectionException
     */
    @Test
    public void testTransportModeDoesNotExist() {
        try {
            gameModel.validateMove(currentPOI, destinationValid, BUS, player);
            fail("Should throw InvalidConnectionException");
        } catch (InvalidConnectionException  e) {
            assertTrue(true);
        } catch (Exception | ZeroTicketException e) {
            fail("Should not be thrown");
        }
    }

    /**
     * 7)
     * Akzeptanzkriterium: (2.4) Für das benutzte Verkehrsmittel hat der Detective mindestens ein Ticket (Positive case)
     * <p>
     * Äquivalenzklasse: Genug Tickets für diesen Zug
     * <p>
     * Ausgangszustand:
     * <br>
     * Aktueller eigener Standort: Stadthalle Bielefeld
     * <p>
     * Benutztes Verkehrsmittel : escooter
     * <p>
     * Aktuelle Ticketanzahl: (Bus:2, Tram:0, eScooter:7)
     * <p>
     * Zug: (Zielort: Hauptbahnhof Bielefeld, Jahnplatz, Verkehrsmittel: Tram)
     * <p>
     * Erwartete Exception: ZeroTicketException
     */
    @Test
    public void testEnoughTickets() {
        try {
            gameModel.validateMove(currentPOI,
                    destinationValid,
                    SCOOTER,
                    player);
            player.decScooterTickets();
            assertTrue(true);
        } catch (Exception | InvalidConnectionException | ZeroTicketException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    /**
     * 8)
     * Akzeptanzkriterium: (2.4) Für das benutzte Verkehrsmittel hat der Detective kein Ticket (Negative case)
     * <p>
     * Äquivalenzklasse: Zero Tickets für das Verkehrsmittel
     * <p>
     * Ausgangszustand:
     * <br>
     * Aktueller eigener Standort: Hauptbahnhof Bielefeld
     * <p>
     * Benutztes Verkehrsmittel : tram
     * <p>
     * Aktuelle Ticketanzahl: (Bus:2, Tram:0, eScooter:8)
     * <p>
     * Zug: (Zielort: Hauptbahnhof Bielefeld, Jahnplatz, Verkehrsmittel: Tram)
     * <p>
     * Erwartete Exception: ZeroTicketException
     */
    @Test
    public void testNotEnoughTickets() {
        try {
            gameModel.validateMove(
                    currentPOI,
                    destinationTram,
                    TRAM,
                    player
            );
            fail("Should throw ZeroTicketException because detective has 0 tram tickets.");
        } catch (ZeroTicketException e) {
            assertTrue(true);
        } catch (Exception | InvalidConnectionException e) {
            fail("Unexpected exception: " + e.getMessage());
        }

    }

    /**
     * 9)
     * Akzeptanzkriterium: (3) InvalidConnectionException wird geworfen für ungültige (nicht existierende) Verbindung
     * <p>
     * Äquivalenzklasse: Ein Zug ist ungültig, da der aktuelle Standort und das Ziel nicht verbunden sind
     * <p>
     * Ausgangszustand:
     * <br>
     * Aktueller eigener Standort: Hauptbahnhof Bielefeld
     *
     * Benutztes Verkehrsmittel : -
     *
     * Aktuelle Ticketanzahl: (Bus:2, Tram:0, eScooter:8)
     * <p>
     * Zug: (Zielort: Hauptbahnhof Bielefeld, Neumarkt, Verkehrsmittel: Bus)
     * <p>
     * Erwartete Exception: InvalidConnectionException
     */
    @Test
    public void testInvalidConnectionExceptionThrown() {
        try {
            gameModel.validateMove(
                    currentPOI,
                    destinationConnectionFail,
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
     * Akzeptanzkriterium: (4) ZeroTicketException wird geworfen für einen Zug ohne verfügbare Ticket für das genutzte Verkehrsmittel
     * 10)
     * <p>
     * Äquivalenzklasse: Kein Ticket für das Verkehrsmittel
     * <p>
     * Ausgangszustand:
     * <br>
     * Aktueller eigener Standort: Hauptbahnhof Bielefeld
     *
     * Benutztes Verkehrsmittel : tram
     *
     * Aktuelle Ticketanzahl: (Bus:2, Tram:0, eScooter:8)
     * <p>
     * Zug: (Zielort: Hauptbahnhof Bielefeld, Jahnplatz, Verkehrsmittel: Bus)
     * <p>
     * Erwartete Exception: ZeroTicketException
     */
    @Test
    public void testZeroTicketExceptionThrown() {
        try {
            gameModel.validateMove(
                    currentPOI,
                    destinationTram,
                    TRAM,
                    player
            );
            fail("Expected ZeroTicketException when detective has no tram tickets.");
        } catch (ZeroTicketException e) {
            assertTrue(true);
        } catch (Exception | InvalidConnectionException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

}