package at.kaindorf.schnopsn.beans;

import lombok.Data;

import java.sql.Array;
import java.util.UUID;

public class Player {
    private UUID playerID;
    private String playerName;
    private boolean caller;
    private boolean playsCall;
    private int playerNumber;

    public Player(UUID playerID, String playerName, boolean caller, boolean playsCall, int playerNumber) {
        this.playerID = playerID;
        this.playerName = playerName;
        this.caller = caller;
        this.playsCall = playsCall;
        this.playerNumber = playerNumber;
    }
    // region <getter, setter, toString>


    public UUID getPlayerID() {
        return playerID;
    }

    public void setPlayerID(UUID playerID) {
        this.playerID = playerID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public boolean isCaller() {
        return caller;
    }

    public void setCaller(boolean caller) {
        this.caller = caller;
    }

    public boolean isPlaysCall() {
        return playsCall;
    }

    public void setPlaysCall(boolean active) {
        this.playsCall = playsCall;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    // endregion
}
