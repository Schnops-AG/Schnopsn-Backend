package at.kaindorf.schnopsn.beans;

import lombok.AllArgsConstructor;
import lombok.Data;

//@Data
//@AllArgsConstructor
public class Statistic {
    private int wins;
    private int looses;
    private int games;
    private GameType gameType;
    private Role rolename;
    private int pointsOverall;

    public Statistic(int wins, int looses, int games, GameType gameType, Role rolename, int pointsOverall) {
        this.wins = wins;
        this.looses = looses;
        this.games = games;
        this.gameType = gameType;
        this.rolename = rolename;
        this.pointsOverall = pointsOverall;
    }

    // region <getter, setter, toString>

    public int getWins() {
        return wins;
    }

    public int getLooses() {
        return looses;
    }

    public int getGames() {
        return games;
    }

    public GameType getGameType() {
        return gameType;
    }

    public Role getRolename() {
        return rolename;
    }

    public int getPointsOverall() {
        return pointsOverall;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void setLooses(int looses) {
        this.looses = looses;
    }

    public void setGames(int games) {
        this.games = games;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public void setRolename(Role rolename) {
        this.rolename = rolename;
    }

    public void setPointsOverall(int pointsOverall) {
        this.pointsOverall = pointsOverall;
    }

    @Override
    public String toString() {
        return "Statistic{" +
                "wins=" + wins +
                ", looses=" + looses +
                ", games=" + games +
                ", gameType=" + gameType +
                ", rolename=" + rolename +
                ", pointsOverall=" + pointsOverall +
                '}';
    }

    // endregion
}
