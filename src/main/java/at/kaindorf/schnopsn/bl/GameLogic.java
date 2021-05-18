package at.kaindorf.schnopsn.bl;

import at.kaindorf.schnopsn.beans.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class GameLogic {

    private List<Card> allCards = new ArrayList<>();

    public GameLogic() {
        try {
            allCards.add(new Card("Bur", 2, new URL("http://link"), Color.KARO));
            allCards.add(new Card("Dame", 3, new URL("http://link"), Color.KARO));
            allCards.add(new Card("König", 4, new URL("http://link"), Color.KARO));
            allCards.add(new Card("Zehner", 10, new URL("http://link"), Color.KARO));
            allCards.add(new Card("Ass", 11, new URL("http://link"), Color.KARO));

            allCards.add(new Card("Dame", 3, new URL("http://link"), Color.KREUZ));
            allCards.add(new Card("Bur", 2, new URL("http://link"), Color.KREUZ));
            allCards.add(new Card("König", 4, new URL("http://link"), Color.KREUZ));
            allCards.add(new Card("Zehner", 10, new URL("http://link"), Color.KREUZ));
            allCards.add(new Card("Ass", 11, new URL("http://link"), Color.KREUZ));

            allCards.add(new Card("Bur", 2, new URL("http://link"), Color.PICK));
            allCards.add(new Card("Dame", 3, new URL("http://link"), Color.PICK));
            allCards.add(new Card("König", 4, new URL("http://link"), Color.PICK));
            allCards.add(new Card("Zehner", 10, new URL("http://link"), Color.PICK));
            allCards.add(new Card("Ass", 11, new URL("http://link"), Color.PICK));

            allCards.add(new Card("Bur", 2, new URL("http://link"), Color.HERZ));
            allCards.add(new Card("Dame", 3, new URL("http://link"), Color.HERZ));
            allCards.add(new Card("König", 4, new URL("http://link"), Color.HERZ));
            allCards.add(new Card("Zehner", 10, new URL("http://link"), Color.HERZ));
            allCards.add(new Card("Ass", 11, new URL("http://link"), Color.HERZ));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public Game createGame(GameType gameType, Player player) {
        Game game = null;
        Team[] teams = new Team[2];
        for (int i = 0; i < 2; i++) {
            teams[i] = new Team(0, 0);
        }
        if (gameType == GameType._2ERSCHNOPSN) {
            game = new Game(UUID.randomUUID(), gameType, new ArrayList<>(), null, null, 2, teams, Call.NORMAL, new LinkedHashMap<Player, Card>());
        } else if (gameType == GameType._4ERSCHNOPSN) {
            game = new Game(UUID.randomUUID(), gameType, new ArrayList<>(), null, null, 4, teams, Call.NORMAL, new LinkedHashMap<Player, Card>());
        }
        player.setPlayerNumber(1);
        game.getPlayers().add(player);
        game.setInviteLink(generateInviteLink(game));
        return game;
    }

    public URL generateInviteLink(Game game) {
        URL inviteLink;
        try {
            inviteLink = new URL("http://localhost:3000/" + game.getGameID());
        } catch (MalformedURLException e) {
            return null;
        }
        return inviteLink;
    }

    public static Player findPlayer(List<Player> activePlayers, String playerID) {
        UUID realPlayerID = UUID.fromString(playerID);
        return activePlayers.stream().filter(player1 -> player1.getPlayerID().equals(realPlayerID)).findFirst().orElse(null);
    }

    public static Game findGame(List<Game> activeGames, String gameID) {
        UUID realGameID = UUID.fromString(gameID);
        return activeGames.stream().filter(game1 -> game1.getGameID().equals(realGameID)).findFirst().orElse(null);
    }

    public Card getCard(String color, int value) {
        Color realColor = Color.valueOf(color.toUpperCase());
        return allCards.stream().filter(card -> card.getColor().equals(realColor) && card.getValue() == value).findFirst().get();
    }

    public boolean isCallHigher(Game game, Call call, Player player) {
        Call actualHighestcall = game.getCurrentHighestCall();
        if (call.getValue() > actualHighestcall.getValue()) {
            game.setCurrentHighestCall(call);
            try {
                game.getPlayers().stream().filter(player1 -> player1.isPlaysCall()).findFirst().get().setPlaysCall(false);
            } catch (NoSuchElementException e) {
                //noch keiner was angesagt
            }
            player.setPlaysCall(true);
            return true;
        }
        return false;
    }

    public UUID getPlayerWithHighestCard(Map<Player, Card> playMap, Color trump) {
        List<Card> playCards = new ArrayList<>();
        
        for (Player player : playMap.keySet()) {
            playCards.add(playMap.get(player));
        }
        Color firstColor = playCards.get(0).getColor();
        //playCards.removeIf(card -> card.getValue())
        System.out.println(playCards);
        int count = 0;
        while (playCards.size() > 1) {
            Card temp = playCards.get(count);
            for (int j = 0; j < playCards.size(); j++) {
                if (temp.getColor() == playCards.get(j).getColor() && temp.getValue() < playCards.get(j).getValue()) {
                    playCards.remove(temp);
                    count--;
                    break;
                } else if (temp.getColor() != trump && playCards.get(j).getColor() == trump) {
                    playCards.remove(temp);
                    count--;
                    break;
                } else if (trump == null && temp.getColor() != firstColor) {
                    playCards.remove(temp);
                    count--;
                    break;
                }
            }
            count++;
        }

        for (Player player : playMap.keySet()) {
            if (playMap.get(player) == playCards.get(0)) {
                return player.getPlayerID();
            }
        }
        return null;
    }
    public boolean trumpNeeded(Call call){
        switch(call){
            case BETTLER,ASSENBETTLER,PLAUDERER,GANG,ZEHNERGANG:
                return false;
            default:
                return true;
        }
    }

}
