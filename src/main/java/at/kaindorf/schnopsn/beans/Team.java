package at.kaindorf.schnopsn.beans;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

public class Team {
    private int currentScore;
    private int currentBummerl;

    public Team(int currentScore, int currentBummerl) {
        this.currentScore = currentScore;
        this.currentBummerl = currentBummerl;
    }

    // region <getter, setter, toString>

    public int getCurrentScore() {
        return currentScore;
    }

    public int getCurrentBummerl() {
        return currentBummerl;
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
                ", currentScore=" + currentScore +
                ", currentBummerl=" + currentBummerl +
                '}';
    }

    // endregion
}
