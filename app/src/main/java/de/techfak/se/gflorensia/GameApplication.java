package de.techfak.se.gflorensia;
import android.app.Application;
import android.util.Log;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

public class GameApplication extends Application {

    Integer round;
    boolean isMXTurn = true;

    Map<String, Integer> detectiveTickets = new HashMap<>();
    Map<String, Integer> mxTickets = new HashMap<>();
    private final PropertyChangeSupport support ;

    public GameApplication(){
        this.support = new PropertyChangeSupport(this);
    }


    public Integer getRound() {
        return this.round;
    }
    public void incRound() {
        this.round++;
        this.support.firePropertyChange("round", this.round, this.round);
    }

    void setDetectiveTickets(Map<String, Integer> tickets){
        this.detectiveTickets = tickets;
        // this.support.firePropertyChange("tickets", this.detectiveTickets, this.detectiveTickets);
    }

    public void setRound(Integer newRound){
        Log.d("GameApplication", "Firing property change"); // Debug log
        this.round = newRound;
        this.support.firePropertyChange("round", this.round, newRound);
    }

    public void addListener(PropertyChangeListener listener) {
        Log.d("GameApplication", "Listener added: " + listener.toString());
        this.support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }


}
