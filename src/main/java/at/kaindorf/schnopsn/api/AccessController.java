package at.kaindorf.schnopsn.api;

import at.kaindorf.schnopsn.beans.*;
import at.kaindorf.schnopsn.bl.GameLogic;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static at.kaindorf.schnopsn.beans.Call.BETTLER;

@RestController
@RequestMapping("api/v1")
@CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.OPTIONS, RequestMethod.HEAD, RequestMethod.PUT})
public class AccessController {

    private final GameLogic logic = new GameLogic();
    private List<Game> activeGames = new ArrayList<>();
    private List<Player> activePlayers = new ArrayList<>();

    @PostMapping(path = "/createPlayer")
    public Object createUser(@RequestParam("playerName") String playerName) {
        Player newPlayer = new Player(UUID.randomUUID(), playerName, false, false, 0,false);
        activePlayers.add(newPlayer);
        return ResponseEntity.status(200).body(newPlayer);
    }

    @PostMapping(path = "/createGame")
    public Object createGame(@RequestParam("gameType") String gameType, @RequestParam("playerID") String playerid) {
        try {
            GameType realGameType = GameType.valueOf(gameType);
            Player player = GameLogic.findPlayer(activePlayers, playerid);
            player.setCaller(true);
            player.setAdmin(true);
            Game newGame = logic.createGame(realGameType, player);
            activeGames.add(newGame);
            return ResponseEntity.status(200).body(newGame);
        }
        catch(NullPointerException e){
            //kein Spieler gefunden
            return ResponseEntity.status(400).body("Player does not exist!");
        }
    }

    @PostMapping(path = "/joinGame")
    public Object joinGame(@RequestParam("gameID") String gameID, @RequestParam("playerID") String playerID) {
        try {
            Player player = GameLogic.findPlayer(activePlayers, playerID);
            Game game = GameLogic.findGame(activeGames, gameID);

            if (game.getPlayers().size() < game.getMaxNumberOfPlayers()) {
                player.setPlayerNumber(game.getPlayers().size() + 1);
                game.getPlayers().add(player);
            }

            System.out.println(game);
            //activeGames.stream().filter(game1 -> game1.getGameid().equals(game.getGameid())).findFirst().get().setPlayers(game.getPlayers());
            return ResponseEntity.status(200).body(GameLogic.findGame(activeGames, gameID));
        }
        catch(NullPointerException e){
            //game nicht da
            return ResponseEntity.status(400).body("Game does not exist!");
        }
    }

    @PostMapping(path = "/startRound")
    public Object startRound(@RequestParam("gameID") String gameID, @RequestParam("color") String color) {
        UUID realGameID = UUID.fromString(gameID);
        Color realColor = Color.valueOf(color.toUpperCase());
        activeGames.stream().filter(game1 -> game1.getGameID().equals(realGameID)).findFirst().orElse(null).setCurrentTrump(realColor);

        //neuen Caller definieren
        //Wenn 4erschnopsn dann caller sonst ned
        int oldCallerNumber = activeGames.stream().filter(game1 -> game1.getGameID().equals(realGameID)).findFirst().orElse(null).getPlayers().stream().filter(player -> player.isCaller()).findFirst().orElse(null).getPlayerNumber();
        activeGames.stream().filter(game1 -> game1.getGameID().equals(realGameID)).findFirst().orElse(null).getPlayers().stream().filter(player -> player.isCaller()).findFirst().orElse(null).setCaller(false);
        activeGames.stream().filter(game -> game.getGameID().equals(realGameID)).findFirst().flatMap(game -> game.getPlayers().stream().filter(player -> player.getPlayerNumber() == oldCallerNumber % 4 + 1).findFirst()).ifPresent(player -> player.setCaller(true));

        return ResponseEntity.status(200).body("started round successfully");
    }

    @PostMapping(path = "/makeCall")
    public Object makeCall(@RequestParam("gameID") String gameID, @RequestParam("playerID") String playerID, @RequestParam("call") String call) {
        return ResponseEntity.status(200).body(logic.isCallHigher(GameLogic.findGame(activeGames, gameID), Call.valueOf(call.toUpperCase()), GameLogic.findPlayer(activePlayers, playerID)));
    }

    @PostMapping(path = "/makeMoveByCall")
    public Object makeMoveByCall(@RequestParam("gameID") String gameID, @RequestParam("playerID") String playerID, @RequestParam("color") String color, @RequestParam("value") int cardValue) {
        Game game = GameLogic.findGame(activeGames, gameID);
        Player player = GameLogic.findPlayer(activePlayers, playerID);
        Card card = logic.getCard(color, cardValue);

        switch (game.getCurrentHighestCall()) {
            case BETTLER, ASSENBETTLER, PLAUDERER:
                if (game.getPlayedCards().size() < game.getMaxNumberOfPlayers() - 1 && game.getPlayedCards().keySet().stream().filter(player1 -> player1.getPlayerID() == player.getPlayerID()).findFirst().orElse(null) == null) {
                    game.getPlayedCards().put(player, card);
                }
                if (game.getPlayedCards().size() == game.getMaxNumberOfPlayers() - 1) {
                    if(logic.trumpNeeded(game.getCurrentHighestCall())){
                        return ResponseEntity.status(200).body(logic.getPlayerWithHighestCard(game.getPlayedCards(), game.getCurrentTrump()));
                    }
                    else {
                        return ResponseEntity.status(200).body(logic.getPlayerWithHighestCard(game.getPlayedCards(), null));
                    }
                }
                break;

            default:
                if (game.getPlayedCards().size() < game.getMaxNumberOfPlayers() && game.getPlayedCards().keySet().stream().filter(player1 -> player1.getPlayerID() == player.getPlayerID()).findFirst().orElse(null) == null) {
                    game.getPlayedCards().put(player, card);
                }
                if (game.getPlayedCards().size() == game.getMaxNumberOfPlayers()) {
                    if(logic.trumpNeeded(game.getCurrentHighestCall())){
                        return ResponseEntity.status(200).body(logic.getPlayerWithHighestCard(game.getPlayedCards(), game.getCurrentTrump()));
                    }
                    else {
                        return ResponseEntity.status(200).body(logic.getPlayerWithHighestCard(game.getPlayedCards(), null));
                    }
                }
                break;
        }

        return ResponseEntity.status(200).body("valid move but not all players have played yet");
    }


}
