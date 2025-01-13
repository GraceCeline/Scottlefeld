package de.techfak.se.gflorensia;
import android.app.Application;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.techfak.gse24.botlib.MX;
import de.techfak.se.gflorensia.model.GameModel;
import de.techfak.se.gflorensia.model.Player;
import de.techfak.se.gflorensia.model.PointOfInterest;

public class GameApplication extends Application {
    public GameModel gameModel;

    public GameModel getGameModel() {
        return gameModel;
    }

    public void setGameModel(GameModel gameModel) {
        this.gameModel = gameModel;
    }
}
