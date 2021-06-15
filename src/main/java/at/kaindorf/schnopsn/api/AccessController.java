package at.kaindorf.schnopsn.api;

import at.kaindorf.schnopsn.beans.*;
import at.kaindorf.schnopsn.bl.GameLogic;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.*;


@RestController
@RequestMapping("api/v1")
@CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.OPTIONS, RequestMethod.HEAD, RequestMethod.PUT})
public class AccessController {

    private final GameLogic logic = new GameLogic();
    private GameStorage storage = GameStorage.getInstance();
    private ObjectMapper mapper = new ObjectMapper();

    // TODO: nach jedem Stich buffer überprüfen
    //TODO setAktive: if Call e.g. BETTLER then only three players //finished
    //TODO: KONTRA myTurn first on caller true //finished
    //TODO: check three cases of SCHNAPSER
    //TODO: zudrehen 2er Schnopsn //finished
    //TODO: färbelpflicht 2erSchnopsn // finsihed
    //TODO:färeblpflicht + stechpflicht 4er Schnopsn
    //TODO: Frontend besprechen: Farbenringerl, available Calls
    //TODO: handkarten in sendstingData anschauen
    //TODO: austauschen 2erSchnopsn //finished
    //TODO: priority bei makeMoveByCall zurückgeben (1.Element betracheten)

    @PostMapping(path = "/createPlayer")
    public Object createUser(@RequestParam("playerName") String playerName) {

        // invalid playerName
        if (playerName == null || playerName.length() <= 0) {
            return ResponseEntity.status(400).body(new Message("error", "Empty or invalid playerName"));
        }
        Player newPlayer = new Player(UUID.randomUUID(), playerName, false, false, 0, false, false, 0, true, null);
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

            // Ab hier nur getestet
           /* Player newPlayer = new Player(UUID.randomUUID(), "Test", false, false, 0, false, false,0, null);
            storage.getActivePlayers().add(newPlayer);
            newGame.getTeams().get(1).getPlayers().add(newPlayer);

            try {
                String json = mapper.writeValueAsString(logic.giveOutCards(newGame,5).get(player));
                System.out.println((mapper.writeValueAsString(new Message("cards", logic.giveOutCards(newGame,5).get(player)))));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }*/
            //Ende Testung

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


        for (Player player : playerCardMap.keySet()) {
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
        //neuen playsCall setzten

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
            return ResponseEntity.status(200).body(new Message("switchedTrumpCard", "switched successful"));

        }

        return ResponseEntity.status(200).body(new Message("error", "only with trumpf bur"));
    }

    @PostMapping(path = "/call20er40er")
    public Object call20er40er(@RequestParam("gameID") String gameID, @RequestParam("playerID") String playerID) {
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
        String type = logic.makeCall2erSchnopsn(game, player);


        if (type.equalsIgnoreCase("20er")) {
            //check ob buffer
            if(game.getTeams().get((player.getPlayerNumber()+1)%2).getCurrentScore()==0){
                game.getTeams().get((player.getPlayerNumber()+1)%2).setBuffer(game.getTeams().get((player.getPlayerNumber()+1)%2).getBuffer()+20);
            }
            else{
                game.getTeams().get((player.getPlayerNumber()+1)%2).setCurrentScore(game.getTeams().get((player.getPlayerNumber()+1)%2).getCurrentScore()+20);
                game.getTeams().get((player.getPlayerNumber()+1)%2).getPlayers().forEach(player1 -> {
                    try {
                        player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("20erPoints", 20))));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

            game.getTeams().forEach(team -> team.getPlayers().forEach(player1 -> {
                try {
                    player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("20er", player.getPlayerName()))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
            try {
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("priorityCards", game.getPlayerCardMap().get(player)))));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ResponseEntity.status(200).body(new Message("20er", "call 20er successful"));

        } else if (type.equalsIgnoreCase("40er")) {
            if(game.getTeams().get(player.getPlayerNumber()+1%2).getCurrentScore()==0){
                game.getTeams().get(player.getPlayerNumber()+1%2).setBuffer(game.getTeams().get(player.getPlayerNumber()+1%2).getBuffer()+40);
            }
            else{
                game.getTeams().get(player.getPlayerNumber()+1%2).setCurrentScore(game.getTeams().get(player.getPlayerNumber()+1%2).getCurrentScore()+40);
                game.getTeams().get(player.getPlayerNumber()+1%2).getPlayers().forEach(player1 -> {
                    try {
                        player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("20erPoints", 20))));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            game.getTeams().forEach(team -> team.getPlayers().forEach(player1 -> {
                try {
                    player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("40er", player.getPlayerName()))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
            try {
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("priorityCards", game.getPlayerCardMap().get(player)))));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ResponseEntity.status(200).body(new Message("40er", "call 40er successful"));
        }

        return ResponseEntity.status(200).body(new Message("error", "only with trumpf bur"));
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
    public Object startRound(@RequestParam("gameID") String gameID, @RequestParam("color") String color) {

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
        game.setCurrentTrump(realColor);
        Map<Player, List<Card>> playerCardMap = logic.giveOutCards(game, 2);
        for (Player player : playerCardMap.keySet()) {
            game.getPlayerCardMap().get(player).addAll(playerCardMap.get(player));
            try {
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("cards", playerCardMap.get(player)))));
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("trump", realColor))));
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("myTurn", player.isMyTurn()))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return ResponseEntity.status(200).body("started round successfully");
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
        logic.callPeriod(game, mapper, player);

        game.getTeams().forEach(team -> team.getPlayers().forEach(player1 -> {
            try {
                player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("highestCall", game.getCurrentHighestCall()))));
                player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("myTurn", player1.isMyTurn()))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        return ResponseEntity.status(200).body(new Message("info", "status: 200"));
    }

}
