package at.kaindorf.schnopsn.bl;

import at.kaindorf.schnopsn.beans.*;
import org.springframework.http.ResponseEntity;

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
        List<Player> players = new ArrayList<>();
        players.add(player);
        List<Team> teams = new ArrayList<>();
        teams.add(new Team(0, 0, players));
        teams.add(new Team(0, 0, new ArrayList<>()));

        if (gameType == GameType._2ERSCHNOPSN) {
            game = new Game(UUID.randomUUID(), gameType, null, null, 2, teams, Call.NORMAL, new LinkedHashMap<Player, Card>(),allCards);
        } else if (gameType == GameType._4ERSCHNOPSN) {
            game = new Game(UUID.randomUUID(), gameType, null, null, 4, teams, Call.NORMAL, new LinkedHashMap<Player, Card>(),allCards);
        }
        player.setPlayerNumber(1);
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

    public static Player findPlayer(List<Player> activePlayers, String playerID) throws IllegalArgumentException {
        UUID realPlayerID = UUID.fromString(playerID);
        return activePlayers.stream().filter(player1 -> player1.getPlayerID().equals(realPlayerID)).findFirst().orElse(null);
    }

    public static Game findGame(List<Game> activeGames, String gameID) {
        UUID realGameID = UUID.fromString(gameID);
        return activeGames.stream().filter(game1 -> game1.getGameID().equals(realGameID)).findFirst().orElse(null);
    }

    public static int getCurrentNumberOfPlayers(Game game) {

        return game.getTeams().get(0).getPlayers().size() + game.getTeams().get(1).getPlayers().size();
    }

    public Card getCard(String color, int value) {
        Color realColor = Color.valueOf(color.toUpperCase());
        return allCards.stream().filter(card -> card.getColor().equals(realColor) && card.getValue() == value).findFirst().get();
    }

    public boolean isCallHigher(Game game, Call call, Player player) {
        Call currentHighestCall = game.getCurrentHighestCall();
        if (call.getValue() > currentHighestCall.getValue()) {
            game.setCurrentHighestCall(call);
            try {
                for (Team team : game.getTeams()) {
                    team.getPlayers().stream().filter(Player::isPlaysCall).findFirst().get().setPlaysCall(false);
                }

            } catch (NoSuchElementException e) {
                //noch keiner was angesagt
            }
            player.setPlaysCall(true);
            return true;
        }
        return false;
    }

    public UUID makeRightMove(Game game, Card card, Player player) {
        switch (game.getCurrentHighestCall()) {
            case BETTLER, ASSENBETTLER, PLAUDERER:
                if (game.getPlayedCards().size() < game.getMaxNumberOfPlayers() - 1 && game.getPlayedCards().keySet().stream().filter(player1 -> player1.getPlayerID() == player.getPlayerID()).findFirst().orElse(null) == null) {
                    game.getPlayedCards().put(player, card);
                }
                if (game.getPlayedCards().size() == game.getMaxNumberOfPlayers() - 1) {
                    if (trumpNeeded(game.getCurrentHighestCall())) {
                        return getPlayerWithHighestCard(game.getPlayedCards(), game.getCurrentTrump());
                    } else {
                        return getPlayerWithHighestCard(game.getPlayedCards(), null);
                    }
                }
                break;

            default:
                if (game.getPlayedCards().size() < game.getMaxNumberOfPlayers() && game.getPlayedCards().keySet().stream().filter(player1 -> player1.getPlayerID() == player.getPlayerID()).findFirst().orElse(null) == null) {
                    game.getPlayedCards().put(player, card);
                }
                if (game.getPlayedCards().size() == game.getMaxNumberOfPlayers()) {
                    if (trumpNeeded(game.getCurrentHighestCall())) {
                        getPlayerWithHighestCard(game.getPlayedCards(), game.getCurrentTrump());
                    } else {
                        return getPlayerWithHighestCard(game.getPlayedCards(), null);
                    }
                }
                break;
        }
        return null;
    }

    public UUID getPlayerWithHighestCard(Map<Player, Card> playMap, Color trump) {
        List<Card> playCards = new ArrayList<>();

        for (Player player : playMap.keySet()) {
            playCards.add(playMap.get(player));
        }
        Color firstColor = playCards.get(0).getColor();

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

    public boolean trumpNeeded(Call call) {
        return switch (call) {
            case BETTLER, ASSENBETTLER, PLAUDERER, GANG, ZEHNERGANG -> false;
            default -> true;
        };
    }

    public void awardForPoints(Player winner, Game game) {
        Call call = game.getCurrentHighestCall();
        int currentScore = 0;
        if (winner.getPlayerNumber() % 2 != 0) {
            currentScore = game.getTeams().get(0).getCurrentScore();
            currentScore += call.getValue();
            game.getTeams().get(0).setCurrentScore(currentScore);
        } else {
            currentScore = game.getTeams().get(1).getCurrentScore();
            currentScore += call.getValue();
            game.getTeams().get(1).setCurrentScore(currentScore);
        }
    }

    public String getAllCurrentPlayerNames(Game game) {
        String allNames = "";
        for (Team team : game.getTeams()) {
            for (Player player : team.getPlayers()) {
                allNames += player.getPlayerName();
                allNames += ";";
            }
        }
        return allNames;
    }

    public void defineCaller(Game game) {
        int oldCallerNumber = 0;
        for (Team team : game.getTeams()) {
            if (team.getPlayers().stream().filter(Player::isCaller).findFirst().orElse(null) != null) {
                oldCallerNumber = team.getPlayers().stream().filter(Player::isCaller).findFirst().get().getPlayerNumber();
                break;
            }
        }
        final int finalOldCallerNumber = oldCallerNumber;
        game.getTeams().stream().forEach(team -> team.getPlayers().stream().filter(Player::isCaller).findFirst().get().setCaller(false));
        game.getTeams().stream().forEach(team -> team.getPlayers().stream().filter(player -> player.getPlayerNumber() == finalOldCallerNumber % 4 + 1).findFirst().get().setCaller(true));
    }

    public Map<Player, List<Card>> giveOutCards(Game game){
        game.getAvailableCards().clear();
        game.getAvailableCards().addAll(allCards);
        Map<Player, List<Card>> playerCardMap = new LinkedHashMap<>();
        System.out.println(game.getAvailableCards().size());
        for (Team team : game.getTeams()) {
            team.getPlayers().forEach(player -> {
                List<Card> playerCardList = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    if (playerCardMap.containsKey(player)){
                        playerCardMap.get(player).add(getRandomCard(game.getAvailableCards()));
                    }else{
                        playerCardList.add(getRandomCard(game.getAvailableCards()));
                        playerCardMap.put(player,playerCardList);
                    }
                }
            });
        }
        return playerCardMap;
    }

    public Card getTrumpCard(Game game){
        return getRandomCard(game.getAvailableCards());
    }

    public Card getRandomCard(List<Card> availableCards){
        Random rand = new Random();
        System.out.println(availableCards.size());
        int index=rand.nextInt(availableCards.size());
        //Card card = availableCards.get(index);
        //availableCards.remove(index);
        return new Card();
    }
}
