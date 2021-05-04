package at.kaindorf.schnopsn.beans;

import lombok.Data;

import java.sql.Array;
import java.util.UUID;

public class Player {
    private UUID playerid;
    private String playername;
    private boolean caller;
    private boolean active;
    private int playerNumber;

    public Player(UUID playerid, String playername, boolean caller, boolean active, int playerNumber) {
        this.playerid = playerid;
        this.playername = playername;
        this.caller = caller;
        this.active = active;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    // endregion
}
