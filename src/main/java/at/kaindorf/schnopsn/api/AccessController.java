package at.kaindorf.schnopsn.api;

import at.kaindorf.schnopsn.beans.Game;
import at.kaindorf.schnopsn.beans.GameType;
import at.kaindorf.schnopsn.beans.Player;
import at.kaindorf.schnopsn.beans.User;
import at.kaindorf.schnopsn.bl.GameLogic;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        System.out.println(playerid);
        UUID realPlayerid = UUID.fromString(playerid);
        GameType realGameType = GameType.valueOf(gameType);

        Player player = activePlayers.stream().filter(player1 -> player1.getPlayerid().equals(realPlayerid)).findFirst().orElse(null);
        Game newGame = logic.createGame(realGameType,player);
        activeGames.add(newGame);
        return ResponseEntity.status(200).body(newGame);
    }

    @PostMapping(path = "/joinGame")
    public Object joinGame(@RequestBody UUID gameID, UUID playerID) {
        Player player = activePlayers.stream().filter(player1 -> player1.getPlayerid().equals(playerID)).findFirst().orElse(null);
        Game game = activeGames.stream().filter(game1 -> game1.getGameid().equals(gameID)).findFirst().orElse(null);

        if(game.getTeams().get(0).getPlayers().size() < 2){
            game.getTeams().get(0).getPlayers().add(player);
        }
        else if(game.getTeams().get(1).getPlayers().size() < 2){
            game.getTeams().get(1).getPlayers().add(player);
        }

        return ResponseEntity.status(200).body("joined game successfully");
    }

}
