package at.kaindorf.schnopsn.beans;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

public class Team {
    private int currentScore;
    private int currentBummerl;
    private List<Player> players;

    public Team(int currentScore, int currentBummerl, List<Player> players) {
        this.currentScore = currentScore;
        this.currentBummerl = currentBummerl;
        this.players = players;
    }

    public Team() {
    }

    // region <getter, setter, toString>

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

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    @Override
    public String toString() {
        return "Team{" +
                "currentScore=" + currentScore +
                ", currentBummerl=" + currentBummerl +
                ", players=" + players +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Team team = (Team) o;

        if (currentScore != team.currentScore) return false;
        if (currentBummerl != team.currentBummerl) return false;
        return players != null ? players.equals(team.players) : team.players == null;
    }

    @Override
    public int hashCode() {
        int result = currentScore;
        result = 31 * result + currentBummerl;
        result = 31 * result + (players != null ? players.hashCode() : 0);
        return result;
    }

    // endregion
}
