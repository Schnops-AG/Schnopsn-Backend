package at.kaindorf.schnopsn.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;
import java.util.*;

public class Game {
    private UUID gameID;
    private GameType gameType;
    private URL inviteLink;
    private Color currentTrump;
    private int maxNumberOfPlayers;
    private List<Team> teams;
    private Call currentHighestCall;
    private Map<Player,Card> playedCards;

    public Game(UUID gameID, GameType gameType, URL inviteLink, Color currentTrump, int maxNumberOfPlayers, List<Team> teams, Call currentHighestCall, Map<Player, Card> playedCards) {
        this.gameID = gameID;
        this.gameType = gameType;
        this.inviteLink = inviteLink;
        this.currentTrump = currentTrump;
        this.maxNumberOfPlayers = maxNumberOfPlayers;
        this.teams = teams;
        this.currentHighestCall = currentHighestCall;
        this.playedCards = playedCards;
    }

    public Game() {
    }

    // region <getter, setter, toString>

    public UUID getGameID() {
        return gameID;
    }

    public void setGameID(UUID gameID) {
        this.gameID = gameID;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
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

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public Call getCurrentHighestCall() {
        return currentHighestCall;
    }

    public void setCurrentHighestCall(Call currentHighestCall) {
        this.currentHighestCall = currentHighestCall;
    }

    public Map<Player, Card> getPlayedCards() {
        return playedCards;
    }

    public void setPlayedCards(Map<Player, Card> playedCards) {
        this.playedCards = playedCards;
    }

    @Override
    public String toString() {
        return "Game{" +
                "gameID=" + gameID +
                ", gameType=" + gameType +
                ", inviteLink=" + inviteLink +
                ", currentTrump=" + currentTrump +
                ", maxNumberOfPlayers=" + maxNumberOfPlayers +
                ", teams=" + teams +
                ", currentHighestCall=" + currentHighestCall +
                ", playedCards=" + playedCards +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Game game = (Game) o;

        if (maxNumberOfPlayers != game.maxNumberOfPlayers) return false;
        if (gameID != null ? !gameID.equals(game.gameID) : game.gameID != null) return false;
        if (gameType != game.gameType) return false;
        if (inviteLink != null ? !inviteLink.equals(game.inviteLink) : game.inviteLink != null) return false;
        if (currentTrump != game.currentTrump) return false;
        if (teams != null ? !teams.equals(game.teams) : game.teams != null) return false;
        if (currentHighestCall != game.currentHighestCall) return false;
        return playedCards != null ? playedCards.equals(game.playedCards) : game.playedCards == null;
    }

    @Override
    public int hashCode() {
        int result = gameID != null ? gameID.hashCode() : 0;
        result = 31 * result + (gameType != null ? gameType.hashCode() : 0);
        result = 31 * result + (inviteLink != null ? inviteLink.hashCode() : 0);
        result = 31 * result + (currentTrump != null ? currentTrump.hashCode() : 0);
        result = 31 * result + maxNumberOfPlayers;
        result = 31 * result + (teams != null ? teams.hashCode() : 0);
        result = 31 * result + (currentHighestCall != null ? currentHighestCall.hashCode() : 0);
        result = 31 * result + (playedCards != null ? playedCards.hashCode() : 0);
        return result;
    }

    //endregion
}
