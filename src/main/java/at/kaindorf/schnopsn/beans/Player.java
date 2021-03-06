package at.kaindorf.schnopsn.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

import java.sql.Array;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Player {
    private UUID playerID;
    private String playerName;
    //True bei dem der Trumpf ansagt
    private boolean caller;
    //True bei dem der seinen Call spielen darf 4er Schnopsn
    private boolean playsCall;
    private int playerNumber;
    //True bei dem der das spiel erstellt hat
    private boolean admin;
    //True bei dem der jetzt an der reihe ist zum ausspielen
    private boolean myTurn;
    //anzahl der Stiche die er in dieser runde gamacht hat
    private int numberOfStingsPerRound;
    //False bei dem Teammitglied von dem der einen BETTLER,ASSENBETTLER oder PLAUDERER spielt
    private boolean active;
    //wer zudreht
    private boolean zudreher;
    //temp speicher für 20er 40er

    @JsonIgnore
    private WebSocketSession session;

    public Player(UUID playerID, String playerName, boolean caller, boolean playsCall, int playerNumber, boolean admin, boolean myTurn, int numberOfStingsPerRound, boolean active, boolean zudreher, WebSocketSession session) {
        this.playerID = playerID;
        this.playerName = playerName;
        this.caller = caller;
        this.playsCall = playsCall;
        this.playerNumber = playerNumber;
        this.admin = admin;
        this.myTurn = myTurn;
        this.numberOfStingsPerRound = numberOfStingsPerRound;
        this.active = active;
        this.zudreher = zudreher;
        this.session = session;
    }

    public Player() {
    }

    public Player(UUID playerID, String playerName, boolean caller, boolean playsCall, int playerNumber, boolean admin, boolean myTurn, int numberOfStingsPerRound, boolean active) {
        this.playerID = playerID;
        this.playerName = playerName;
        this.caller = caller;
        this.playsCall = playsCall;
        this.playerNumber = playerNumber;
        this.admin = admin;
        this.myTurn = myTurn;
        this.numberOfStingsPerRound = numberOfStingsPerRound;
        this.active = active;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isZudreher() {
        return zudreher;
    }

    public void setZudreher(boolean zudreher) {
        this.zudreher = zudreher;
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
        return playerID.equals(player.playerID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerID);
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
                ", myTurn=" + myTurn +
                ", numberOfStingsPerRound=" + numberOfStingsPerRound +
                ", active=" + active +
                ", session=" + session +
                '}';
    }
}
