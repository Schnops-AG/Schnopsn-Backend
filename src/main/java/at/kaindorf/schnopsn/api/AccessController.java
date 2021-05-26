package at.kaindorf.schnopsn.api;

import at.kaindorf.schnopsn.beans.*;
import at.kaindorf.schnopsn.bl.GameLogic;
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
    /*private final List<Game> activeGames = new ArrayList<>();
    private final List<Player> activePlayers = new ArrayList<>();*/
    private GameStorage storage = GameStorage.getInstance();

    @PostMapping(path = "/createPlayer")
    public Object createUser(@RequestParam("playerName") String playerName) {

        // invalid playerName
        if (playerName == null || playerName.length() <= 0) {
            return ResponseEntity.status(400).body("Empty or invalid playerName");
        }
        Player newPlayer = new Player(UUID.randomUUID(), playerName, false, false, 0, false, null);
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
            player.setAdmin(true);
            Game newGame = logic.createGame(realGameType, player);
            storage.getActiveGames().add(newGame);
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

    @PostMapping(path = "/startRound")
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


        //neuen Caller definieren
        //Wenn 4erschnopsn dann caller sonst ned
        Game game = GameLogic.findGame(storage.getActiveGames(), gameID);
        game.setCurrentTrump(realColor);
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

        return ResponseEntity.status(200).body(logic.isCallHigher(GameLogic.findGame(storage.getActiveGames(), gameID), validCall, player));
    }

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
            if (winnerID == null) {
                return ResponseEntity.status(200).body("valid move but not all players have played yet");
            } else {
                Player winner = GameLogic.findPlayer(storage.getActivePlayers(), winnerID.toString());
                logic.awardForPoints(winner, game);
                return ResponseEntity.status(200).body(winner.getPlayerName());
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("Invalid format of ID or invalid color.");
        }
    }


}
