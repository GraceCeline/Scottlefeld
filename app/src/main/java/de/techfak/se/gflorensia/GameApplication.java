package de.techfak.se.gflorensia;
import android.app.Application;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

public class GameApplication extends Application {

    Integer round;
    boolean isMXTurn = true;

    Map<String, Integer> detectiveTickets = new HashMap<>();
    Map<String, Integer> mxTickets = new HashMap<>();

    public Integer getRound() {
        return this.round;
    }

    public void setRound(Integer newRound){
        this.round = newRound;
    }


}
