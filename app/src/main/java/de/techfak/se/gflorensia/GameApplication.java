package de.techfak.se.gflorensia;
import android.app.Application;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameApplication extends Application {

    int round;
    Player player;
    List<PointOfInterest> poiList = new ArrayList<>();

    Map<String, Integer> detectiveTickets = new HashMap<>();
    Map<String, Integer> mxTickets = new HashMap<>();
    final PropertyChangeSupport support;
    public GameApplication() {
        this.support = new PropertyChangeSupport(this);
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
    public void setPOIList(List<PointOfInterest> poiList) {
        this.poiList = poiList;
    }

}
