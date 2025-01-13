package de.techfak.se.gflorensia.model;
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
public class GameModel {
    public static final String MAPS = "maps/";
    public static final String GEO = ".geojson";
    public static final String BUS = "bus";
    public static final String TRAM = "tram";
    public static final String SCOOTER = "escooter";
    public int round;
    public MX mx;

    public Player player;
    public PlayerFactory playerFactory;

    public List<PointOfInterest> poiList = new ArrayList<>();

    public Map<String, Integer> detectiveTickets = new HashMap<>();
    public Map<String, Integer> mxTickets = new HashMap<>();
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

    public void setMX(MX mx) {
        this.mx = mx;
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
    public MX createMX(String jsonContent, Player player, Map<String, Integer> mxTickets)
            throws JSONParseException, NoFreePositionException {

        playerFactory = new PlayerFactory(jsonContent, player);

        return playerFactory.createMx(mxTickets.get(BUS), mxTickets.get(TRAM), mxTickets.get(SCOOTER));
    }
    public void addListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
