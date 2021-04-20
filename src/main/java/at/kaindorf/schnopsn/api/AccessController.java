package at.kaindorf.schnopsn.api;

import at.kaindorf.schnopsn.beans.Game;
import at.kaindorf.schnopsn.beans.GameType;
import at.kaindorf.schnopsn.beans.Player;
import at.kaindorf.schnopsn.beans.User;
import at.kaindorf.schnopsn.bl.GameLogic;
import at.kaindorf.schnopsn.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1")
@CrossOrigin(origins= "http://localhost:3000", methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.OPTIONS, RequestMethod.HEAD, RequestMethod.PUT})
public class AccessController {

    //private final UserService userService;
    private GameLogic logic = new GameLogic();
    private List<Game> activeGames = new ArrayList<>();
    private List<Player> activePlayer = new ArrayList<>();

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
    public Object createUser(@RequestBody String playername){
        Player newPlayer = new Player(UUID.randomUUID(),playername,false,true);
        activePlayer.add(newPlayer);
        return ResponseEntity.status(200).body(newPlayer);
    }

    @PostMapping(path = "/createGame")
    public Object creatGame(@RequestBody String gameType, @NonNull String playerid){
        System.out.println(playerid);
        UUID realPlayerid = UUID.fromString(playerid);
        GameType realGameType = GameType.valueOf(gameType);

        Player player = activePlayer.stream().filter(player1 -> player1.getPlayerid().equals(realPlayerid)).findFirst().orElse(null);
        Game newGame = logic.createGame(realGameType,player);
        activeGames.add(newGame);
        return ResponseEntity.status(200).body(newGame);
    }

    @PostMapping(path = "/joinGame")
    public Object creatGame(@RequestBody UUID gameid, UUID playerid) {
        Player player = activePlayer.stream().filter(player1 -> player1.getPlayerid().equals(playerid)).findFirst().orElse(null);
        Game game = activeGames.stream().filter(game1 -> game1.getGameid().equals(gameid)).findFirst().orElse(null);

        if(game.getTeams().get(0).getPlayers().size() < 2){
            game.getTeams().get(0).getPlayers().add(player);
        }
        else if(game.getTeams().get(1).getPlayers().size() < 2){
            game.getTeams().get(1).getPlayers().add(player);
        }

        return ResponseEntity.status(200).body("joined game successfully");
    }

}
