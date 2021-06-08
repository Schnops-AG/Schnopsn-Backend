package at.kaindorf.schnopsn.beans;

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
    //ausgespielte Karten (makeMoveByCall)
    private Map<Player, Card> playedCards;
    //Stapel (welche Karten man noch ziehen kann)
    private List<Card> availableCards;
    private int numberOfCalledCalls;
    private int numberOfStingsPerRound;

    public Game(UUID gameID, GameType gameType, URL inviteLink, Color currentTrump, int maxNumberOfPlayers, List<Team> teams, Call currentHighestCall, Map<Player, Card> playedCards, List<Card> availableCards, int numberOfCalledCalls, int numberOfStingsPerRound) {
        this.gameID = gameID;
        this.gameType = gameType;
        this.inviteLink = inviteLink;
        this.currentTrump = currentTrump;
        this.maxNumberOfPlayers = maxNumberOfPlayers;
        this.teams = teams;
        this.currentHighestCall = currentHighestCall;
        this.playedCards = playedCards;
        this.availableCards = availableCards;
        this.numberOfCalledCalls = numberOfCalledCalls;
        this.numberOfStingsPerRound = numberOfStingsPerRound;
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

    public int getNumberOfCalledCalls() {
        return numberOfCalledCalls;
    }

    public void setNumberOfCalledCalls(int numberOfCalledCalls) {
        this.numberOfCalledCalls = numberOfCalledCalls;
    }

    public int getNumberOfStingsPerRound() {
        return numberOfStingsPerRound;
    }

    public void setNumberOfStingsPerRound(int numberOfStingsPerRound) {
        this.numberOfStingsPerRound = numberOfStingsPerRound;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return maxNumberOfPlayers == game.maxNumberOfPlayers && numberOfCalledCalls == game.numberOfCalledCalls && numberOfStingsPerRound == game.numberOfStingsPerRound && Objects.equals(gameID, game.gameID) && gameType == game.gameType && Objects.equals(inviteLink, game.inviteLink) && currentTrump == game.currentTrump && Objects.equals(teams, game.teams) && currentHighestCall == game.currentHighestCall && Objects.equals(playedCards, game.playedCards) && Objects.equals(availableCards, game.availableCards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameID, gameType, inviteLink, currentTrump, maxNumberOfPlayers, teams, currentHighestCall, playedCards, availableCards, numberOfCalledCalls, numberOfStingsPerRound);
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
                ", availableCards=" + availableCards +
                ", numberOfCalledCalls=" + numberOfCalledCalls +
                ", numberOfStingsPerRound=" + numberOfStingsPerRound +
                '}';
    }
}
