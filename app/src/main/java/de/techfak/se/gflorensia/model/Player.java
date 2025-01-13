package de.techfak.se.gflorensia.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Player implements de.techfak.gse24.botlib.Player {
    private static final String PROPERTY_BUS = "bus";
    private static final String PROPERTY_TRAM = "tram";
    private static final String PROPERTY_SCOOTER = "escooter";
    int busTickets;
    int scooterTickets;
    int tramTickets;
    String position;


    private final PropertyChangeSupport support;

    public Player() {
        this.support = new PropertyChangeSupport(this);
    }

    public void setBusTickets(int busTickets) {
        int oldTickets = this.tramTickets;
        this.busTickets = busTickets;
        this.support.firePropertyChange(PROPERTY_BUS, oldTickets, this.busTickets);
    }

    public void setTramTickets(int tramTickets) {
        int oldTickets = this.tramTickets;
        this.tramTickets = tramTickets;
        this.support.firePropertyChange(PROPERTY_TRAM, oldTickets, this.tramTickets);
    }

    public void setScooterTickets(int scooterTickets) {
        int oldTickets = this.scooterTickets;
        this.scooterTickets = scooterTickets;
        this.support.firePropertyChange(PROPERTY_SCOOTER, oldTickets, this.scooterTickets);
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void decBusTickets() {
        int oldTickets = this.busTickets;
        this.busTickets--;
        this.support.firePropertyChange(PROPERTY_BUS, oldTickets, this.busTickets);
    }

    public void decTramTickets() {
        int oldTickets = this.tramTickets;
        this.tramTickets--;
        this.support.firePropertyChange(PROPERTY_TRAM, oldTickets, this.tramTickets);
    }

    public void decScooterTickets() {
        int oldTickets = this.scooterTickets;
        this.scooterTickets--;
        this.support.firePropertyChange(PROPERTY_SCOOTER, oldTickets, this.scooterTickets);
    }


    @Override
    public int getBusTickets() {
        return busTickets;
    }

    @Override
    public int getScooterTickets() {
        return scooterTickets;
    }

    @Override
    public int getTramTickets() {
        return tramTickets;
    }

    @Override
    public String getPosition() {
        return position;
    }

    public void addListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }


    public boolean returnAllZero(PointOfInterest poi) {
        List<String> transport = new ArrayList<>();
        for (Connection connection : poi.getConnections()) {
            transport.add(connection.getTransportMode());
        }
        Set<String> availableTransport = new HashSet<>(transport);
        for (String transportMode : availableTransport) {
            switch (transportMode) {
                case PROPERTY_BUS:
                    if (this.getBusTickets() > 0) {
                        return false;
                    }
                    break;
                case PROPERTY_SCOOTER:
                    if (this.getScooterTickets() > 0) {
                        return false;
                    }
                    break;
                case PROPERTY_TRAM:
                    if (this.getTramTickets() > 0) {
                        return false;
                    }
                    break;
                default:
                    break;
            }
        }
        return true;
    }
}
