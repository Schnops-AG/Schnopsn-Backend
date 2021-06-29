package at.kaindorf.schnopsn.bl;

import at.kaindorf.schnopsn.api.GameStorage;
import at.kaindorf.schnopsn.beans.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.tool.schema.internal.exec.ScriptTargetOutputToFile;
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
        /**
         *  We add all possible cards to a card list.
         * */
        try {
            allCards.add(new Card("Bur", 2, new URL("http://link"), Color.KARO, true));
            allCards.add(new Card("Dame", 3, new URL("http://link"), Color.KARO, true));
            allCards.add(new Card("König", 4, new URL("http://link"), Color.KARO, true));
            allCards.add(new Card("Zehner", 10, new URL("http://link"), Color.KARO, true));
            allCards.add(new Card("Ass", 11, new URL("http://link"), Color.KARO, true));

            allCards.add(new Card("Dame", 3, new URL("http://link"), Color.KREUZ, true));
            allCards.add(new Card("Bur", 2, new URL("http://link"), Color.KREUZ, true));
            allCards.add(new Card("König", 4, new URL("http://link"), Color.KREUZ, true));
            allCards.add(new Card("Zehner", 10, new URL("http://link"), Color.KREUZ, true));
            allCards.add(new Card("Ass", 11, new URL("http://link"), Color.KREUZ, true));

            allCards.add(new Card("Bur", 2, new URL("http://link"), Color.PICK, true));
            allCards.add(new Card("Dame", 3, new URL("http://link"), Color.PICK, true));
            allCards.add(new Card("König", 4, new URL("http://link"), Color.PICK, true));
            allCards.add(new Card("Zehner", 10, new URL("http://link"), Color.PICK, true));
            allCards.add(new Card("Ass", 11, new URL("http://link"), Color.PICK, true));

            allCards.add(new Card("Bur", 2, new URL("http://link"), Color.HERZ, true));
            allCards.add(new Card("Dame", 3, new URL("http://link"), Color.HERZ, true));
            allCards.add(new Card("König", 4, new URL("http://link"), Color.HERZ, true));
            allCards.add(new Card("Zehner", 10, new URL("http://link"), Color.HERZ, true));
            allCards.add(new Card("Ass", 11, new URL("http://link"), Color.HERZ, true));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a game.
     * */
    public Game createGame(GameType gameType, Player player) {
        Game game = null;
        List<Player> players = new ArrayList<>();
        players.add(player);
        List<Team> teams = new ArrayList<>();
        teams.add(new Team(0, 0, 0, 0, players));
        teams.add(new Team(0, 0, 0, 0, new ArrayList<>()));

        if (gameType == GameType._2ERSCHNOPSN) {
            game = new Game(UUID.randomUUID(), gameType, null, null, 2, teams, Call.NORMAL, new LinkedHashMap<>(), allCards, 0, 0, new LinkedHashMap<>(), false);
        } else if (gameType == GameType._4ERSCHNOPSN) {
            game = new Game(UUID.randomUUID(), gameType, null, null, 4, teams, Call.NORMAL, new LinkedHashMap<>(), allCards, 0, 0, new LinkedHashMap<>(), true);
        }
        player.setPlayerNumber(1);
        game.setInviteLink(generateInviteLink(game));
        return game;
    }

    /**
     *  Method to generate an invite link.
     * */
    public URL generateInviteLink(Game game) {
        URL inviteLink;
        try {
            inviteLink = new URL("http://localhost:3000/" + game.getGameID());
        } catch (MalformedURLException e) {
            return null;
        }
        return inviteLink;
    }

    /**
     * This method returns one specific player.
     * */
    public static Player findPlayer(List<Player> activePlayers, String playerID) throws IllegalArgumentException {
        UUID realPlayerID = UUID.fromString(playerID);
        return activePlayers.stream().filter(player1 -> player1.getPlayerID().equals(realPlayerID)).findFirst().orElse(null);
    }

    /**
     * This method returns one specific game.
     * */
    public static Game findGame(List<Game> activeGames, String gameID) {
        UUID realGameID = UUID.fromString(gameID);
        return activeGames.stream().filter(game1 -> game1.getGameID().equals(realGameID)).findFirst().orElse(null);
    }

    /**
     * This method returns the current number of players.
     * */
    public static int getCurrentNumberOfPlayers(Game game) {
        return game.getTeams().get(0).getPlayers().size() + game.getTeams().get(1).getPlayers().size();
    }

    /**
     * This method returns one card.
     * */
    public Card getCard(String color, int value) {
        Color realColor = Color.valueOf(color.toUpperCase());
        return allCards.stream().filter(card -> card.getColor().equals(realColor) && card.getValue() == value).findFirst().orElse(null);
    }

    /**
     * This method checks if a call is higher than the current highest call in a game.
     * */
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

    /**
     *  This method checks if all moves of every player is correct, depending on the current call.
     * */
    public UUID makeRightMove(Game game, Card card, Player player) {
        switch (game.getCurrentHighestCall()) {
            case BETTLER, ASSENBETTLER, PLAUDERER:
                if (game.getPlayedCards().size() < game.getMaxNumberOfPlayers() - 1 && game.getPlayedCards().keySet().stream().filter(player1 -> player1.getPlayerID() == player.getPlayerID()).findFirst().orElse(null) == null) {
                    game.getPlayedCards().put(player, card);
                    game.getPlayerCardMap().get(player).remove(card);
                }
                if (game.getPlayedCards().size() == game.getMaxNumberOfPlayers() - 1) {
                    if (trumpNeeded(game.getCurrentHighestCall())) {
                        return getPlayerWithHighestCard(game.getPlayedCards(), game.getCurrentTrump(), game);
                    } else {
                        return getPlayerWithHighestCard(game.getPlayedCards(), null, game);
                    }
                }
                break;

            default:
                if (game.getPlayedCards().size() < game.getMaxNumberOfPlayers() && game.getPlayedCards().keySet().stream().filter(player1 -> player1.getPlayerID() == player.getPlayerID()).findFirst().orElse(null) == null) {
                    game.getPlayedCards().put(player, card);
                    game.getPlayerCardMap().get(player).remove(card);
                }
                if (game.getPlayedCards().size() == game.getMaxNumberOfPlayers()) {
                    if (trumpNeeded(game.getCurrentHighestCall())) {
                        return getPlayerWithHighestCard(game.getPlayedCards(), game.getCurrentTrump(), game);
                    } else {
                        return getPlayerWithHighestCard(game.getPlayedCards(), null, game);
                    }
                }
                break;
        }
        return null;
    }

    /**
     *  This method checks which player has played the highest card.
     * */
    public UUID getPlayerWithHighestCard(Map<Player, Card> playMap, Color trump, Game game) {
        List<Card> playCards = new ArrayList<>();

        for (Player player : playMap.keySet()) {
            playCards.add(playMap.get(player));
        }

        //If Zehnergang then Ass has value 1
        if (game.getCurrentHighestCall() == Call.ZEHNERGANG) {
            playCards.forEach(card -> {
                if (card.getValue() == 11) {
                    card.setValue(1);
                }
            });
        }

        Color firstColor = playCards.get(0).getColor();
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
                } else if (temp.getColor() != trump && temp.getColor() != firstColor) {
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

    /**
     *  This method checks if the trump is needed depending of the current call.
     * */
    public boolean trumpNeeded(Call call) {
        return switch (call) {
            case BETTLER, ASSENBETTLER, PLAUDERER, GANG, ZEHNERGANG -> false;
            default -> true;
        };
    }

    /**
     *  This method awards points to the winner team.
     * */
    public boolean awardForPoints4erSchnopsn(Player winner, Game game) {
        Call call = game.getCurrentHighestCall();
        int winnerTeam = 1;
        int looserScore = 0;
        if (winner.getPlayerNumber() % 2 != 0) {
            winnerTeam = 0;
        }
        if (game.getCurrentHighestCall() == Call.NORMAL) {
            looserScore = game.getTeams().get((winner.getPlayerNumber() + 1) % 2).getCurrentScore();
            if (looserScore == 0) {
                game.getTeams().get(winnerTeam).setCurrentGameScore(game.getTeams().get(winnerTeam).getCurrentGameScore() + 3);
            } else if (looserScore < 33) {
                game.getTeams().get(winnerTeam).setCurrentGameScore(game.getTeams().get(winnerTeam).getCurrentGameScore() + 2);
            } else {
                game.getTeams().get(winnerTeam).setCurrentGameScore(game.getTeams().get(winnerTeam).getCurrentGameScore() + 1);
            }
        } else {
            game.getTeams().get(winnerTeam).setCurrentGameScore(game.getTeams().get(winnerTeam).getCurrentGameScore() + call.getValue());
        }

        if (game.getTeams().get(winnerTeam).getCurrentGameScore() >= 24) {
            return true;
        }
        return false;
    }

    /**
     *  Set points for the players and returns true if the round is over.
     * */
    public void endOfRound2erSchnopsn(Player winner, Game game, int looserPoints) {
        int winnerTeam = 1;
        int currentGameScore;
        if (winner.getPlayerNumber() % 2 != 0) {
            winnerTeam = 0;
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
    }

    /**
     *  Returns all player names of a specific game.
     * */
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

    /**
     *  This method returns the current caller of a specific game.
     * */
    public Player getCurrentCaller(Game game) {
        for (Team team : game.getTeams()) {
            for (Player player : team.getPlayers()) {
                if (player.isCaller()) {
                    return player;
                }
            }
        }
        return null;
    }

    /**
     *  Define which player is the next one who is allowed to call trump.
     * */
    public void defineCaller(Game game) {
        int oldCallerNumber = 0;
        for (Team team : game.getTeams()) {
            if (team.getPlayers().stream().filter(Player::isCaller).findFirst().orElse(null) != null) {
                oldCallerNumber = team.getPlayers().stream().filter(Player::isCaller).findFirst().get().getPlayerNumber();
                break;
            }
        }
        for (Team team : game.getTeams()) {
            for (Player player : team.getPlayers()) {
                if (player.isCaller()) {
                    player.setCaller(false);
                }
                if (player.getPlayerNumber() == (oldCallerNumber % game.getMaxNumberOfPlayers()) + 1) {
                    player.setCaller(true);
                }
            }
        }
    }

    /**
     *  This method gives out cards to every player.
     * */
    public Map<Player, List<Card>> giveOutCards(Game game, int anz) {
        if (anz != 2) {
            game.setAvailableCards(new ArrayList<>(allCards));
        }
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

    /**
     *  This method defines the trump card in a 2erSchnopsn game.
     * */
    public Card getTrumpCard(Game game) {
        return getRandomCard(game.getAvailableCards(), true);
    }

    /**
     *  Get one random Card of the available Cards of a game.
     * */
    public Card getRandomCard(List<Card> availableCards, boolean isTrumpCard) {
        Random rand = new Random();
        int index = 0;
        if (availableCards.size() == 1) {
            Card card = availableCards.get(0);
            availableCards.clear();
            return card;
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

    /**
     *  This method swaps your "Bur" trump card with the current trump card in a 2erSchnopsn game.
     * */
    public boolean switchTrumpCard(Game game, Player player) {
        List<Card> handCards = game.getPlayerCardMap().get(player);
        for (Card card : handCards) {
            //Wenn es Trumpf bur is
            if (card.getColor() == game.getCurrentTrump() && card.getValue() == 2) {
                //wenn am Stapel noch 2 Karten vorhanden sind
                if (game.getAvailableCards().size() >= 2) {
                    Card trumpCard = game.getAvailableCards().get(game.getAvailableCards().size() - 1);
                    //lösche Trumpfkarte aus Stapel
                    game.getAvailableCards().remove(game.getAvailableCards().size() - 1);
                    //setze trumpfBur als Trumpfkarte
                    game.getAvailableCards().add(card);
                    game.getPlayerCardMap().get(player).remove(card);
                    game.getPlayerCardMap().get(player).add(trumpCard);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *  Method to check if the player has to play a card with the same color like the card of his opponent.
     *  (For a 2erSchnopsn game)
     * */
    public void defineValidCards2erSchnopsn(Game game, Player player) {
        //get first played Card
        //Level3: alle Karten; Level2: Trumpfkarten; Level1:gleichfarbige Karten
        Card firstCard = game.getPlayedCards().entrySet().iterator().next().getValue();
        List<Integer> levels = new ArrayList<>();

        for (Card card : game.getPlayerCardMap().get(player)) {
            //erstes Karte false dann überprüfen ob ok
            card.setPriority(false);

            //Wenn nichts ausgespielt
            if (firstCard == null) {
                levels.add(3);
            }
            //Wenn gleiche farbe
            else if (card.getColor() == firstCard.getColor()) {
                levels.add(1);
            }
            //Wenn Trumpf
            else if (card.getColor() != firstCard.getColor() && card.getColor() == game.getCurrentTrump()) {
                levels.add(2);
            }
            //sonst irgendeine
            else {
                levels.add(3);
            }
        }

        Collections.sort(levels);
        int minLevel = levels.get(0);

        switch (minLevel) {
            case 3:
                //Alle Karten
                for (Card card : game.getPlayerCardMap().get(player)) {
                    card.setPriority(true);
                }
                break;
            case 2:
                //Trumpfkarten
                for (Card card : game.getPlayerCardMap().get(player)) {
                    if (card.getColor() != firstCard.getColor() && card.getColor() == game.getCurrentTrump()) {
                        card.setPriority(true);
                    }
                }
                break;
            case 1:
                //Karten mit gleicher Farbe
                for (Card card : game.getPlayerCardMap().get(player)) {
                    if (card.getColor() == firstCard.getColor()) {
                        card.setPriority(true);
                    }
                }
                break;
        }
    }

    /**
     *  Method to check if the player has to play a card with the same color like the card of his opponent.
     *  (For a 4erSchnopsn game)
     * */
    public void defineValidCards4erSchnopsn(Game game, Player player) {
        //Level3: alle Karten; Level2:trumpfkarten; Level1:Gleichfarbige Karten; Level0: Gleichfarbige Karten mit höherem value
        Card firstCard = game.getPlayedCards().entrySet().iterator().next().getValue();
        List<Integer> levels = new ArrayList<>();

        for (Card card : game.getPlayerCardMap().get(player)) {
            //erstes Karte false dann überprüfen ob ok
            card.setPriority(false);

            //Wenn nichts ausgespielt
            if (firstCard == null) {
                levels.add(3);
            } else if (card.getColor() == firstCard.getColor() && card.getValue() > firstCard.getValue()) {
                levels.add(0);
            }
            //Wenn gleiche farbe
            else if (card.getColor() == firstCard.getColor()) {
                levels.add(1);
            }
            //Wenn Trumpf
            else if (card.getColor() != firstCard.getColor() && card.getColor() == game.getCurrentTrump()) {
                levels.add(2);
            }
            //sonst irgendeine
            else {
                levels.add(3);
            }
        }
        Collections.sort(levels);
        int minLevel = levels.get(0);

        switch (minLevel) {
            case 3:
                //Alle Karten
                for (Card card : game.getPlayerCardMap().get(player)) {
                    card.setPriority(true);
                }
                break;
            case 2:
                //Trumpfkarten
                for (Card card : game.getPlayerCardMap().get(player)) {
                    if (card.getColor() != firstCard.getColor() && card.getColor() == game.getCurrentTrump()) {
                        card.setPriority(true);
                    }
                }
                break;
            case 1:
                //Karten mit gleicher Farbe
                for (Card card : game.getPlayerCardMap().get(player)) {
                    if (card.getColor() == firstCard.getColor()) {
                        card.setPriority(true);
                    }
                }
                break;
            case 0:
                //Karten mit gleicher Farbe und höherem value
                for (Card card : game.getPlayerCardMap().get(player)) {
                    if (card.getColor() == firstCard.getColor() && card.getValue() > firstCard.getValue()) {
                        card.setPriority(true);
                    }
                }
                break;
        }

    }

    /**
     *  Sends data to players after one has played out a card.
     * */
    public void sendStingDataToPlayers(Game game, UUID winnerID) {
        List<Card> cards = new ArrayList<>(game.getPlayedCards().values());
        ObjectMapper mapper = new ObjectMapper();

        if (winnerID == null) {
            //Sends a message to all players, about who will play next
            // Spieler der gerade dran war
            Player turnPlayer = null;
            Player nextPlayer = null;
            for (Team team : game.getTeams()) {
                turnPlayer = team.getPlayers().stream().filter(Player::isMyTurn).findFirst().orElse(null);
                if (turnPlayer != null) {
                    break;
                }
            }
            final Player finalTurnPlayer = turnPlayer;

            for (Team team : game.getTeams()) {
                nextPlayer = team.getPlayers().stream().filter(player -> player.getPlayerNumber() == (finalTurnPlayer.getPlayerNumber() % game.getMaxNumberOfPlayers() + 1)).findFirst().orElse(null);
                if (nextPlayer != null) {
                    break;
                }
            }

            final Player finalNextPlayer = nextPlayer;
            switch (game.getCurrentHighestCall()) {
                case BETTLER, ASSENBETTLER, PLAUDERER:
                    if (!nextPlayer.isActive()) {
                        for (Team team : game.getTeams()) {
                            nextPlayer = team.getPlayers().stream().filter(player -> player.getPlayerNumber() == (finalNextPlayer.getPlayerNumber() % game.getMaxNumberOfPlayers() + 1)).findFirst().orElse(null);
                            if (nextPlayer != null) {
                                break;
                            }
                        }
                    }
                    break;
            }
            final Player realNextPlayer = nextPlayer;
            game.getTeams().forEach(team -> team.getPlayers().forEach(player1 -> {
                try {
                    player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("playedCards", cards))));
                    if (player1.getPlayerNumber() == realNextPlayer.getPlayerNumber()) {
                        player1.setMyTurn(true);
                        player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("myTurn", true))));
                        //priorities setzen
                        if (game.isFaerbeln()) {
                            if (game.getGameType() == GameType._2ERSCHNOPSN) {
                                defineValidCards2erSchnopsn(game, player1);
                            } else if (game.getGameType() == GameType._4ERSCHNOPSN) {
                                defineValidCards4erSchnopsn(game, player1);
                            }
                            player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("priorityCards", game.getPlayerCardMap().get(player1)))));
                        }

                    } else {
                        player1.setMyTurn(false);
                        player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("myTurn", false))));
                    }

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
            Player zudreher = getZudreher(game);
            for (Player player1 : game.getPlayedCards().keySet()) {
                try {
                    player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("playedCards", cards))));
                    //schicke an den gewinner seinen Stich und an Verlierer, dass der Gewinner den Stich bekommt
                    if (player1.getPlayerID() == winnerID) {
                        player1.setNumberOfStingsPerRound(player1.getNumberOfStingsPerRound() + 1);
                        //Punkte setzten
                        game.getTeams().get((player1.getPlayerNumber() + 1) % 2).setCurrentScore(game.getTeams().get((player1.getPlayerNumber() + 1) % 2).getCurrentScore() + points);
                        game.getTeams().get((player1.getPlayerNumber() + 1) % 2).setCurrentScore(game.getTeams().get((player1.getPlayerNumber() + 1) % 2).getCurrentScore() + game.getTeams().get((player1.getPlayerNumber() + 1) % 2).getBuffer());
                        game.getTeams().get((player1.getPlayerNumber() + 1) % 2).setBuffer(0);


                        //Stingscore an alle im Team schicken
                        game.getTeams().get((winner.getPlayerNumber() + 1) % 2).getPlayers().stream().forEach(player2 -> {
                            try {
                                player2.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("sting", cards))));
                                player2.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("stingScore", game.getTeams().get((player1.getPlayerNumber() + 1) % 2).getCurrentScore()))));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                        player1.setMyTurn(true);
                        player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("myTurn", player1.isMyTurn()))));


                    } else {
                        player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("winner", winner.getPlayerName()))));
                        player1.setMyTurn(false);
                        player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("myTurn", player1.isMyTurn()))));
                    }

                    for (Card card : game.getPlayerCardMap().get(player1)) {
                        card.setPriority(true);
                    }
                    player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("priorityCards", game.getPlayerCardMap().get(player1)))));


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //karte ziehen und zurückschicken nur bei 2er schnopsn
            if (game.getGameType() == GameType._2ERSCHNOPSN) {
                sendAdditionalData2erSchnopsn(game, mapper, winner);
            }
            //beim 4er Schnopsn überprüfen ob ansage durchgeht und Punkte vergeben
            else if (game.getGameType() == GameType._4ERSCHNOPSN) {
                sendAdditionalData4erSchnopsn(game, mapper, winner);
            }
            game.getPlayedCards().clear();

        }
    }

    /**
     *  This method returns the player who "hat zugedreht" in a 2erSchonpsn game.
     * */
    public Player getZudreher(Game game) {
        for (Team team : game.getTeams()) {
            for (Player player : team.getPlayers()) {
                if (player.isZudreher()) {
                    return player;
                }
            }
        }
        return null;
    }

    /**
     *  Sends data to players after one has played out a card.
     * */
    public void sendAdditionalData4erSchnopsn(Game game, ObjectMapper mapper, Player winner) {
        Call call = game.getCurrentHighestCall();
        Player calledPlayer = null;
        //Lamda um player mit playsCall true zu bekommen
        for (Team team : game.getTeams()) {
            calledPlayer = team.getPlayers().stream().filter(Player::isPlaysCall).findFirst().orElse(null);
            if (calledPlayer != null) {
                break;
            }

        }
        //makeRightMove
        //check if succeeds
        if (checkCall(game, calledPlayer)) {
            if (game.getCurrentHighestCall() == Call.NORMAL && game.getTeams().get((winner.getPlayerNumber() + 1) % 2).getCurrentScore() > 65) {
                awardForPoints4erSchnopsn(winner, game);
                sendScoreDataToPlayers4erSchnopsn(game, mapper, winner);
            } else if ((game.getCurrentHighestCall() == Call.SCHNAPSER || game.getCurrentHighestCall() == Call.KONTRASCHNAPSER) && game.getTeams().get((calledPlayer.getPlayerNumber() + 1) % 2).getCurrentScore() > 65) {
                awardForPoints4erSchnopsn(calledPlayer, game);
                sendScoreDataToPlayers4erSchnopsn(game, mapper, calledPlayer);
            } else if (game.getNumberOfStingsPerRound() == 5) {
                awardForPoints4erSchnopsn(calledPlayer, game);
                sendScoreDataToPlayers4erSchnopsn(game, mapper, calledPlayer);
            }
            //ok
        } else {
            awardForPoints4erSchnopsn(game.getTeams().get(calledPlayer.getPlayerNumber() % 2).getPlayers().get(0), game);
            sendScoreDataToPlayers4erSchnopsn(game, mapper, game.getTeams().get(calledPlayer.getPlayerNumber() % 2).getPlayers().get(0));
        }

    }


    /**
     *  Checks if the current call is still valid.
     * */
    public boolean checkCall(Game game, Player calledPlayer) {
        switch (game.getCurrentHighestCall()) {
            //The player has to win the game with all stings
            case GANG, BAUER, ZEHNERGANG, KONTRABAUER:
                if (game.getNumberOfStingsPerRound() == calledPlayer.getNumberOfStingsPerRound())
                    return true;
                break;

            //The player has to reach 66 or more points with three stings
            case SCHNAPSER, KONTRASCHNAPSER:
                if ((game.getNumberOfStingsPerRound() == calledPlayer.getNumberOfStingsPerRound()) && game.getNumberOfStingsPerRound() <= 3) {
                    return true;
                }
                break;

            //The player wins the round when he gets no sting
            case BETTLER, ASSENBETTLER:
                if (game.getNumberOfStingsPerRound() <= 5 && calledPlayer.getNumberOfStingsPerRound() == 0)
                    return true;
                break;
            case NORMAL:
                return true;
        }
        return false;
    }

    /**
     *  This method sends messages to all players about the current score in a 4erSchnopsn game.
     * */
    public void sendScoreDataToPlayers4erSchnopsn(Game game, ObjectMapper mapper, Player winner) {
        //send gameScore
        int index=0;
        for (Team team:game.getTeams()) {
            Map<String, Integer> gameScore = new LinkedHashMap<>();
            gameScore.put("myScore", game.getTeams().get(index%2).getCurrentGameScore());
            gameScore.put("opponents", game.getTeams().get((index+1) %2).getCurrentGameScore());
            for (Player player1:team.getPlayers()) {
                try {
                    player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("gameScore", gameScore))));
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
            index++;
        }

        //Bummerlzeit
        if (game.getTeams().get((winner.getPlayerNumber() + 1) % 2).getCurrentGameScore() > 23) {
            game.getTeams().stream().forEach(team -> {
                team.setCurrentGameScore(0);
            });
            game.getTeams().get(winner.getPlayerNumber() % 2).setCurrentBummerl(game.getTeams().get(winner.getPlayerNumber() % 2).getCurrentBummerl() + 1);

            int indexB=0;
            for (Team team:game.getTeams()) {
                Map<String, Integer> bummerl = new LinkedHashMap<>();
                bummerl.put("myBummerl", game.getTeams().get(indexB%2).getCurrentBummerl());
                bummerl.put("opponentsBummerl", game.getTeams().get((indexB+1) %2).getCurrentBummerl());
                for (Player player1:team.getPlayers()) {
                    try {
                        player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("bummerl", bummerl))));
                    } catch (IOException e) {
                        //e.printStackTrace();
                    }
                }
                indexB++;
            }

        }
    }

    /**
     *  This method sends messages to all players about the current score in a 2erSchnopsn game.
     * */
    public void sendAdditionalData2erSchnopsn(Game game, ObjectMapper mapper, Player winner) {
        if (game.getAvailableCards().size() == 0) {
            game.getTeams().forEach(team -> team.getPlayers().forEach(player4 -> {
                try {
                    player4.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("emptyCardStack", "cardstack is empty"))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        }
        //Wenn man 66 Punkte hat oder keine Karten mehr zum ziehen hat, oder letzter stich
        if (!checkIfRoundOver(game, mapper, winner)) {
            try {
                if (!game.isFaerbeln()) {
                    //Get new Card; winner gets the new card before the looser
                    Card card = getRandomCard(game.getAvailableCards(), false);
                    winner.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("newCard", card))));
                    game.getPlayerCardMap().get(winner).add(card);
                    card = getRandomCard(game.getAvailableCards(), false);
                    game.getTeams().get((winner.getPlayerNumber()) % 2).getPlayers().get(0).getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("newCard", card))));
                    game.getPlayerCardMap().get(game.getTeams().get((winner.getPlayerNumber()) % 2).getPlayers().get(0)).add(card);
                    if (game.getAvailableCards().size() == 0) {
                        game.setFaerbeln(true);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *  This method checks if one round is over in a 2er Schnopsn game.
     * */
    public boolean checkIfRoundOver(Game game, ObjectMapper mapper, Player winner) {
        if ((game.getTeams().get((winner.getPlayerNumber() + 1) % 2).getCurrentScore() > 65) || (getAllHandCrads(game) == 0)) {
            if (winner.isZudreher() && game.getTeams().get((winner.getPlayerNumber() + 1) % 2).getCurrentScore() < 66) {
                winner = game.getTeams().get(winner.getPlayerNumber() % 2).getPlayers().get(0);
                game.getTeams().get(winner.getPlayerNumber() % 2).setCurrentScore(66);
            }
            sendWinnerName(game, mapper, winner);
            //Punkte vergeben
            endOfRound2erSchnopsn(winner, game, game.getTeams().get((winner.getPlayerNumber()) % 2).getCurrentScore());

            //GameScore zurückschicken
            Map<UUID, Integer> gamescore = new LinkedHashMap<>();
            //alle Gamescore holen
            for (Team team : game.getTeams()) {
                gamescore.put(team.getPlayers().get(0).getPlayerID(), team.getCurrentGameScore());
            }
            //Gamescorestand zurückschicken
            game.getTeams().forEach(team -> team.getPlayers().forEach(player4 -> {
                try {
                    player4.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("gamescore", gamescore))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));


            //überprüfen ob Bummerl gegeben wird
            if (game.getTeams().get((winner.getPlayerNumber() + 1) % 2).getCurrentGameScore() > 6) {
                //Bummerlstand von Verlierer erhöhen
                game.getTeams().get((winner.getPlayerNumber()) % 2).setCurrentBummerl(game.getTeams().get((winner.getPlayerNumber()) % 2).getCurrentBummerl() + 1);
                //alle Bummerl holen
                Map<UUID, Integer> bummerl = new LinkedHashMap<>();
                for (Team team : game.getTeams()) {
                    bummerl.put(team.getPlayers().get(0).getPlayerID(), team.getCurrentBummerl());
                }
                //Bummerlstand zurückschicken
                game.getTeams().forEach(team -> team.getPlayers().forEach(player4 -> {
                    try {
                        player4.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("bummerl", bummerl))));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));
                for (Team team : game.getTeams()) {
                    team.setCurrentGameScore(0);
                }
            }
            return true;
        }
        return false;
    }

    /**
     *  This method returns the current number of cards of all players.
     * */
    public int getAllHandCrads(Game game) {
        int anz = 0;
        for (Player player : game.getPlayerCardMap().keySet()) {
            anz += game.getPlayerCardMap().get(player).size();
        }
        return anz;
    }

    /**
     *  This method sends messages to all players about the name of the winner.
     * */
    public void sendWinnerName(Game game, ObjectMapper mapper, Player player1) {
        for (Player player3 : game.getPlayedCards().keySet()) {
            try {
                player3.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("winnerOfRound", game.getTeams().get(player1.getPlayerNumber() % 2).getPlayers().get(0).getPlayerName()))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A period of time where everyone can make a call.
     * */
    public boolean callPeriod(Game game, ObjectMapper mapper, Player player) {
        if (game.getNumberOfCalledCalls() == 4) {
            //send Data
            game.getTeams().forEach(team -> team.getPlayers().forEach(player1 -> {
                if (player1.isPlaysCall()) {
                    player1.setMyTurn(true);

                    switch (game.getCurrentHighestCall()) {
                        case BETTLER, ASSENBETTLER, PLAUDERER -> game.getTeams().get((player1.getPlayerNumber() + 1) % 2).getPlayers().stream().filter(player2 -> !player2.isPlaysCall()).findFirst().get().setActive(false);
                        case KONTRABAUER, KONTRASCHNAPSER -> {
                            player1.setMyTurn(false);
                            game.getTeams().forEach(team2 -> team2.getPlayers().forEach(player2 -> {
                                if (player2.isCaller()) {
                                    player2.setMyTurn(true);
                                }
                            }));
                        }
                    }

                } else {
                    player1.setMyTurn(false);
                }

            }));
            if (!trumpNeeded(game.getCurrentHighestCall())) {
                game.setCurrentTrump(null);
            }
            //deshalb weil wir den aktuellen hier noch brauchen
            defineCaller(game);
            return false;
        } else {
            Player turnPlayer = player;
            for (Team team : game.getTeams()) {
                turnPlayer = team.getPlayers().stream().filter(Player::isMyTurn).findFirst().orElse(null);
                if (turnPlayer != null) {
                    break;
                }
            }
            final Player realTurnPlayer = turnPlayer;
            game.getTeams().forEach(team -> team.getPlayers().forEach(player1 -> {
                if (player1.getPlayerNumber() == (realTurnPlayer.getPlayerNumber() % 4) + 1) {
                    player1.setMyTurn(true);
                }
            }));
            player.setMyTurn(false);
            return true;
        }
    }

    /**
     * When someone wants to call a 20er or 40er.
     * */
    public int makeCall2erSchnopsn(Game game, Player player) {
        List<Card> handCards = game.getPlayerCardMap().get(player);
        //Becomes true if the first card is found
        boolean foundFirstCard = false;
        //To store the first card if it is found
        Card tempCard = null;

        //alle Colors der Handkarten hinzufügen
        Set<Color> colorSet = new TreeSet<>();
        for (Card card : handCards) {
            card.setPriority(false);
            colorSet.add(card.getColor());
        }
        for (Color color : colorSet) {
            for (Card card : handCards) {
                //adde erste Karte der Farbe
                if ((card.getName().equals("Dame") || card.getName().equals("König")) && card.getColor() == color && !foundFirstCard) {
                    foundFirstCard = true;
                    tempCard = card;
                    //Wenn erste Karte gefunden und diese die gleiche farbe hat
                } else if (foundFirstCard && card.getColor() == color) {
                    switch (tempCard.getName()) {
                        case "Dame":
                            if (card.getName().equals("König")) {
                                tempCard.setPriority(true);
                                card.setPriority(true);
                                if (game.getCurrentTrump() == color) {
                                    return 40;
                                }
                                return 20;
                            }
                            break;
                        case "König":
                            if (card.getName().equals("Dame")) {
                                tempCard.setPriority(true);
                                card.setPriority(true);
                                if (game.getCurrentTrump() == color) {
                                    return 40;
                                }
                                return 20;
                            }
                            break;
                    }
                }
            }
            foundFirstCard = false;
        }

        for (Card card : handCards) {
            card.setPriority(true);
        }
        return 0;
    }
}