package de.techfak.se.gflorensia;
import android.app.Application;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameApplication extends Application {

    int round ;
    Player player;

    Map<String, Integer> detectiveTickets = new HashMap<>();
    Map<String, Integer> mxTickets = new HashMap<>();
    private final PropertyChangeSupport support ;


    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }



    void setDetectiveTickets(Map<String, Integer> tickets){
        this.detectiveTickets = tickets;
        // this.support.firePropertyChange("tickets", this.detectiveTickets, this.detectiveTickets);
    }

    public GameApplication(){
        this.support = new PropertyChangeSupport(this);
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }


}
