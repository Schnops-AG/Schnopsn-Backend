package at.kaindorf.schnopsn.beans;

import lombok.Data;
import java.util.UUID;

public class Player {
    private UUID playerid;
    private String playername;
    private boolean caller;
    private boolean active;

    public Player(UUID playerid, String playername, boolean caller, boolean active) {
        this.playerid = playerid;
        this.playername = playername;
        this.caller = caller;
        this.active = active;
    }

    // region <getter, setter, toString>

    public UUID getPlayerid() {
        return playerid;
    }

    public String getPlayername() {
        return playername;
    }

    public boolean isCaller() {
        return caller;
    }

    public boolean isActive() {
        return active;
    }

    public void setPlayerid(UUID playerid) {
        this.playerid = playerid;
    }

    public void setPlayername(String playername) {
        this.playername = playername;
    }

    public void setCaller(boolean caller) {
        this.caller = caller;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Player{" +
                "playerid=" + playerid +
                ", playername='" + playername + '\'' +
                ", caller=" + caller +
                ", active=" + active +
                '}';
    }

    // endregion
}
