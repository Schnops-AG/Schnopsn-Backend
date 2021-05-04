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
        Player newPlayer = new Player(UUID.randomUUID(),playerName,false,true,0);
        activePlayers.add(newPlayer);
        return ResponseEntity.status(200).body(newPlayer);
    }

    @PostMapping(path = "/createGame")
    public Object createGame(@RequestParam("gameType") String gameType, @RequestParam("playerID") String playerid){
        UUID realPlayerid = UUID.fromString(playerid);
        GameType realGameType = GameType.valueOf(gameType);

        Player player = activePlayers.stream().filter(player1 -> player1.getPlayerid().equals(realPlayerid)).findFirst().orElse(null);
        player.setPlayerNumber(1);
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
            player.setPlayerNumber(3);
            game.getTeams().get(0).getPlayers().add(player);
        }
        else if(game.getTeams().get(1).getPlayers().size() < 2){
            if(game.getTeams().get(1).getPlayers().size()==0){
                player.setPlayerNumber(2);
            }
            else{
                player.setPlayerNumber(4);
            }
            game.getTeams().get(1).getPlayers().add(player);
        }
        System.out.println(game);
        activeGames.stream().filter(game1 -> game1.getGameid().equals(game.getGameid())).findFirst().get().setTeams(game.getTeams());
        return ResponseEntity.status(200).body(game);
    }
    @PostMapping(path = "/startRound")
    public Object startRound(@RequestParam("gameID") String gameID,@RequestParam("color") String color){
        UUID realGameID = UUID.fromString(gameID);
        Color realColor = Color.valueOf(color);
        activeGames.stream().filter(game1 -> game1.getGameid().equals(realGameID)).findFirst().orElse(null).setCurrentTrump(realColor);
        List<Team> teams = activeGames.stream().filter(game -> game.getGameid().equals(realGameID)).findFirst().orElse(null).getTeams();
        //stream().filter(team -> team.getPlayers().stream().filter(player1 -> player1.getPlayerNumber() == 1).findFirst());
        int oldCallerNumber=0;
        for (Team team:teams) {
            for (Player player:team.getPlayers()) {
                if(player.isCaller()){
                    player.setCaller(false);
                    oldCallerNumber=player.getPlayerNumber();
                    if(oldCallerNumber==4){
                        teams.get(0).getPlayers().get(0).setCaller(true);
                    }
                }
                if(player.getPlayerNumber()==oldCallerNumber%4+1){
                    player.setCaller(true);
                }
            }
        }
        return ResponseEntity.status(200).body("success");
    }

    @PostMapping(path = "/makeCall")
    public Object makeCall(@RequestBody String jsonMap) {
        try {
            Map<String,String> result = new ObjectMapper().readValue(jsonMap, LinkedHashMap.class);
            //System.out.println(result);
            return ResponseEntity.status(200).body(logic.choosePlayerWhoMakeHighestCall(result));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
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
