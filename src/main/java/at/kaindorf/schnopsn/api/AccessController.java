package at.kaindorf.schnopsn.api;

import at.kaindorf.schnopsn.beans.*;
import at.kaindorf.schnopsn.bl.GameLogic;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


@RestController
@RequestMapping("api/v1")
@CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.OPTIONS, RequestMethod.HEAD, RequestMethod.PUT})
public class AccessController {

    private final GameLogic logic = new GameLogic();
    private GameStorage storage = GameStorage.getInstance();
    private ObjectMapper mapper = new ObjectMapper();

    // TODO: nach jedem Stich buffer überprüfen //finished
    //TODO: check three cases of SCHNAPSER
    //TODO:färeblpflicht + stechpflicht 4er Schnopsn //finished
    //TODO: Frontend besprechen: Farbenringerl, available Calls
    //TODO: handkarten in sendstingData anschauen // finished
    //TODO: priority bei makeMoveByCall zurückgeben (1.Element betracheten) //finished
    //TODO: 20er40er schauen ob fertig (von sendStingData Methode) //finished
    //TODO: JavaDocs machen
    //TODO: aufdehen (4erSchnopsn) - random card //finished
    //TODO: priority bei calls (Schnapser, Gang, ...) //finished

    @PostMapping(path = "/createPlayer")
    public Object createUser(@RequestParam("playerName") String playerName) {

        // invalid playerName
        if (playerName == null || playerName.length() <= 0) {
            return ResponseEntity.status(400).body(new Message("error", "Empty or invalid playerName"));
        }
        Player newPlayer = new Player(UUID.randomUUID(), playerName, false, false, 0, false, false, 0, true,false, null);
        storage.getActivePlayers().add(newPlayer);
        return ResponseEntity.status(200).body(newPlayer);
    }

    @PostMapping(path = "/createGame")
    public Object createGame(@RequestParam("gameType") String gameType, @RequestParam("playerID") String playerID) {

        // if wrong gameType
        if (gameType == null || !(GameType._2ERSCHNOPSN.toString().equals(gameType) || GameType._4ERSCHNOPSN.toString().equals(gameType))) {
            return ResponseEntity.status(400).body(new Message("error", "Empty or invalid gameType: _2ERSCHNOPSN, _4ERSCHNOPSN"));
        }

        // if invalid playerID
        if (playerID == null || playerID.length() != 36) {
            return ResponseEntity.status(400).body(new Message("error", "Empty or invalid playerID: must be type UUID!"));
        }


        try {
            GameType realGameType = GameType.valueOf(gameType);
            Player player = GameLogic.findPlayer(storage.getActivePlayers(), playerID);
            player.setCaller(true);
            player.setMyTurn(true);
            player.setAdmin(true);
            player.setPlayerNumber(1);
            Game newGame = logic.createGame(realGameType, player);
            storage.getActiveGames().add(newGame);

            return ResponseEntity.status(200).body(newGame);
        } catch (NullPointerException e) {
            return ResponseEntity.status(400).body(new Message("error", "Player does not exist!")); // no player found
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new Message("error", "Invalid playerID: must be type UUID!")); // wrong format of playerID (UUID)
        }
    }

    @PostMapping(path = "/joinGame")
    public Object joinGame(@RequestParam("gameID") String gameID, @RequestParam("playerID") String playerID) {

        // if invalid playerID
        if (playerID == null || playerID.length() != 36) {
            return ResponseEntity.status(400).body(new Message("error", "Empty or invalid playerID: must be type UUID!"));
        }

        // if invalid gameID
        if (gameID == null || gameID.length() != 36) {
            return ResponseEntity.status(400).body(new Message("error", "Empty or invalid gameID: must be type UUID!"));
        }

        try {
            Player player = GameLogic.findPlayer(storage.getActivePlayers(), playerID);
            Game game = GameLogic.findGame(storage.getActiveGames(), gameID);
            int numberOfPlayers = GameLogic.getCurrentNumberOfPlayers(game);
            if (numberOfPlayers < game.getMaxNumberOfPlayers()) {
                player.setPlayerNumber(numberOfPlayers + 1);
                if ((numberOfPlayers + 1) % 2 == 0) {
                    game.getTeams().get(1).getPlayers().add(player);
                } else {
                    game.getTeams().get(0).getPlayers().add(player);
                }
            }

            //System.out.println(game);
            game.getTeams().forEach(team -> team.getPlayers().forEach(player1 -> {
                try {
                    // TODO - BUG: .IllegalStateException: The WebSocket session [f] has been closed and no method (apart from close()) may be called on a closed session
                    // if: more players join than allowed in a room
                    player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("join", logic.getAllCurrentPlayerNames(game)))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
            return ResponseEntity.status(200).body(game);

        } catch (NullPointerException e) {
            return ResponseEntity.status(400).body(new Message("error", "Player or Game does not exist!")); // no player|game found
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new Message("error", "ID: must be type UUID!")); // wrong format of playerID (UUID)
        }
    }

    @PostMapping(path = "/startRound2erSchnopsn")
    public Object startRound2erSchnopsn(@RequestParam("gameID") String gameID) {
        if (gameID == null || gameID.length() != 36) {
            return ResponseEntity.status(400).body(new Message("error", "Empty or invalid gameID: must be type UUID!"));
        }
        UUID realGameID;

        try {
            realGameID = UUID.fromString(gameID);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new Message("error", "Wrong color or wrong format of gameID")); // wrong format of gameID or wrong color
        }

        //Karten Methode 5 zurück
        Game game = GameLogic.findGame(storage.getActiveGames(), gameID);

        Map<Player, List<Card>> playerCardMap;
        playerCardMap = logic.giveOutCards(game, 5);
        game.setPlayerCardMap(playerCardMap);

        Card trumpCard = logic.getTrumpCard(game);
        game.setCurrentTrump(trumpCard.getColor());
        game.setFaerbeln(false);

        //Stichpunkte zurücksetzen
        for (Team team : game.getTeams()) {
            team.setCurrentScore(0);
        }
        game.getPlayedCards().clear();

        Player caller=logic.getActualCaller(game);
        for (Player player : playerCardMap.keySet()) {
            player.setZudreher(false);

            if(player.getPlayerID()== caller.getPlayerID()){
                player.setMyTurn(true);
            }
            else{
                player.setMyTurn(false);
            }
            try {
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("forward", "./play"))));
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("cards", playerCardMap.get(player)))));
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("trumpCard", trumpCard))));
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("myTurn", player.isMyTurn()))));
            } catch (IOException e) {
                e.printStackTrace();
            }
//            if (player.isMyTurn()) {
//                player.setMyTurn(false);
//            } else {
//                player.setMyTurn(true);
//            }
        }
        logic.defineCaller(game);
        System.out.println("nach defineCaller");
        for (Player player : playerCardMap.keySet()) {
            System.out.println(player.isCaller());
        }


        return ResponseEntity.status(200).body("Hurray!");
    }

    @PostMapping(path = "/zudrehen")
    public Object zudrehen(@RequestParam("gameID") String gameID, @RequestParam("playerID") String playerID) {
        // if invalid playerID
        if (playerID == null || playerID.length() != 36) {
            return ResponseEntity.status(400).body("Empty or invalid playerID: must be type UUID!");
        }

        // if invalid gameID
        if (gameID == null || gameID.length() != 36) {
            return ResponseEntity.status(400).body("Empty or invalid gameID: must be type UUID!");
        }
        Game game = GameLogic.findGame(storage.getActiveGames(), gameID);
        Player player = GameLogic.findPlayer(storage.getActivePlayers(), playerID);
        game.setFaerbeln(true);
        player.setZudreher(true);
        game.getTeams().forEach(team -> team.getPlayers().forEach(player1 -> {
            try {
                player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("zugedreht", player.getPlayerName()))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        return ResponseEntity.status(200).body(new Message("zugedreht", "zudrehen successful"));
    }


    @PostMapping(path = "/switchTrumpCard")
    public Object switchTrumpCard(@RequestParam("gameID") String gameID, @RequestParam("playerID") String playerID) {
        // if invalid playerID
        if (playerID == null || playerID.length() != 36) {
            return ResponseEntity.status(400).body("Empty or invalid playerID: must be type UUID!");
        }

        // if invalid gameID
        if (gameID == null || gameID.length() != 36) {
            return ResponseEntity.status(400).body("Empty or invalid gameID: must be type UUID!");
        }
        Game game = GameLogic.findGame(storage.getActiveGames(), gameID);
        Player player = GameLogic.findPlayer(storage.getActivePlayers(), playerID);
        if (logic.switchTrumpCard(game, player)) {
            game.getTeams().forEach(team -> team.getPlayers().forEach(player1 -> {
                try {
                    player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("switchedTrumpCard", player.getPlayerName()))));
                    player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("newTrumpCard", game.getAvailableCards().get(game.getAvailableCards().size() - 1)))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
            try {
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("cards", game.getPlayerCardMap().get(player)))));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ResponseEntity.status(200).body(new Message("switchedTrumpCard", "switched successful"));

        }

        return ResponseEntity.status(200).body(new Message("error", "only with trumpf bur"));
    }

    @PostMapping(path = "/call20er40er")
    public Object call20er40er(@RequestParam("gameID") String gameID, @RequestParam("playerID") String playerID,@RequestParam("type") String typeID) {
        // if invalid playerID
        if (playerID == null || playerID.length() != 36) {
            return ResponseEntity.status(400).body("Empty or invalid playerID: must be type UUID!");
        }

        // if invalid gameID
        if (gameID == null || gameID.length() != 36) {
            return ResponseEntity.status(400).body("Empty or invalid gameID: must be type UUID!");
        }
        Game game = GameLogic.findGame(storage.getActiveGames(), gameID);
        Player player = GameLogic.findPlayer(storage.getActivePlayers(), playerID);
        int type = logic.makeCall2erSchnopsn(game, player);

        if(type!=0){
            //Buffer oder gleich zu Stichpunkten
            if(game.getTeams().get((player.getPlayerNumber()+1)%2).getCurrentScore()==0) {
                game.getTeams().get((player.getPlayerNumber()+1)%2).setBuffer(game.getTeams().get((player.getPlayerNumber()+1)%2).getBuffer()+type);
            }
            else{
                game.getTeams().get((player.getPlayerNumber()+1)%2).setCurrentScore(game.getTeams().get((player.getPlayerNumber()+1)%2).getCurrentScore()+type);
            }
            //Sag allen der hat was angesagt
            Color color = game.getPlayerCardMap().get(player).stream().filter(card ->  card.isPriority()).findFirst().get().getColor();
            game.getTeams().forEach(team -> team.getPlayers().forEach(player1 -> {
                System.out.println(player1.isCaller());
                try {
                    player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message(type+"er mit "+color, player.getPlayerName()))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
            //schick ihm seine neuen stichpunkte und priorityCards
            try {
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("stingScore", game.getTeams().get((player.getPlayerNumber()+1)%2).getCurrentScore()))));
                //automatisch nachrichten zum beenden der Runde schicken und priorities zurückstellen
                if(logic.checkIfRoundOver(game,mapper,player)){
                    game.getPlayerCardMap().get(player).stream().forEach(card -> {
                        card.setPriority(true);
                    });
                }
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("priorityCards", game.getPlayerCardMap().get(player)))));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //schau ob mit ansage die Runde vorbei ist

            return ResponseEntity.status(200).body(new Message("20er", "call 20er successful"));
        }


        return ResponseEntity.status(400).body(new Message("error", "kein König und Dame von gleicher Farbe vorhanden"));
    }


    //Stichfunktion
    @PostMapping(path = "/makeMoveByCall")
    public Object makeMoveByCall(@RequestParam("gameID") String gameID, @RequestParam("playerID") String playerID, @RequestParam("color") String color, @RequestParam("value") int cardValue) {

        // if invalid playerID
        if (playerID == null || playerID.length() != 36) {
            return ResponseEntity.status(400).body(new Message("error", "Empty or invalid playerID: must be type UUID!"));
        }

        // if invalid gameID
        if (gameID == null || gameID.length() != 36) {
            return ResponseEntity.status(400).body(new Message("error", "Empty or invalid gameID: must be type UUID!"));
        }

        if (color == null) {
            return ResponseEntity.status(400).body(new Message("error", "No color given!"));
        }

        try {
            Game game = GameLogic.findGame(storage.getActiveGames(), gameID);
            Player player = GameLogic.findPlayer(storage.getActivePlayers(), playerID);
            Card card = logic.getCard(color, cardValue);
            UUID winnerID = logic.makeRightMove(game, card, player);
            logic.sendStingDataToPlayers(game, winnerID);

            if (winnerID == null) {
                return ResponseEntity.status(200).body(new Message("winner", "waiting for the other(s) to play.."));
            } else {
                return ResponseEntity.status(200).body(new Message("winner", GameLogic.findPlayer(storage.getActivePlayers(), winnerID + "")));
            }


        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(new Message("error", "Invalid format of ID or invalid color: " + e.getMessage()));
        }
    }

    @PostMapping(path = "/endOfGame")
    public Object endOfGame(@RequestParam("gameID") String gameID, @RequestParam("playerID") String playerID) {
        Game game = GameLogic.findGame(storage.getActiveGames(), gameID);
        game.getTeams().forEach(team -> team.getPlayers().forEach(player -> {
            try {
                player.setActive(true);
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("message", "Game is over!"))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        storage.getActiveGames().remove(game);

        return ResponseEntity.status(200).body("Game over!");
    }

    @PostMapping(path = "/getCards4erSchnopsn")
    public Object getCards4erSchnopsn(@RequestParam("gameID") String gameID) {
        //Serverintern: jeder bekommt seine 5 Karten -> geschickt werden nur die ersten 3; dann Trumpf die letzten 2
        Game game = GameLogic.findGame(storage.getActiveGames(), gameID);
        Map<Player, List<Card>> playerCardMap = logic.giveOutCards(game, 3);
        game.setPlayerCardMap(playerCardMap);
        for (Player player : game.getPlayerCardMap().keySet()) {
            player.setActive(true);
            player.setNumberOfStingsPerRound(0);
            if (player.isCaller()) {
                player.setMyTurn(true);
            }
            try {
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("forward", "./play"))));
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("cards", playerCardMap.get(player)))));
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("caller", player.isCaller()))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        game.setCurrentHighestCall(Call.NORMAL);
        game.setCurrentTrump(null);
        game.getPlayedCards().clear();
        game.setNumberOfCalledCalls(0);
        game.setNumberOfStingsPerRound(0);

        return ResponseEntity.status(200).body("got cards successfully");
    }

    @PostMapping(path = "/callTrump")
    public Object startRound(@RequestParam("gameID") String gameID,@RequestParam String playerID, @RequestParam("color") String color) {
        // if invalid playerID
        if (playerID == null || playerID.length() != 36) {
            return ResponseEntity.status(400).body(new Message("error", "Empty or invalid playerID: must be type UUID!"));
        }
        // if invalid gameID
        if (gameID == null || gameID.length() != 36) {
            return ResponseEntity.status(400).body("Empty or invalid gameID: must be type UUID!");
        }

        if (color == null) {
            return ResponseEntity.status(400).body("No color given!");
        }

        Color realColor;

        try {
            realColor = Color.valueOf(color.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("Wrong color or wrong format of gameID"); // wrong format of gameID or wrong color
        }

        Game game = GameLogic.findGame(storage.getActiveGames(), gameID);
        Player playerTrump = GameLogic.findPlayer(storage.getActivePlayers(),playerID);
        Map<Player, List<Card>> playerCardMap = logic.giveOutCards(game, 2);
        Card trumpColorCard = null;
        if(realColor==Color.RANDOM){
            trumpColorCard =playerCardMap.get(playerTrump).get(0);
            game.setCurrentTrump(trumpColorCard.getColor());
        }
        else{
            try {
                trumpColorCard = new Card("temp",1,new URL("http://link1"),realColor,true);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            game.setCurrentTrump(realColor);
        }
        for (Player player : playerCardMap.keySet()) {
            game.getPlayerCardMap().get(player).addAll(playerCardMap.get(player));
            try {
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("cards", game.getPlayerCardMap().get(player))))); // return all 5 cards
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("trump", trumpColorCard))));
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("myTurn", player.isMyTurn()))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return ResponseEntity.status(200).body(new Message("info", "started round successfully"));
    }

    @PostMapping(path = "/makeCall")
    public Object makeCall(@RequestParam("gameID") String gameID, @RequestParam("playerID") String playerID, @RequestParam("call") String call) {

        // if invalid playerID
        if (playerID == null || playerID.length() != 36) {
            return ResponseEntity.status(400).body("Empty or invalid playerID: must be type UUID!");
        }

        // if invalid gameID
        if (gameID == null || gameID.length() != 36) {
            return ResponseEntity.status(400).body("Empty or invalid gameID: must be type UUID!");
        }

        if (call == null) {
            return ResponseEntity.status(400).body("No call given!");
        }

        Call validCall;
        try {
            validCall = Call.valueOf(call.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("Invalid call!");
        }

        Player player = GameLogic.findPlayer(storage.getActivePlayers(), playerID);
        if (player == null) {
            return ResponseEntity.status(404).body("No player found");
        }

        Game game = GameLogic.findGame(storage.getActiveGames(), gameID);
        game.setNumberOfCalledCalls(game.getNumberOfCalledCalls() + 1);
        logic.isCallHigher(game, validCall, player);
        boolean callPeriod= logic.callPeriod(game, mapper, player);

        game.getTeams().forEach(team -> team.getPlayers().forEach(player1 -> {
            try {
                player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("highestCall", game.getCurrentHighestCall()))));
                player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("myTurn", player1.isMyTurn()))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        //Benachrichtigen dass keine Calls mehr gemacht werden
        if(!callPeriod){
            game.getTeams().forEach(team -> team.getPlayers().forEach(player1 -> {
            try {
                player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("message", "finished with Calls!"))));
            } catch (IOException e) {
                e.printStackTrace();
            }
            }));
        }

        return ResponseEntity.status(200).body(new Message("info", "status: 200"));
    }

    @GetMapping(path = "/getAvailableCalls")
    public Object getAvailableCalls(){
        return ResponseEntity.status(200).body(new Message("availableCalls", Call.values()));
    }

}
