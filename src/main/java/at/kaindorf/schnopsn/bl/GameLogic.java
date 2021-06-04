package at.kaindorf.schnopsn.bl;

import at.kaindorf.schnopsn.api.GameStorage;
import at.kaindorf.schnopsn.beans.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class GameLogic {

    private List<Card> allCards = new ArrayList<>();
    private GameStorage storage = GameStorage.getInstance();

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

    //erstellt ein Game
    public Game createGame(GameType gameType, Player player) {
        Game game = null;
        List<Player> players = new ArrayList<>();
        players.add(player);
        List<Team> teams = new ArrayList<>();
        teams.add(new Team(0, 0, 0, players));
        teams.add(new Team(0, 0, 0, new ArrayList<>()));

        if (gameType == GameType._2ERSCHNOPSN) {
            game = new Game(UUID.randomUUID(), gameType, null, null, 2, teams, Call.NORMAL, new LinkedHashMap<Player, Card>(), allCards,0);
        } else if (gameType == GameType._4ERSCHNOPSN) {
            game = new Game(UUID.randomUUID(), gameType, null, null, 4, teams, Call.NORMAL, new LinkedHashMap<Player, Card>(), allCards,0);
        }
        player.setPlayerNumber(1);
        game.setInviteLink(generateInviteLink(game));
        return game;
    }

    //generiert einen invitelink zu einem Game
    public URL generateInviteLink(Game game) {
        URL inviteLink;
        try {
            inviteLink = new URL("http://localhost:3000/" + game.getGameID());
        } catch (MalformedURLException e) {
            return null;
        }
        return inviteLink;
    }

    //sucht sich einen Spieler aus allen aktiven Spielern
    public static Player findPlayer(List<Player> activePlayers, String playerID) throws IllegalArgumentException {
        UUID realPlayerID = UUID.fromString(playerID);
        return activePlayers.stream().filter(player1 -> player1.getPlayerID().equals(realPlayerID)).findFirst().orElse(null);
    }

    //sucht sich ein Game aus allen aktiven Games
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

    //überprüft ob eine Ansage höher als die aktuell höchste Ansage in einem Spiel ist
    public boolean isCallHigher(Game game, Call call, Player player) {
        Call currentHighestCall = game.getCurrentHighestCall();
        if (call.getValue() > currentHighestCall.getValue()) {
            game.setCurrentHighestCall(call);
            try {
                for (Team team : game.getTeams()) {
                    team.getPlayers().stream().filter(Player::isPlaysCall).findFirst().get().setPlaysCall(false);
                }

            } catch (NoSuchElementException e) {
                e.printStackTrace();
                //noch keiner was angesagt
            }
            player.setPlaysCall(true);
            return true;
        }
        return false;
    }

    //wartet bis alle Spieler ausgespielt haben und holt sich dann den Gewinner
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

    //definiertwelcher Spieler den Stich bekommt (welche Karte die Höchste ist)
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

    //vergibt punkte für ansagen
    public void awardForPoints4erSchnopsn(Player winner, Game game) {
        Call call = game.getCurrentHighestCall();
        int currentGameScore = 0;
        if (winner.getPlayerNumber() % 2 != 0) {
            currentGameScore = game.getTeams().get(0).getCurrentScore();
            currentGameScore += call.getValue();
            game.getTeams().get(0).setCurrentGameScore(currentGameScore);
        } else {
            currentGameScore = game.getTeams().get(1).getCurrentScore();
            currentGameScore += call.getValue();
            game.getTeams().get(1).setCurrentGameScore(currentGameScore);
        }
    }

    public boolean endOfRound2erSchnopsn(Player winner, Game game, int looserPoints) {
        int winnerTeam = 0;
        int currentGameScore = 0;
        if (winner.getPlayerNumber() % 2 != 0) {
            winnerTeam = 1;
        }

        if (looserPoints == 0) {
            currentGameScore = game.getTeams().get(winnerTeam).getCurrentGameScore();
            currentGameScore += 3;
            game.getTeams().get(winnerTeam).setCurrentGameScore(currentGameScore);
        } else if (looserPoints < 33) {
            currentGameScore = game.getTeams().get(winnerTeam).getCurrentGameScore();
            currentGameScore += 2;
            game.getTeams().get(winnerTeam).setCurrentGameScore(currentGameScore);
        } else {
            currentGameScore = game.getTeams().get(winnerTeam).getCurrentGameScore();
            currentGameScore += 1;
            game.getTeams().get(winnerTeam).setCurrentGameScore(currentGameScore);
        }
        if (game.getTeams().get(winnerTeam).getCurrentGameScore() > 6) {
            return true;
        } else {
            return false;
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

    //define which player is the next one who is allowed to call trump
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

    //give Crads for each player
    public Map<Player, List<Card>> giveOutCards(Game game, int anz) {
        game.setAvailableCards(new ArrayList<>(allCards));
        Map<Player, List<Card>> playerCardMap = new LinkedHashMap<>();
        for (Team team : game.getTeams()) {
            team.getPlayers().forEach(player -> {
                List<Card> playerCardList = new ArrayList<>();
                for (int i = 0; i < anz; i++) {
                    if (playerCardMap.containsKey(player)) {
                        playerCardMap.get(player).add(getRandomCard(game.getAvailableCards(), false));
                    } else {
                        playerCardList.add(getRandomCard(game.getAvailableCards(), false));
                        playerCardMap.put(player, playerCardList);
                    }
                }
            });
        }
        return playerCardMap;
    }

    public Card getTrumpCard(Game game) {
        return getRandomCard(game.getAvailableCards(), true);
    }

    // get one random Card of the available Cards of a game
    public Card getRandomCard(List<Card> availableCards, boolean isTrumpCard) {
        Random rand = new Random();
        int index = 0;
        if (availableCards.size() == 1) {
            return availableCards.get(0);
        } else {
            index = rand.nextInt(availableCards.size() - 1);
        }
        Card card = availableCards.get(index);
        availableCards.remove(index);
        if (isTrumpCard) {
            availableCards.add(card);
        }
        return card;
    }

    //sendData toPlayers after one has played out a card
    public void sendStingDataToPlayers(Game game, UUID winnerID) {
        List<Card> cards = game.getPlayedCards().values().stream().collect(Collectors.toList());
        ObjectMapper mapper = new ObjectMapper();

        if (winnerID == null) {
            //Sends a message to all players, about who will play next
            game.getTeams().forEach(team -> team.getPlayers().forEach(player1 -> {
                try {
                    if (player1.getPlayerNumber() == player1.getPlayerNumber() % game.getMaxNumberOfPlayers() + 1) {
                        player1.setMyTurn(true);
                        player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("myTurn", true))));
                    } else {
                        player1.setMyTurn(false);
                        player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("myTurn", false))));
                    }
                    player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("playedCards", cards))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        } else {
            Player winner = GameLogic.findPlayer(storage.getActivePlayers(), winnerID.toString());

            int points = 0;
            for (Card card1 : cards) {
                points += card1.getValue();
            }
            for (Player player1 : game.getPlayedCards().keySet()) {
                try {
                    //schicke an den gewinner seinen Stich und an Verlierer, dass der Gewinner den Stich bekommt
                    if (player1.getPlayerID() == winnerID) {

                        player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("sting", cards))));
                        player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("stingPoints", points))));
                        player1.setMyTurn(true);
                        player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("myTurn", player1.isMyTurn()))));
                        //Punkte setzten
                        game.getTeams().get(player1.getPlayerNumber() % 2).setCurrentScore(game.getTeams().get(player1.getPlayerNumber() % 2).getCurrentGameScore() + points);


                    } else {
                        player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("winner", winner.getPlayerName()))));
                        player1.setMyTurn(false);
                        player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("myTurn", player1.isMyTurn()))));
                    }
                    //karte ziehen und zurückschicken nur bei 2er schnopsn

                    if (game.getGameType() == GameType._2ERSCHNOPSN) {
                        //Wenn man 66 Punkte hat oder keine Karten mehr zum ziehen hat
                        if (game.getTeams().get(player1.getPlayerNumber() % 2).getCurrentScore() > 65 || game.getAvailableCards().size() == 0) {
                            for (Player player3 : game.getPlayedCards().keySet()) {
                                player3.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("winnerOfRound", game.getTeams().get(player1.getPlayerNumber() % 2).getPlayers().get(0).getPlayerName()))));
                            }
                            //Punkte vergeben und überprüfen ob Bummerl gegeben wird
                            if (endOfRound2erSchnopsn(game.getTeams().get(player1.getPlayerNumber() % 2).getPlayers().get(0), game, game.getTeams().get(player1.getPlayerNumber() % 2 + 1).getCurrentScore())) {
                                game.getTeams().get(player1.getPlayerNumber() % 2).setCurrentBummerl(game.getTeams().get(player1.getPlayerNumber() % 2).getCurrentBummerl() + 1);
                                Map<String, Integer> bummerl = new LinkedHashMap<>();
                                //alle Bummerl holen
                                for (Team team : game.getTeams()) {
                                    bummerl.put(team.getPlayers().get(0).getPlayerName(), team.getCurrentBummerl());
                                }
                                //Bummerlstand zurückschicken
                                game.getTeams().forEach(team -> team.getPlayers().forEach(player4 -> {
                                    try {
                                        player4.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("bummerl",bummerl))));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }));
                                for (Team team:game.getTeams()) {
                                    team.setCurrentGameScore(0);
                                }
                            } else {
                                //GameScore zurückschicken
                                Map<String, Integer> gamescore = new LinkedHashMap<>();
                                //alle Gamescore holen
                                for (Team team : game.getTeams()) {
                                    gamescore.put(team.getPlayers().get(0).getPlayerName(), team.getCurrentGameScore());
                                }
                                //Gamescorestand zurückschicken
                                game.getTeams().forEach(team -> team.getPlayers().forEach(player4 -> {
                                    try {
                                        player4.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("gamescore",gamescore))));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }));
                            }
                        } else {
                            player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("newCard",getRandomCard(game.getAvailableCards(), false)))));
                        }

                    }
                    //beim 4er Schnopsn überprüfen ob ansage durchgeht und Punkte vergeben
                    else if (game.getGameType()==GameType._4ERSCHNOPSN){
                        Call playedCall = game.getCurrentHighestCall();
                        Player callPlayer;

                    }

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}