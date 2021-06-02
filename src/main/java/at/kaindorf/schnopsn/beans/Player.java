package at.kaindorf.schnopsn.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

import java.sql.Array;
import java.util.UUID;

public class Player {
    private UUID playerID;
    private String playerName;
    private boolean caller;
    private boolean playsCall;
    private int playerNumber;
    private boolean admin;

    @JsonIgnore
    private WebSocketSession session;

    public Player(UUID playerID, String playerName, boolean caller, boolean playsCall, int playerNumber, boolean admin, WebSocketSession session) {
        this.playerID = playerID;
        this.playerName = playerName;
        this.caller = caller;
        this.playsCall = playsCall;
        this.playerNumber = playerNumber;
        this.admin = admin;
        this.session = session;
    }

    public Player() {
    }

    // region <getter, setter, toString>

    public Player(UUID playerID, String playerName, boolean caller, boolean playsCall, int playerNumber, boolean admin) {
        this.playerID = playerID;
        this.playerName = playerName;
        this.caller = caller;
        this.playsCall = playsCall;
        this.playerNumber = playerNumber;
        this.admin = admin;
    }

    @Override
    public String toString() {
        return "Player{" +
                "playerID=" + playerID +
                ", playerName='" + playerName + '\'' +
                ", caller=" + caller +
                ", playsCall=" + playsCall +
                ", playerNumber=" + playerNumber +
                ", admin=" + admin +
                '}';
    }

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

    public void setPlaysCall(boolean playsCall) {
        this.playsCall = playsCall;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public WebSocketSession getSession() {
        return session;
    }

    public void setSession(WebSocketSession session) {
        this.session = session;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        if (caller != player.caller) return false;
        if (playsCall != player.playsCall) return false;
        if (playerNumber != player.playerNumber) return false;
        if (admin != player.admin) return false;
        if (playerID != null ? !playerID.equals(player.playerID) : player.playerID != null) return false;
        if (playerName != null ? !playerName.equals(player.playerName) : player.playerName != null) return false;
        return session != null ? session.equals(player.session) : player.session == null;
    }

    @Override
    public int hashCode() {
        int result = playerID != null ? playerID.hashCode() : 0;
        result = 31 * result + (playerName != null ? playerName.hashCode() : 0);
        result = 31 * result + (caller ? 1 : 0);
        result = 31 * result + (playsCall ? 1 : 0);
        result = 31 * result + playerNumber;
        result = 31 * result + (admin ? 1 : 0);
        result = 31 * result + (session != null ? session.hashCode() : 0);
        return result;
    }

    // endregion
}
