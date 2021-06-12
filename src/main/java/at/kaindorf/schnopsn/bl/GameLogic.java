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
    //TODO 2erSchnopsn zuadrahen

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
            game = new Game(UUID.randomUUID(), gameType, null, null, 2, teams, Call.NORMAL, new LinkedHashMap<>(), allCards, 0, 0, new LinkedHashMap<>());
        } else if (gameType == GameType._4ERSCHNOPSN) {
            game = new Game(UUID.randomUUID(), gameType, null, null, 4, teams, Call.NORMAL, new LinkedHashMap<>(), allCards, 0, 0, new LinkedHashMap<>());
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
        return allCards.stream().filter(card -> card.getColor().equals(realColor) && card.getValue() == value).findFirst().orElse(null);
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
                    game.getPlayerCardMap().get(player).remove(card);
                }
                if (game.getPlayedCards().size() == game.getMaxNumberOfPlayers() - 1) {
                    if (trumpNeeded(game.getCurrentHighestCall())) {
                        return getPlayerWithHighestCard(game.getPlayedCards(), game.getCurrentTrump(),game);
                    } else {
                        return getPlayerWithHighestCard(game.getPlayedCards(), null,game);
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
                        return getPlayerWithHighestCard(game.getPlayedCards(), game.getCurrentTrump(),game);
                    } else {
                        return getPlayerWithHighestCard(game.getPlayedCards(), null,game);
                    }
                }
                break;
        }
        return null;
    }

    //definiertwelcher Spieler den Stich bekommt (welche Karte die Höchste ist)
    public UUID getPlayerWithHighestCard(Map<Player, Card> playMap, Color trump,Game game) {
        List<Card> playCards = new ArrayList<>();

        //System.out.println(playMap);

        for (Player player : playMap.keySet()) {
           // System.out.println(player);
            //System.out.println(playMap.get(player));
            playCards.add(playMap.get(player));
        }


        //System.out.println(playCards);

        //If Zehnergang then Ass has value 1
        if(game.getCurrentHighestCall()==Call.ZEHNERGANG){
            playCards.forEach(card -> {
                if(card.getValue()==11){
                    card.setValue(1);
                }
            });
        }

        Color firstColor = playCards.get(0).getColor();

        //System.out.println(playCards);
        int count = 0;
        while (playCards.size() > 1) {
            Card temp = playCards.get(count); // TODO - Bug: IndexOutOfBoundsException  Index 2 out of bounds for length 2 (wenn 2 gleiche Karten unterschiedlicher Farbe ausgespielt werden, zb: Pick Zehner + Herz Zehner)
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
                else if(game.getGameType()==GameType._2ERSCHNOPSN && temp.getColor() != firstColor){
                    playCards.remove(temp);
                    count--;
                    break;
                }
            }
            count++;
        }
        //System.out.println(playCards);
        //System.out.println("in for");

        for (Player player : playMap.keySet()) {
            //System.out.println(playMap.get(player));
            if (playMap.get(player) == playCards.get(0)) {
                //System.out.println(player.getPlayerID());
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
    public boolean awardForPoints4erSchnopsn(Player winner, Game game) {
        Call call = game.getCurrentHighestCall();
        int winnerTeam = 0;
        int looserScore = 0;
        if (winner.getPlayerNumber() % 2 != 0) {
            winnerTeam = 1;
        }
        if (game.getCurrentHighestCall() == Call.NORMAL) {
            looserScore = game.getTeams().get((winner.getPlayerNumber()+1) % 2).getCurrentScore();
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

    //Set points for the players and returns true if the round is over
    public boolean endOfRound2erSchnopsn(Player winner, Game game, int looserPoints) {
        int winnerTeam = 0;
        int currentGameScore;
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
        game.getTeams().forEach(team -> team.getPlayers().stream().filter(Player::isCaller).findFirst().get().setCaller(false));
        game.getTeams().forEach(team -> team.getPlayers().stream().filter(player -> player.getPlayerNumber() == finalOldCallerNumber % 4 + 1).findFirst().get().setCaller(true));
    }

    //give Cards for each player
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
    //handkarten anschuen
    //sendData toPlayers after one has played out a card
    public void sendStingDataToPlayers(Game game, UUID winnerID) {
        List<Card> cards = new ArrayList<>(game.getPlayedCards().values());
        ObjectMapper mapper = new ObjectMapper();

        if (winnerID == null) {
            //System.out.println("kein gewinner");
            //Sends a message to all players, about who will play next
            // Spieler der gerade dran war
            Player turnPlayer = null;
            Player nextPlayer=null;
            for(Team team: game.getTeams()) {
                turnPlayer = team.getPlayers().stream().filter(Player::isMyTurn).findFirst().orElse(null);
                if(turnPlayer!=null){
                    break;
                }
            }
            final Player finalTurnPlayer = turnPlayer;

            for (Team team:game.getTeams()) {
                nextPlayer = team.getPlayers().stream().filter(player -> player.getPlayerNumber() == (finalTurnPlayer.getPlayerNumber()%game.getMaxNumberOfPlayers()+1)).findFirst().orElse(null);
                if(nextPlayer!=null){
                    break;
                }
            }

            final Player finalNextPlayer = nextPlayer;
            switch(game.getCurrentHighestCall()){
                case BETTLER,ASSENBETTLER,PLAUDERER:
                    if(!nextPlayer.isActive()){
                        for (Team team:game.getTeams()) {
                            nextPlayer = team.getPlayers().stream().filter(player -> player.getPlayerNumber() == (finalNextPlayer.getPlayerNumber()%game.getMaxNumberOfPlayers()+1)).findFirst().orElse(null);
                            if(nextPlayer!=null){
                                break;
                            }
                        }
                    }
                    break;
            }
            final Player realNextPlayer = nextPlayer;
            game.getTeams().forEach(team -> team.getPlayers().forEach(player1 -> {
                try {
                    if (player1.getPlayerNumber() == realNextPlayer.getPlayerNumber()) {
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
            //System.out.println(winner);


            int points = 0;
            for (Card card1 : cards) {
                points += card1.getValue();
            }
            for (Player player1 : game.getPlayedCards().keySet()) {
                try {
                    player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("playedCards",cards))));
                    //schicke an den gewinner seinen Stich und an Verlierer, dass der Gewinner den Stich bekommt
                    if (player1.getPlayerID() == winnerID) {

                        player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("sting", cards))));
                        player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("stingPoints", points))));
                        player1.setMyTurn(true);
                        player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("myTurn", player1.isMyTurn()))));
                        //Punkte setzten
                        game.getTeams().get(player1.getPlayerNumber() % 2).setCurrentScore(game.getTeams().get(player1.getPlayerNumber() % 2).getCurrentScore() + points);

                    } else {
                        player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("winner", winner.getPlayerName()))));
                        player1.setMyTurn(false);
                        player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("myTurn", player1.isMyTurn()))));
                    }



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

    public void sendAdditionalData4erSchnopsn(Game game, ObjectMapper mapper, Player winner) {
        Call call = game.getCurrentHighestCall();
        Player calledPlayer = null;
        //Lamda um player mit playsCall true zu bekommen
        for (Team team : game.getTeams()) {
            calledPlayer = team.getPlayers().stream().filter(Player::isPlaysCall).findFirst().get(); // TODO
        }
        //makeRightMove
        //check if succeeds
        if (checkCall(game, calledPlayer)) {
            if (game.getNumberOfStingsPerRound() == 5) {
                awardForPoints4erSchnopsn(calledPlayer, game);
            }
            //ok
        } else {
            awardForPoints4erSchnopsn(game.getTeams().get((calledPlayer.getPlayerNumber()+1) % 2).getPlayers().get(0), game);
        }

    }

    //checks if the current call is still valid
    public boolean checkCall(Game game, Player calledPlayer) {
        switch (game.getCurrentHighestCall()) {
            //The player has to win the game with all stings
            case GANG, BAUER, ZEHNERGANG, KONTRABAUER:
                if (game.getNumberOfStingsPerRound() == calledPlayer.getNumberOfStingsPerRound())
                    return true;
                break;

            //The player has to reach 66 or more points with three stings
            case SCHNAPSER, KONTRASCHNAPSER:
                if ((game.getNumberOfStingsPerRound() == calledPlayer.getNumberOfStingsPerRound()) && game.getNumberOfStingsPerRound() < 3) {
                    return true;
                }
                break;

            //The player wins the round when he gets no sting
            case BETTLER, ASSENBETTLER:
                if (game.getNumberOfStingsPerRound() <= 5 && calledPlayer.getNumberOfStingsPerRound() == 0)
                    return true;
                break;
        }
        return false;
    }

    public void sendAdditionalData2erSchnopsn(Game game, ObjectMapper mapper, Player winner) {
        //Wenn man 66 Punkte hat oder keine Karten mehr zum ziehen hat
        if (game.getTeams().get(winner.getPlayerNumber() % 2).getCurrentScore() > 65 || game.getAvailableCards().size() == 0) {
            sendWinnerName(game, mapper, winner);
            //Punkte vergeben und überprüfen ob Bummerl gegeben wird
            if (endOfRound2erSchnopsn(winner, game, game.getTeams().get((winner.getPlayerNumber()+1) % 2).getCurrentScore())) {
                //Bummerlstand von Verlierer erhöhen
                game.getTeams().get((winner.getPlayerNumber()+1) % 2).setCurrentBummerl(game.getTeams().get((winner.getPlayerNumber()+1) % 2).getCurrentBummerl() + 1);
                //alle Bummerl holen
                Map<String, Integer> bummerl = new LinkedHashMap<>();
                for (Team team : game.getTeams()) {
                    bummerl.put(team.getPlayers().get(0).getPlayerName(), team.getCurrentBummerl());
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
                        player4.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("gamescore", gamescore))));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));
            }
        } else {
            try {
                //Get new Card; winner gets the new card before the looser
                Card card = getRandomCard(game.getAvailableCards(),false);
                winner.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("newCard", card))));
                game.getPlayerCardMap().get(winner).add(card);
                card = getRandomCard(game.getAvailableCards(),false);
                game.getTeams().get((winner.getPlayerNumber()+1) % 2).getPlayers().get(0).getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("newCard", card))));
                game.getPlayerCardMap().get(game.getTeams().get((winner.getPlayerNumber()+1) % 2).getPlayers().get(0)).add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendWinnerName(Game game, ObjectMapper mapper, Player player1) {
        for (Player player3 : game.getPlayedCards().keySet()) {
            try {
                player3.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("winnerOfRound", game.getTeams().get(player1.getPlayerNumber() % 2).getPlayers().get(0).getPlayerName()))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void callPeriod(Game game, ObjectMapper mapper, Player player){
        if (game.getNumberOfCalledCalls() == 4) {
            //send Data
            game.getTeams().forEach(team -> team.getPlayers().forEach(player1 -> {
                if (player1.isPlaysCall()) {
                    player1.setMyTurn(true);

                    switch (game.getCurrentHighestCall()) {
                        case BETTLER, ASSENBETTLER, PLAUDERER -> game.getTeams().get(player1.getPlayerNumber() % 2).getPlayers().stream().filter(player2 -> !player2.isPlaysCall()).findFirst().get().setActive(false);
                        case KONTRABAUER, KONTRASCHNAPSER -> {
                            player1.setMyTurn(false);
                            game.getTeams().forEach(team2 -> team.getPlayers().forEach(player2 -> {
                                if (player2.isCaller()) {
                                    player2.setMyTurn(true);
                                }
                            }));
                        }
                    }

                } else {
                    player1.setMyTurn(false);
                }
                try {
                    player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("message", "finished with Calls!"))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
            //deshalb weil wir den aktuellen hier noch brauchen
            defineCaller(game);
        } else {
            game.getTeams().forEach(team -> team.getPlayers().forEach(player1 -> {
                if (player1.isMyTurn()) {
                    game.getTeams().forEach(team1 -> team.getPlayers().forEach(player2 -> {
                        if (player2.getPlayerNumber() == player1.getPlayerNumber() % 4 + 1) {
                            player2.setMyTurn(true);
                        }
                    }));
                }
            }));
            player.setMyTurn(false);
        }
    }
}