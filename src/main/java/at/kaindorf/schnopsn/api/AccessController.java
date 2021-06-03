package at.kaindorf.schnopsn.api;

import at.kaindorf.schnopsn.beans.*;
import at.kaindorf.schnopsn.bl.GameLogic;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.json.*;

import javax.persistence.criteria.CriteriaBuilder;

@RestController
@RequestMapping("api/v1")
@CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.OPTIONS, RequestMethod.HEAD, RequestMethod.PUT})
public class AccessController {

    private final GameLogic logic = new GameLogic();
    private GameStorage storage = GameStorage.getInstance();
    private ObjectMapper mapper = new ObjectMapper();

    @PostMapping(path = "/createPlayer")
    public Object createUser(@RequestParam("playerName") String playerName) {

        // invalid playerName
        if (playerName == null || playerName.length() <= 0) {
            return ResponseEntity.status(400).body("Empty or invalid playerName");
        }
        Player newPlayer = new Player(UUID.randomUUID(), playerName, false, false, 0, false, false,null);
        storage.getActivePlayers().add(newPlayer);
        return ResponseEntity.status(200).body(newPlayer);
    }

    @PostMapping(path = "/createGame")
    public Object createGame(@RequestParam("gameType") String gameType, @RequestParam("playerID") String playerID) {

        // if wrong gameType
        if (gameType == null || !(GameType._2ERSCHNOPSN.toString().equals(gameType) || GameType._4ERSCHNOPSN.toString().equals(gameType))) {
            return ResponseEntity.status(400).body("Empty or invalid gameType: _2ERSCHNOPSN, _4ERSCHNOPSN");
        }

        // if invalid playerID
        if (playerID == null || playerID.length() != 36) {
            return ResponseEntity.status(400).body("Empty or invalid playerID: must be type UUID!");
        }


        try {
            GameType realGameType = GameType.valueOf(gameType);
            Player player = GameLogic.findPlayer(storage.getActivePlayers(), playerID);
            player.setCaller(true);
            player.setMyTurn(true);
            player.setAdmin(true);
            Game newGame = logic.createGame(realGameType, player);
            storage.getActiveGames().add(newGame);

            //Ab hier nur getestet
           /* Player newPlayer = new Player(UUID.randomUUID(), "Test", false, false, 0, false, null);
            storage.getActivePlayers().add(newPlayer);
            newGame.getTeams().get(1).getPlayers().add(newPlayer);

            try {
                String json = mapper.writeValueAsString(logic.giveOutCards(newGame).get(player));
                System.out.println(json);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            try {
                JSONObject json = new JSONObject(logic.giveOutCards(newGame).toString());
                System.out.println(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }*/
            //Ende Testung

            return ResponseEntity.status(200).body(newGame);
        } catch (NullPointerException e) {
            return ResponseEntity.status(400).body("Player does not exist!"); // no player found
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("Invalid playerID: must be type UUID!"); // wrong format of playerID (UUID)
        }
    }

    @PostMapping(path = "/joinGame")
    public Object joinGame(@RequestParam("gameID") String gameID, @RequestParam("playerID") String playerID) {

        // if invalid playerID
        if (playerID == null || playerID.length() != 36) {
            return ResponseEntity.status(400).body("Empty or invalid playerID: must be type UUID!");
        }

        // if invalid gameID
        if (gameID == null || gameID.length() != 36) {
            return ResponseEntity.status(400).body("Empty or invalid gameID: must be type UUID!");
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

            System.out.println(game);
            game.getTeams().forEach(team -> team.getPlayers().forEach(player1 -> {
                try {
                    player1.getSession().sendMessage(new TextMessage(logic.getAllCurrentPlayerNames(game)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
            return ResponseEntity.status(200).body(game);

        } catch (NullPointerException e) {
            return ResponseEntity.status(400).body("Player or Game does not exist!"); // no player|game found
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("ID: must be type UUID!"); // wrong format of playerID (UUID)
        }
    }

    @PostMapping(path = "/startRound2erSchnopsn")
    public Object startRound2erSchnopsn(@RequestParam("gameID") String gameID) {
        if (gameID == null || gameID.length() != 36) {
            return ResponseEntity.status(400).body("Empty or invalid gameID: must be type UUID!");
        }
        UUID realGameID;

        try {
            realGameID = UUID.fromString(gameID);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("Wrong color or wrong format of gameID"); // wrong format of gameID or wrong color
        }

        //Karten Methode 5 zurück
        Game game = GameLogic.findGame(storage.getActiveGames(), gameID);
        Map<Player, List<Card>> playerCardMap = new LinkedHashMap<>();
        playerCardMap = logic.giveOutCards(game);
        Card trumpCard = logic.getTrumpCard(game);
        game.setCurrentTrump(trumpCard.getColor());


        for (Player player : playerCardMap.keySet()) {
            try {
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(playerCardMap.get(player))));
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(trumpCard)));
                player.getSession().sendMessage(new TextMessage("\"myTurn:\"" + player.isMyTurn()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (player.isMyTurn()) {
                player.setMyTurn(false);
            } else {
                player.setMyTurn(true);
            }
        }
        //neuen playsCall setzten

        return ResponseEntity.status(400).body("Hurray!");
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

        return ResponseEntity.status(200).body(logic.isCallHigher(GameLogic.findGame(storage.getActiveGames(), gameID), validCall, player));
    }

    //Stichfunktion
    @PostMapping(path = "/makeMoveByCall")
    public Object makeMoveByCall(@RequestParam("gameID") String gameID, @RequestParam("playerID") String playerID, @RequestParam("color") String color, @RequestParam("value") int cardValue) {

        // if invalid playerID
        if (playerID == null || playerID.length() != 36) {
            return ResponseEntity.status(400).body("Empty or invalid playerID: must be type UUID!");
        }

        // if invalid gameID
        if (gameID == null || gameID.length() != 36) {
            return ResponseEntity.status(400).body("Empty or invalid gameID: must be type UUID!");
        }

        if (color == null) {
            return ResponseEntity.status(400).body("No color given!");
        }

        try {
            Game game = GameLogic.findGame(storage.getActiveGames(), gameID);
            Player player = GameLogic.findPlayer(storage.getActivePlayers(), playerID);
            Card card = logic.getCard(color, cardValue);
            UUID winnerID = logic.makeRightMove(game, card, player);
            logic.sendStingDataToPlayers(game, winnerID);
            return ResponseEntity.status(200).body(GameLogic.findPlayer(storage.getActivePlayers(), winnerID + "") + "");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("Invalid format of ID or invalid color.");
        }
    }

    @PostMapping(path = "/endOfRound2erSchnopsn")
    public Object endOfRound(@RequestParam("gameID") String gameID, @RequestParam("playerID") String playerID, @RequestParam("looserPoints") String points) {
        Game game = GameLogic.findGame(storage.getActiveGames(), gameID);
        Player winner = GameLogic.findPlayer(storage.getActivePlayers(), playerID);
        logic.endOfRound2erSchnopsn(winner, game, Integer.parseInt(points));
        //Frontend fragen ob gewinner oder verlierer die runde beendet
        game.getTeams().forEach(team -> team.getPlayers().forEach(player -> {
            try {
                player.getSession().sendMessage(new TextMessage("Round is over! winner: " + winner.getPlayerName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        return ResponseEntity.status(200).body(game);
    }

    @PostMapping(path = "/endOfGame2erSchnopsn")
    public Object endOfGame(@RequestParam("gameID") String gameID, @RequestParam("playerID") String playerID) {
        Game game = GameLogic.findGame(storage.getActiveGames(), gameID);
        Player player = GameLogic.findPlayer(storage.getActivePlayers(), playerID);

        return ResponseEntity.status(200).body("");
    }

    @PostMapping(path = "/getCards4erSchnopsn")
    public Object getCards4erSchnopsn(@RequestParam("gameID") String gameID){
        //Serverintern: jeder bekommt seine 5 Karten -> geschickt werden nur die ersten 3; dann Trumpf die letzten 2
        Game game = GameLogic.findGame(storage.getActiveGames(), gameID);
        List<Card> allCardsOfPlayer = new ArrayList<>();
        /*game.getTeams().forEach(team -> team.getPlayers().forEach(player -> {
            List<Card> allCardsOfPlayer = logic.giveOutCards(game).get(player)
            try {
                List<Card> cardList = new ArrayList<>();

                for (int i = 0; i < 3; i++) {
                    cardList.add()
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));*/
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

        UUID realGameID;
        Color realColor;

        try {
            realGameID = UUID.fromString(gameID);
            realColor = Color.valueOf(color.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("Wrong color or wrong format of gameID"); // wrong format of gameID or wrong color
        }

        Game game = GameLogic.findGame(storage.getActiveGames(), gameID);
        game.setCurrentTrump(realColor);
        logic.defineCaller(game);

        return ResponseEntity.status(200).body("started round successfully");
    }
}
