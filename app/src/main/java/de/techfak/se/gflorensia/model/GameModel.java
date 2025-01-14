package de.techfak.se.gflorensia.model;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.techfak.gse24.botlib.MX;
import de.techfak.gse24.botlib.PlayerFactory;
import de.techfak.gse24.botlib.exceptions.JSONParseException;
import de.techfak.gse24.botlib.exceptions.NoFreePositionException;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GameModel {
    public static final String BUS = "bus";
    public static final String TRAM = "tram";
    public static final String SCOOTER = "escooter";
    int round;
    MX mx;

    Player player;
    public List<PointOfInterest> poiList = new ArrayList<>();
    public Map<String, Integer> detectiveTickets;
    public Map<String, Integer> mxTickets;
    final PropertyChangeSupport support;
    public GameModel() {
        // mr x erstellen mit plazerfactorz
        // ....
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
    public List<PointOfInterest> getPoiList() { return poiList; }
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
    public void addListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
