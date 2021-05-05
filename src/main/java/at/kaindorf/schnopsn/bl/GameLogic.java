package at.kaindorf.schnopsn.bl;

import at.kaindorf.schnopsn.beans.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

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
        Game game = null;
        Team[] teams = new Team[2];
        for (int i = 0; i < 2; i++) {
            teams[i]= new Team(0,0);
        }
        if(gameType==GameType._2ERSCHNOPSN) {
            game = new Game(UUID.randomUUID(), gameType, new ArrayList<>(), null, null, 2,teams,Call.NORMAL);
        }
        else if(gameType==GameType._4ERSCHNOPSN){
            game = new Game(UUID.randomUUID(), gameType, new ArrayList<>(), null, null, 4,teams,Call.NORMAL);
        }
        player.setPlayerNumber(1);
        game.getPlayers().add(player);
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

    public static Player findPlayer(List<Player> activePlayers,String playerID){
        UUID realPlayerID = UUID.fromString(playerID);
        return activePlayers.stream().filter(player1 -> player1.getPlayerid().equals(realPlayerID)).findFirst().orElse(null);
    }

    public static Game findGame(List<Game> activeGames, String gameID){
        UUID realGameID = UUID.fromString(gameID);
        return activeGames.stream().filter(game1 -> game1.getGameid().equals(realGameID)).findFirst().orElse(null);
    }

    public boolean isCallHigher(Game game, Call call, Player player){
        Call actualHighestcall = game.getCurrentHighestCall();
        if(call.getValue() > actualHighestcall.getValue()) {
            game.setCurrentHighestCall(call);
            try {
                game.getPlayers().stream().filter(player1 -> player1.isPlaysCall()).findFirst().get().setPlaysCall(false);
            } catch(NoSuchElementException e){
                //noch keiner was angesagt
            }
            player.setPlaysCall(true);
            return true;
        }
        return false;
    }


}
