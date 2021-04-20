package at.kaindorf.schnopsn.beans;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

public class Team {
    private UUID teamid;
    private List<Player> players;
    private int currentScore;
    private int currentBummerl;

    public Team(UUID teamid, List<Player> players, int currentScore, int currentBummerl) {
        this.teamid = teamid;
        this.players = players;
        this.currentScore = currentScore;
        this.currentBummerl = currentBummerl;
    }

    public UUID getTeamid() {
        return teamid;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public int getCurrentBummerl() {
        return currentBummerl;
    }

    public void setTeamid(UUID teamid) {
        this.teamid = teamid;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }

    public void setCurrentBummerl(int currentBummerl) {
        this.currentBummerl = currentBummerl;
    }

    @Override
    public String toString() {
        return "Team{" +
                "teamid=" + teamid +
                ", players=" + players +
                ", currentScore=" + currentScore +
                ", currentBummerl=" + currentBummerl +
                '}';
    }
}
