package de.techfak.se.gflorensia;
import android.app.Application;
import java.util.HashMap;
import java.util.Map;

public class GameApplication extends Application {

    int round;
    boolean isMXTurn = true;

    Map<String, Integer> detectiveTickets = new HashMap<>();
    Map<String, Integer> mxTickets = new HashMap<>();

    public int getRound() {
        return this.round;
    }

}
