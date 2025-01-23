package de.techfak.se.gflorensia.model;


import de.techfak.gse24.botlib.MX;
import de.techfak.se.gflorensia.InvalidConnectionException;
import de.techfak.se.gflorensia.ZeroTicketException;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class GameModel {
    public static final String BUS = "bus";
    public static final String TRAM = "tram";
    public static final String SCOOTER = "escooter";
    public static final int ENDGAME = 22;
    final PropertyChangeSupport support;
    int round;
    MX mx;
    Player player;
    PointOfInterest currentLocation;
    List<PointOfInterest> poiList = new ArrayList<>();
    Map<String, Integer> detectiveTickets;
    Map<String, Integer> mxTickets;
    public GameModel() {
        this.support = new PropertyChangeSupport(this);
    }

    public Player getPlayer() {
        return player;
    }
    public void setPlayer(Player player) {
        this.player = player;
    }
    public MX getMX() {
        return mx;
    }

    public void setMX(MX mxPlayer) {
        this.mx = mxPlayer;
    }
    public Map<String, Integer> getDetectiveTickets() {
        return this.detectiveTickets;
    }
    public void setDetectiveTickets(Map<String, Integer> detectiveTickets) {
        this.detectiveTickets = detectiveTickets;
    }
    public Map<String, Integer> getMXTickets() {
        return this.mxTickets;
    }
    public void setMXTickets(Map<String, Integer> mxTickets) {
        this.mxTickets = mxTickets;
    }
    public List<PointOfInterest> getPoiList() {
        return poiList;
    }
    public void setPOIList(List<PointOfInterest> poiList) {
        this.poiList = poiList;
    }
    // mr x
    // normalen spieler
    // validateTurn
    // increase round counter
    // is game over check


    public Integer getRound() {
        return this.round;
    }
    public void incRound() {
        int oldRound = this.getRound();
        this.round++;
        this.support.firePropertyChange("round", oldRound, this.round);
    }

    public PointOfInterest getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(PointOfInterest currentLocation) {
        this.currentLocation = currentLocation;
        this.player.setPosition(currentLocation.getName());
    }

    public MXPlayer initMXPlayer(int busTickets,
                                 int tramTickets, int scooterTickets) {

        MXPlayer newMxPlayer = new MXPlayer();
        newMxPlayer.setBusTickets(busTickets);
        newMxPlayer.setTramTickets(tramTickets);
        newMxPlayer.setScooterTickets(scooterTickets);

        return newMxPlayer;
    }

    public void setMXStart(Player player, MXPlayer mx) {
        String normalPlayerLoc = player.getPosition();
        List<PointOfInterest> availablePois = new ArrayList<>(this.poiList);
        availablePois.removeIf(poi -> poi.getName().equals(normalPlayerLoc));

        Random random = new Random();
        PointOfInterest chosenPoi = availablePois.get(random.nextInt(availablePois.size()));
        mx.setPosition(chosenPoi.getName());
    }

    public void manageTickets(String selectedTransportMode) {
        if (Objects.equals(selectedTransportMode, BUS)) {
            player.decBusTickets();
            mx.giveBusTicket();
        } else if (Objects.equals(selectedTransportMode, TRAM)) {
            player.decTramTickets();
            mx.giveTramTicket();
        } else if (Objects.equals(selectedTransportMode, SCOOTER)) {
            player.decScooterTickets();
            mx.giveScooterTicket();
        }
    }
    public void validateMove(PointOfInterest poiStart,
                             PointOfInterest destinationPOI,
                                    String transportMode,
                                    Player player)
            throws InvalidConnectionException, ZeroTicketException {

        boolean connectionFound = false;
        for (Connection connection : poiStart.getConnections()) {
            if (connection.getDestination().equals(destinationPOI)) {
                if (connection.getTransportMode().equals(transportMode)) {
                    connectionFound = true;
                    break;
                }
            }
        }

        if (!connectionFound) {
            throw new InvalidConnectionException("Invalid connection");
        }

        switch (transportMode) {
            case BUS:
                if (player.getBusTickets() == 0) {
                    throw new ZeroTicketException("No ticket available for bus");
                }
                break;
            case SCOOTER:
                if (player.getScooterTickets() == 0) {
                    throw new ZeroTicketException("No ticket available for scooter");
                }
                break;
            case TRAM:
                if (player.getTramTickets() == 0) {
                    throw new ZeroTicketException("No ticket available for tram");
                }
                break;
            default:
                break;
        }
    }

    public String endGameConditions(PointOfInterest destination) {
        if (player.returnAllZero(this.currentLocation) || round == ENDGAME) {
            return "MX";
        } else if (destination.getName().equals(mx.getPosition())) {
            return "Detective";
        }
        return "";
    }
    public void addListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
