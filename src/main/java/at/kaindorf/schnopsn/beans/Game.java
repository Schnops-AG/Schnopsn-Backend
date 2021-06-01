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
    private List<Card> availableCards;

    public Game(UUID gameID, GameType gameType, URL inviteLink, Color currentTrump, int maxNumberOfPlayers, List<Team> teams, Call currentHighestCall, Map<Player, Card> playedCards, List<Card> availableCards) {
        this.gameID = gameID;
        this.gameType = gameType;
        this.inviteLink = inviteLink;
        this.currentTrump = currentTrump;
        this.maxNumberOfPlayers = maxNumberOfPlayers;
        this.teams = teams;
        this.currentHighestCall = currentHighestCall;
        this.playedCards = playedCards;
        this.availableCards = availableCards;
    }

    public Game() {
    }

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

    public List<Card> getAvailableCards() {
        return availableCards;
    }

    public void setAvailableCards(List<Card> availableCards) {
        this.availableCards = availableCards;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return maxNumberOfPlayers == game.maxNumberOfPlayers && gameID.equals(game.gameID) && gameType == game.gameType && inviteLink.equals(game.inviteLink) && currentTrump == game.currentTrump && teams.equals(game.teams) && currentHighestCall == game.currentHighestCall && playedCards.equals(game.playedCards) && availableCards.equals(game.availableCards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameID, gameType, inviteLink, currentTrump, maxNumberOfPlayers, teams, currentHighestCall, playedCards, availableCards);
    }
}
