package at.kaindorf.schnopsn.beans;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Team {
    private int currentGameScore;
    private int currentScore;
    private int currentBummerl;
    private int buffer;
    private List<Player> players;


    public Team(int currentGameScore, int currentScore, int currentBummerl, int buffer, List<Player> players) {
        this.currentGameScore = currentGameScore;
        this.currentScore = currentScore;
        this.currentBummerl = currentBummerl;
        this.buffer = buffer;
        this.players = players;
    }

    public Team() {
    }

    // region <getter, setter, toString>

    public int getCurrentGameScore() {
        return currentGameScore;
    }

    public void setCurrentGameScore(int currentGameScore) {
        this.currentGameScore = currentGameScore;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }

    public int getCurrentBummerl() {
        return currentBummerl;
    }

    public void setCurrentBummerl(int currentBummerl) {
        this.currentBummerl = currentBummerl;
    }

    public int getBuffer() {
        return buffer;
    }

    public void setBuffer(int buffer) {
        this.buffer = buffer;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return currentGameScore == team.currentGameScore && currentScore == team.currentScore && currentBummerl == team.currentBummerl && Objects.equals(players, team.players);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentGameScore, currentScore, currentBummerl, players);
    }

    @Override
    public String toString() {
        return "Team{" +
                "currentGameScore=" + currentGameScore +
                ", currentScore=" + currentScore +
                ", currentBummerl=" + currentBummerl +
                ", players=" + players +
                '}';
    }


    // endregion
}
