package at.kaindorf.schnopsn.api;

import at.kaindorf.schnopsn.beans.Game;
import at.kaindorf.schnopsn.beans.Player;

import java.util.ArrayList;
import java.util.List;

public class GameStorage {
    /**
     * This is a singleton class, where we store our "activeGames" list and our "activePlayers" list.
     * */
    private final List<Game> activeGames = new ArrayList<>();
    private final List<Player> activePlayers = new ArrayList<>();
    private static GameStorage instance;

    private GameStorage(){}

    public static GameStorage getInstance(){
        if (GameStorage.instance == null){
            GameStorage.instance = new GameStorage();
        }
        return GameStorage.instance;
    }

    public List<Game> getActiveGames() {
        return activeGames;
    }

    public List<Player> getActivePlayers() {
        return activePlayers;
    }

}
