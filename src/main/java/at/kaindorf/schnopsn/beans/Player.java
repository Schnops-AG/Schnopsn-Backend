package at.kaindorf.schnopsn.beans;

import lombok.Data;

import java.sql.Array;
import java.util.UUID;

public class Player {
    private UUID playerid;
    private String playername;
    private boolean caller;
    private boolean playsCall;
    private int playerNumber;

    public Player(UUID playerid, String playername, boolean caller, boolean playsCall, int playerNumber) {
        this.playerid = playerid;
        this.playername = playername;
        this.caller = caller;
        this.playsCall = playsCall;
        this.playerNumber = playerNumber;
    }
    // region <getter, setter, toString>
    public UUID getPlayerid() {
        return playerid;
    }

    public void setPlayerid(UUID playerid) {
        this.playerid = playerid;
    }

    public String getPlayername() {
        return playername;
    }

    public void setPlayername(String playername) {
        this.playername = playername;
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
