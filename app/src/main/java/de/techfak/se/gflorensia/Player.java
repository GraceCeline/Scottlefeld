package de.techfak.se.gflorensia;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Player implements de.techfak.gse24.botlib.Player {
    int round;
    int busTickets;
    int scooterTickets;
    int tramTickets;
    String position;


    private final PropertyChangeSupport support ;

    public Player() {
        this.support = new PropertyChangeSupport(this);
    }

    public void setBusTickets(int busTickets) {
        this.busTickets = busTickets;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setTramTickets(int tramTickets) {
        this.tramTickets = tramTickets;
    }

    public void setScooterTickets(int scooterTickets) {
        this.scooterTickets = scooterTickets;
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

    public Integer getRound() {
        return this.round;
    }

    public void incRound() {
        int oldRound = this.getRound();
        this.round++;
        this.support.firePropertyChange("round", oldRound, this.round);
    }
}
