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

    //private final UserService userService;
    private final GameLogic logic = new GameLogic();
    private List<Game> activeGames = new ArrayList<>();
    private List<Player> activePlayers = new ArrayList<>();

    /*@Autowired
    public AccessController(UserService userService) {
        this.userService = userService;
    }*/

    @GetMapping(path = "/test")
    public Object test(){
        return ResponseEntity.status(200).body("test");
    }

    @PostMapping(path = "/test")
    public Object postTest(@RequestBody User user){
        return ResponseEntity.status(200).body("Hello ");
    }

    @PostMapping(path = "/createPlayer")
    public Object createUser(@RequestBody String playerName){
        Player newPlayer = new Player(UUID.randomUUID(),playerName,false,true);
        activePlayers.add(newPlayer);
        return ResponseEntity.status(200).body(newPlayer);
    }

    @PostMapping(path = "/createGame")
    public Object createGame(@RequestParam("gameType") String gameType, @RequestParam("playerID") String playerid){
        UUID realPlayerid = UUID.fromString(playerid);
        GameType realGameType = GameType.valueOf(gameType);

        Player player = activePlayers.stream().filter(player1 -> player1.getPlayerid().equals(realPlayerid)).findFirst().orElse(null);
        Game newGame = logic.createGame(realGameType,player);
        activeGames.add(newGame);
        return ResponseEntity.status(200).body(newGame);
    }

    @PostMapping(path = "/joinGame")
    public Object joinGame(@RequestParam("gameID") String gameID,@RequestParam("playerID") String playerID) {
        UUID realGameID = UUID.fromString(gameID);
        UUID realPlayerID = UUID.fromString(playerID);
        Player player = activePlayers.stream().filter(player1 -> player1.getPlayerid().equals(realPlayerID)).findFirst().orElse(null);
        Game game = activeGames.stream().filter(game1 -> game1.getGameid().equals(realGameID)).findFirst().orElse(null);

        if(game.getTeams().get(0).getPlayers().size() < 2){
            game.getTeams().get(0).getPlayers().add(player);
        }
        else if(game.getTeams().get(1).getPlayers().size() < 2){
            game.getTeams().get(1).getPlayers().add(player);
        }
        System.out.println(game);
        activeGames.stream().filter(game1 -> game1.getGameid().equals(game.getGameid())).findFirst().get().setTeams(game.getTeams());
        return ResponseEntity.status(200).body(game);
    }
    @PostMapping(path = "/makeCall")
    public Object makeCall(@RequestBody String jsonMap) {
        try {
            Map<String,String> result = new ObjectMapper().readValue(jsonMap, LinkedHashMap.class);
            //System.out.println(result);
            UUID playerWithHighestAnsage = null;
            int highestVal = 0;
            for (String id : result.keySet()) {
                //System.out.println(result.get(id));
                int value = Call.valueOf(result.get(id).toUpperCase()).getValue();
                if(value > highestVal){
                    highestVal = value;
                    playerWithHighestAnsage = UUID.fromString(id);
                }
            }
            return ResponseEntity.status(200).body(playerWithHighestAnsage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }
}
