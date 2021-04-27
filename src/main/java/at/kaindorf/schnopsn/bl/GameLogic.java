package at.kaindorf.schnopsn.bl;

import at.kaindorf.schnopsn.beans.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

public class GameLogic {

    public Game createGame(GameType gameType, Player player){
        Game game = new Game(UUID.randomUUID(),gameType,new ArrayList<>(),null, null);
        Team team1 = new Team(UUID.randomUUID(),new ArrayList<>(),0,0);
        Team team2 = new Team(UUID.randomUUID(),new ArrayList<>(),0,0);

        team1.getPlayers().add(player);
        game.getTeams().add(team1);
        game.getTeams().add(team2);
        game.setInviteLink(generateInviteLink(game));
        return game;
    }

    public URL generateInviteLink(Game game){
        URL inviteLink;
        try {
            inviteLink = new URL("http://localhost:8080/"+game.getGameid());
        } catch (MalformedURLException e) {
            return null;
        }
        return inviteLink;
    }


}
