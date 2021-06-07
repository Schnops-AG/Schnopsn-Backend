package at.kaindorf.schnopsn.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

import java.sql.Array;
import java.util.Objects;
import java.util.UUID;

public class Player {
    private UUID playerID;
    private String playerName;
    private boolean caller;
    private boolean playsCall;
    private int playerNumber;
    private boolean admin;
    private boolean myTurn;
    private int numberOfStingsPerRound;

    @JsonIgnore
    private WebSocketSession session;

    public Player(UUID playerID, String playerName, boolean caller, boolean playsCall, int playerNumber, boolean admin, boolean myTurn, int numberOfStingsPerRound, WebSocketSession session) {
        this.playerID = playerID;
        this.playerName = playerName;
        this.caller = caller;
        this.playsCall = playsCall;
        this.playerNumber = playerNumber;
        this.admin = admin;
        this.myTurn = myTurn;
        this.numberOfStingsPerRound = numberOfStingsPerRound;
        this.session = session;
    }

    public Player() {
    }

    public Player(UUID playerID, String playerName, boolean caller, boolean playsCall, int playerNumber, boolean admin, boolean myTurn, int numberOfStingsPerRound) {
        this.playerID = playerID;
        this.playerName = playerName;
        this.caller = caller;
        this.playsCall = playsCall;
        this.playerNumber = playerNumber;
        this.admin = admin;
        this.myTurn = myTurn;
        this.numberOfStingsPerRound = numberOfStingsPerRound;
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

    public boolean isMyTurn() {
        return myTurn;
    }

    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }

    public int getNumberOfStingsPerRound() {
        return numberOfStingsPerRound;
    }

    public void setNumberOfStingsPerRound(int numberOfStingsPerRound) {
        this.numberOfStingsPerRound = numberOfStingsPerRound;
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
        return caller == player.caller && playsCall == player.playsCall && playerNumber == player.playerNumber && admin == player.admin && myTurn == player.myTurn && numberOfStingsPerRound == player.numberOfStingsPerRound && Objects.equals(playerID, player.playerID) && Objects.equals(playerName, player.playerName) && Objects.equals(session, player.session);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerID, playerName, caller, playsCall, playerNumber, admin, myTurn, numberOfStingsPerRound, session);
    }
}
