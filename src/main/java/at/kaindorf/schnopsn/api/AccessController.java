package at.kaindorf.schnopsn.api;

import at.kaindorf.schnopsn.beans.*;
import at.kaindorf.schnopsn.bl.GameLogic;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("api/v1")
@CrossOrigin(origins= "http://localhost:3000", methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.OPTIONS, RequestMethod.HEAD, RequestMethod.PUT})
public class AccessController {

    private final GameLogic logic = new GameLogic();
    private List<Game> activeGames = new ArrayList<>();
    private List<Player> activePlayers = new ArrayList<>();

    @PostMapping(path = "/createPlayer")
    public Object createUser(@RequestBody String playerName){
        Player newPlayer = new Player(UUID.randomUUID(),playerName,false,false,0);
        activePlayers.add(newPlayer);
        return ResponseEntity.status(200).body(newPlayer);
    }

    @PostMapping(path = "/createGame")
    public Object createGame(@RequestParam("gameType") String gameType, @RequestParam("playerID") String playerid){
        GameType realGameType = GameType.valueOf(gameType);

        Player player = GameLogic.findPlayer(activePlayers,playerid);
        player.setCaller(true);
        Game newGame = logic.createGame(realGameType,player);
        activeGames.add(newGame);
        return ResponseEntity.status(200).body(newGame);
    }

    @PostMapping(path = "/joinGame")
    public Object joinGame(@RequestParam("gameID") String gameID,@RequestParam("playerID") String playerID) {
        Player player = GameLogic.findPlayer(activePlayers, playerID);
        Game game = GameLogic.findGame(activeGames, gameID);

        if(game.getPlayers().size() < game.getMaxNumberOfPlayers()){
            player.setPlayerNumber(game.getPlayers().size() + 1);
            game.getPlayers().add(player);
        }

        System.out.println(game);
        //activeGames.stream().filter(game1 -> game1.getGameid().equals(game.getGameid())).findFirst().get().setPlayers(game.getPlayers());
        return ResponseEntity.status(200).body(GameLogic.findGame(activeGames, gameID));
    }

    @PostMapping(path = "/startRound")
    public Object startRound(@RequestParam("gameID") String gameID,@RequestParam("color") String color){
        UUID realGameID = UUID.fromString(gameID);
        Color realColor = Color.valueOf(color);
        activeGames.stream().filter(game1 -> game1.getGameid().equals(realGameID)).findFirst().orElse(null).setCurrentTrump(realColor);

        //neuen Caller definieren
        int oldCallerNumber = activeGames.stream().filter(game1 -> game1.getGameid().equals(realGameID)).findFirst().orElse(null).getPlayers().stream().filter(player -> player.isCaller()).findFirst().orElse(null).getPlayerNumber();
        activeGames.stream().filter(game1 -> game1.getGameid().equals(realGameID)).findFirst().orElse(null).getPlayers().stream().filter(player -> player.isCaller()).findFirst().orElse(null).setCaller(false);
        activeGames.stream().filter(game -> game.getGameid().equals(realGameID)).findFirst().flatMap(game -> game.getPlayers().stream().filter(player -> player.getPlayerNumber() == oldCallerNumber%4+1).findFirst()).ifPresent(player -> player.setCaller(true));

        return ResponseEntity.status(200).body("success");
    }

    @PostMapping(path = "/makeCall")
    public Object makeCall(@RequestParam("gameID") String gameID,@RequestParam("playerID") String playerID,@RequestParam("call") String call) {
        return ResponseEntity.status(200).body(logic.isCallHigher(GameLogic.findGame(activeGames, gameID), Call.valueOf(call), GameLogic.findPlayer(activePlayers,playerID)));
    }

    @PostMapping(path = "/makeMoveByCall")
    public Object makeMoveByCall(@RequestBody String jsonMap) {
        //return wer stich kriegt
        try {
            //playerid, Kartenname
            Map<String, String> result = new ObjectMapper().readValue(jsonMap, LinkedHashMap.class);
            return ResponseEntity.status(200).body("jdfh");//logic.choosePlayerWhoGetsStich(result,"trumpf"));
        }
        catch(JsonProcessingException e){
            e.printStackTrace();
        }
        return null;
    }
}
