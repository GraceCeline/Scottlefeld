package de.techfak.se.gflorensia;
import android.app.Application;
import de.techfak.se.gflorensia.model.GameModel;

public class GameApplication extends Application {
    GameModel gameModel;
    public GameModel getGameModel() {
        return gameModel;
    }

    public void setGameModel(GameModel gameModel) {
        this.gameModel = gameModel;
    }
}
