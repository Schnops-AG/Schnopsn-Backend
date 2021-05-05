package at.kaindorf.schnopsn.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Game {
    private UUID gameid;
    private GameType gameType;
    private List<Player> players;
    private URL inviteLink;
    private Color currentTrump;
    private int maxNumberOfPlayers;
    private Team[] teams;
    private Call currentHighestCall;

    public Game(UUID gameid, GameType gameType, List<Player> players, URL inviteLink, Color currentTrump, int maxNumberOfPlayers, Team[] teams, Call currentHighestCall) {
        this.gameid = gameid;
        this.gameType = gameType;
        this.players = players;
        this.inviteLink = inviteLink;
        this.currentTrump = currentTrump;
        this.maxNumberOfPlayers = maxNumberOfPlayers;
        this.teams = teams;
        this.currentHighestCall = currentHighestCall;
    }

    // region <getter, setter, toString>

    public UUID getGameid() {
        return gameid;
    }

    public void setGameid(UUID gameid) {
        this.gameid = gameid;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public URL getInviteLink() {
        return inviteLink;
    }

    public void setInviteLink(URL inviteLink) {
        this.inviteLink = inviteLink;
    }

    public Color getCurrentTrump() {
        return currentTrump;
    }

    public void setCurrentTrump(Color currentTrump) {
        this.currentTrump = currentTrump;
    }

    public int getMaxNumberOfPlayers() {
        return maxNumberOfPlayers;
    }

    public void setMaxNumberOfPlayers(int maxNumberOfPlayers) {
        this.maxNumberOfPlayers = maxNumberOfPlayers;
    }

    public Team[] getTeams() {
        return teams;
    }

    public void setTeams(Team[] teams) {
        this.teams = teams;
    }

    public Call getCurrentHighestCall() {
        return currentHighestCall;
    }

    public void setCurrentHighestCall(Call currentHighestCall) {
        this.currentHighestCall = currentHighestCall;
    }

    @Override
    public String toString() {
        return "Game{" +
                "gameid=" + gameid +
                ", gameType=" + gameType +
                ", players=" + players +
                ", inviteLink=" + inviteLink +
                ", currentTrump=" + currentTrump +
                ", maxNumberOfPlayers=" + maxNumberOfPlayers +
                ", teams=" + Arrays.toString(teams) +
                ", currentHighestCall=" + currentHighestCall +
                '}';
    }

//endregion
}
