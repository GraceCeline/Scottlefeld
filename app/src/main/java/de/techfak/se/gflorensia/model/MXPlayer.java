package de.techfak.se.gflorensia.model;

import de.techfak.gse24.botlib.MX;
import de.techfak.gse24.botlib.Turn;
import de.techfak.gse24.botlib.exceptions.NoTicketAvailableException;

public class MXPlayer extends Player implements MX {
    int busTickets;
    int tramTickets;
    int scooterTickets;
    String position;
    public MXPlayer() {
    }

    @Override
    public Turn getTurn() throws NoTicketAvailableException {
        return null;
    }

    @Override
    public void giveBusTicket() {
        // Example: increment bus ticket count, or do something else
        this.busTickets++;
    }

    @Override
    public void giveScooterTicket() {
        this.scooterTickets++;
    }

    @Override
    public void giveTramTicket() {
        this.tramTickets++;
    }
    public int getBusTickets() {
        return busTickets;
    }

    public void setBusTickets(int busTickets) {
        this.busTickets = busTickets;
    }

    public int getTramTickets() {
        return tramTickets;
    }

    public void setTramTickets(int tramTickets) {
        this.tramTickets = tramTickets;
    }

    public int getScooterTickets() {
        return scooterTickets;
    }

    public void setScooterTickets(int scooterTickets) {
        this.scooterTickets = scooterTickets;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
