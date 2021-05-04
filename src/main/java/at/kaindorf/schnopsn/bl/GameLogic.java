package at.kaindorf.schnopsn.bl;

import at.kaindorf.schnopsn.beans.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GameLogic {

    private List<Card> allCards = new ArrayList<>();

    public GameLogic(){
        try {
            allCards.add(new Card("Bur",2,new URL("http://link"),Color.KARO));
            allCards.add(new Card("Dame",3,new URL("http://link"),Color.KARO));
            allCards.add(new Card("König",4,new URL("http://link"),Color.KARO));
            allCards.add(new Card("Zehner",10,new URL("http://link"),Color.KARO));
            allCards.add(new Card("Ass",11,new URL("http://link"),Color.KARO));

            allCards.add(new Card("Dame",3,new URL("http://link"),Color.KREUZ));
            allCards.add(new Card("Bur",2,new URL("http://link"),Color.KREUZ));
            allCards.add(new Card("König",4,new URL("http://link"),Color.KREUZ));
            allCards.add(new Card("Zehner",10,new URL("http://link"),Color.KREUZ));
            allCards.add(new Card("Ass",11,new URL("http://link"),Color.KREUZ));

            allCards.add(new Card("Bur",2,new URL("http://link"),Color.PICK));
            allCards.add(new Card("Dame",3,new URL("http://link"),Color.PICK));
            allCards.add(new Card("König",4,new URL("http://link"),Color.PICK));
            allCards.add(new Card("Zehner",10,new URL("http://link"),Color.PICK));
            allCards.add(new Card("Ass",11,new URL("http://link"),Color.PICK));

            allCards.add(new Card("Bur",2,new URL("http://link"),Color.HERZ));
            allCards.add(new Card("Dame",3,new URL("http://link"),Color.HERZ));
            allCards.add(new Card("König",4,new URL("http://link"),Color.HERZ));
            allCards.add(new Card("Zehner",10,new URL("http://link"),Color.HERZ));
            allCards.add(new Card("Ass",11,new URL("http://link"),Color.HERZ));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

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



    public UUID choosePlayerWhoMakeHighestCall(Map<String, String> result){
            UUID playerWithHighestAnsage = null;
            int highestVal = 0;
            for (String id : result.keySet()) {
                //System.out.println(result.get(id));
                int value=0;
                    value = Call.valueOf(result.get(id).toUpperCase()).getValue();
                    if (value > highestVal) {
                        highestVal = value;
                        playerWithHighestAnsage = UUID.fromString(id);
                    }
            }
            return playerWithHighestAnsage;
    }

    /*public UUID choosePlayerWhoGetsStich(Map<String,String> result,String trumpf){

    }*/

}
