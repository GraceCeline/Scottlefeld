package de.techfak.se.gflorensia;
import android.app.Application;

import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

public class GameApplication extends Application {

    int round;
    Player player;

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

}
