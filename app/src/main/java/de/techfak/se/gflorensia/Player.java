package de.techfak.se.gflorensia;

import android.util.Log;

public class Player implements de.techfak.gse24.botlib.Player {
    int busTickets;
    int scooterTickets;
    int tramTickets;
    String position;

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
        Log.i("Bus Ticket", String.valueOf(busTickets));
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
}
