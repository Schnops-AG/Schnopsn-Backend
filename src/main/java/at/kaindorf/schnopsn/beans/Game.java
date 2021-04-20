package at.kaindorf.schnopsn.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Game {
    private UUID gameid;
    private GameType gameType;
    private List<Team> teams;
    private URL inviteLink;
    private Color currentTrump;

    public Game(UUID gameid, GameType gameType, List<Team> teams, URL inviteLink, Color currentTrump) {
        this.gameid = gameid;
        this.gameType = gameType;
        this.teams = teams;
        this.inviteLink = inviteLink;
        this.currentTrump = currentTrump;
    }

    public UUID getGameid() {
        return gameid;
    }

    public GameType getGameType() {
        return gameType;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public URL getInviteLink() {
        return inviteLink;
    }

    public Color getCurrentTrump() {
        return currentTrump;
    }

    public void setGameid(UUID gameid) {
        this.gameid = gameid;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public void setInviteLink(URL inviteLink) {
        this.inviteLink = inviteLink;
    }

    public void setCurrentTrump(Color currentTrump) {
        this.currentTrump = currentTrump;
    }

    @Override
    public String toString() {
        return "Game{" +
                "gameid=" + gameid +
                ", gameType=" + gameType +
                ", teams=" + teams +
                ", inviteLink=" + inviteLink +
                ", currentTrump=" + currentTrump +
                '}';
    }
}
