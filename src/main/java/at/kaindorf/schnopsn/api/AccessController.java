package at.kaindorf.schnopsn.api;

import at.kaindorf.schnopsn.beans.*;
import at.kaindorf.schnopsn.bl.GameLogic;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequestMapping("api/v1")
@CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.OPTIONS, RequestMethod.HEAD, RequestMethod.PUT})
public class AccessController {

    private final GameLogic logic = new GameLogic();
    private final List<Game> activeGames = new ArrayList<>();
    private final List<Player> activePlayers = new ArrayList<>();

    @PostMapping(path = "/createPlayer")
    public Object createUser(@RequestParam("playerName") String playerName) {

        // invalid playerName
        if(playerName == null || playerName.length() <= 0){
            return ResponseEntity.status(400).body("Empty or invalid playerName");
        }

        Player newPlayer = new Player(UUID.randomUUID(), playerName, false, false, 0, false,null);
        activePlayers.add(newPlayer);
        return ResponseEntity.status(200).body(newPlayer);
    }

    @PostMapping(path = "/createGame")
    public Object createGame(@RequestParam("gameType") String gameType, @RequestParam("playerID") String playerID) {

        // if wrong gameType
        if(gameType == null || !(GameType._2ERSCHNOPSN.toString().equals(gameType) || GameType._4ERSCHNOPSN.toString().equals(gameType))){
            return ResponseEntity.status(400).body("Empty or invalid gameType: _2ERSCHNOPSN, _4ERSCHNOPSN");
        }

        // if invalid playerID
        if(playerID == null || playerID.length() != 36){
            return ResponseEntity.status(400).body("Empty or invalid playerID: must be type UUID!");
        }


        try {
            GameType realGameType = GameType.valueOf(gameType);
            Player player = GameLogic.findPlayer(activePlayers, playerID);
            player.setCaller(true);
            player.setAdmin(true);
            Game newGame = logic.createGame(realGameType, player);
            activeGames.add(newGame);
            return ResponseEntity.status(200).body(newGame);
        } catch (NullPointerException e) {
            return ResponseEntity.status(400).body("Player does not exist!"); // no player found
        } catch(IllegalArgumentException e){
            return ResponseEntity.status(400).body("Invalid playerID: must be type UUID!"); // wrong format of playerID (UUID)
        }
    }

    @PostMapping(path = "/joinGame")
    public Object joinGame(@RequestParam("gameID") String gameID, @RequestParam("playerID") String playerID) {

        // if invalid playerID
        if(playerID == null || playerID.length() != 36){
            return ResponseEntity.status(400).body("Empty or invalid playerID: must be type UUID!");
        }

        // if invalid gameID
        if(gameID == null || gameID.length() != 36){
            return ResponseEntity.status(400).body("Empty or invalid gameID: must be type UUID!");
        }

        try {
            Player player = GameLogic.findPlayer(activePlayers, playerID);
            Game game = GameLogic.findGame(activeGames, gameID);
            int numberOfPlayers = GameLogic.getCurrentNumberOfPlayers(game);
            if (numberOfPlayers < game.getMaxNumberOfPlayers()) {
                player.setPlayerNumber(numberOfPlayers + 1);
                if((numberOfPlayers + 1) % 2 == 0){
                    game.getTeams().get(1).getPlayers().add(player);
                }
                else{
                    game.getTeams().get(0).getPlayers().add(player);
                }
            }

            System.out.println(game);
            //activeGames.stream().filter(game1 -> game1.getGameid().equals(game.getGameid())).findFirst().get().setPlayers(game.getPlayers());
            return ResponseEntity.status(200).body(GameLogic.findGame(activeGames, gameID));

        } catch (NullPointerException e) {
            return ResponseEntity.status(400).body("Player or Game does not exist!"); // no player|game found
        } catch(IllegalArgumentException e){
            return ResponseEntity.status(400).body("ID: must be type UUID!"); // wrong format of playerID (UUID)
        }
    }

    @PostMapping(path = "/startRound")
    public Object startRound(@RequestParam("gameID") String gameID, @RequestParam("color") String color) {

        // if invalid gameID
        if(gameID == null || gameID.length() != 36){
            return ResponseEntity.status(400).body("Empty or invalid gameID: must be type UUID!");
        }

        if(color == null){
            return ResponseEntity.status(400).body("No color given!");
        }

        UUID realGameID;
        Color realColor;

        try{
            realGameID = UUID.fromString(gameID);
            realColor = Color.valueOf(color.toUpperCase());
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(400).body("Wrong color or wrong format of gameID"); // wrong format of gameID or wrong color
        }

        activeGames.stream().filter(game1 -> game1.getGameID().equals(realGameID)).findFirst().orElse(null).setCurrentTrump(realColor);

        //neuen Caller definieren
        //Wenn 4erschnopsn dann caller sonst ned
//        int oldCallerNumber = activeGames.stream().filter(game1 -> game1.getGameID().equals(realGameID)).findFirst().orElsegetPlayers().stream().filter(Player::isCaller).findFirst().orElse(null).getPlayerNumber();
//        activeGames.stream().filter(game1 -> game1.getGameID().equals(realGameID)).findFirst().orElse(null).getPlayers().stream().filter(Player::isCaller).findFirst().orElse(null).setCaller(false);
//        activeGames.stream().filter(game -> game.getGameID().equals(realGameID)).findFirst().flatMap(game -> game.getPlayers().stream().filter(player -> player.getPlayerNumber() == oldCallerNumber % 4 + 1).findFirst()).ifPresent(player -> player.setCaller(true));

        return ResponseEntity.status(200).body("started round successfully");
    }

    @PostMapping(path = "/makeCall")
    public Object makeCall(@RequestParam("gameID") String gameID, @RequestParam("playerID") String playerID, @RequestParam("call") String call) {

        // if invalid playerID
        if(playerID == null || playerID.length() != 36){
            return ResponseEntity.status(400).body("Empty or invalid playerID: must be type UUID!");
        }

        // if invalid gameID
        if(gameID == null || gameID.length() != 36){
            return ResponseEntity.status(400).body("Empty or invalid gameID: must be type UUID!");
        }

        if(call == null){
            return ResponseEntity.status(400).body("No call given!");
        }

        Call validCall;
        try{
            validCall = Call.valueOf(call.toUpperCase());
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(400).body("Invalid call!");
        }

        Player player = GameLogic.findPlayer(activePlayers, playerID);
        if(player == null){
            return ResponseEntity.status(404).body("No player found");
        }

        return ResponseEntity.status(200).body(logic.isCallHigher(GameLogic.findGame(activeGames, gameID), validCall, player));
    }

    @PostMapping(path = "/makeMoveByCall")
    public Object makeMoveByCall(@RequestParam("gameID") String gameID, @RequestParam("playerID") String playerID, @RequestParam("color") String color, @RequestParam("value") int cardValue) {

        // if invalid playerID
        if(playerID == null || playerID.length() != 36){
            return ResponseEntity.status(400).body("Empty or invalid playerID: must be type UUID!");
        }

        // if invalid gameID
        if(gameID == null || gameID.length() != 36){
            return ResponseEntity.status(400).body("Empty or invalid gameID: must be type UUID!");
        }

        if(color == null){
            return ResponseEntity.status(400).body("No color given!");
        }

        try{
            Game game = GameLogic.findGame(activeGames, gameID);
            Player player = GameLogic.findPlayer(activePlayers, playerID);
            Card card = logic.getCard(color, cardValue);
            UUID winner = logic.makeRightMove(game, card, player);
            if (winner == null) {
                return ResponseEntity.status(200).body("valid move but not all players have played yet");
            } else {
                return ResponseEntity.status(200).body(winner);
            }

        }catch(IllegalArgumentException e){
            return ResponseEntity.status(400).body("Invalid format of ID or invalid color.");
        }
    }


}
