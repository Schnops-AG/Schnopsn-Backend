package at.kaindorf.schnopsn.api;

import at.kaindorf.schnopsn.beans.*;
import at.kaindorf.schnopsn.bl.GameLogic;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


@RestController
@RequestMapping("api/v1")
@CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.OPTIONS, RequestMethod.HEAD, RequestMethod.PUT})
public class AccessController {

    private final GameLogic logic = new GameLogic();
    private GameStorage storage = GameStorage.getInstance();
    private ObjectMapper mapper = new ObjectMapper();

    //TODO: JavaDocs machen
    //TODO: check three cases of SCHNAPSER //finished
    //TODO: Frontend besprechen: Farbenringerl //finished

    @PostMapping(path = "/createPlayer")
    public Object createUser(@RequestParam("playerName") String playerName) {

        // invalid playerName
        if (playerName == null || playerName.length() <= 0) {
            return ResponseEntity.status(400).body(new Message("error", "Empty or invalid playerName"));
        }
        Player newPlayer = new Player(UUID.randomUUID(), playerName, false, false, 0, false, false, 0, true,false, null);
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
            int count=0;
            for (Team team:game.getTeams()) {
                for (Player player1: team.getPlayers()) {
                    count++;
                }
            }
            final int finalCount = count;

            //System.out.println(game);
            game.getTeams().forEach(team -> team.getPlayers().forEach(player1 -> {
                try {
                    // if: more players join than allowed in a room
                    player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("join", logic.getAllCurrentPlayerNames(game)))));
                    if(finalCount== game.getMaxNumberOfPlayers()){
                        player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("game", game))));
                    }
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
        game.getPlayerCardMap().clear();
        Map<Player, List<Card>> playerCardMap;
        playerCardMap = logic.giveOutCards(game, 5);
        game.setPlayerCardMap(playerCardMap);

        Card trumpCard = logic.getTrumpCard(game);
        game.setCurrentTrump(trumpCard.getColor());
        game.setFaerbeln(false);

        //Stichpunkte zurücksetzen
        for (Team team : game.getTeams()) {
            team.setCurrentScore(0);
            team.setBuffer(0);
        }
        game.getPlayedCards().clear();

        Player caller=logic.getActualCaller(game);
        for (Player player : playerCardMap.keySet()) {
            player.setZudreher(false);

            if(player.getPlayerID()== caller.getPlayerID()){
                player.setMyTurn(true);
            }
            else{
                player.setMyTurn(false);
            }
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
        logic.defineCaller(game);


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
        player.setZudreher(true);
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
            try {
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("cards", game.getPlayerCardMap().get(player)))));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ResponseEntity.status(200).body(new Message("switchedTrumpCard", "switched successful"));

        }

        return ResponseEntity.status(200).body(new Message("error", "only with trumpf bur"));
    }

    @PostMapping(path = "/call20er40er")
    public Object call20er40er(@RequestParam("gameID") String gameID, @RequestParam("playerID") String playerID,@RequestParam("type") String typeID) {
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
        int type = logic.makeCall2erSchnopsn(game, player);

        if(type!=0){
            //Buffer oder gleich zu Stichpunkten
            if(game.getTeams().get((player.getPlayerNumber()+1)%2).getCurrentScore()==0) {
                game.getTeams().get((player.getPlayerNumber()+1)%2).setBuffer(game.getTeams().get((player.getPlayerNumber()+1)%2).getBuffer()+type);
            }
            else{
                game.getTeams().get((player.getPlayerNumber()+1)%2).setCurrentScore(game.getTeams().get((player.getPlayerNumber()+1)%2).getCurrentScore()+type);
            }
            //Sag allen der hat was angesagt
            Color color = game.getPlayerCardMap().get(player).stream().filter(card ->  card.isPriority()).findFirst().get().getColor();
            game.getTeams().forEach(team -> team.getPlayers().forEach(player1 -> {
                System.out.println(player1.isCaller());
                String jsonString="";
                try {
                    jsonString = new JSONObject().put("color",color).put("player",player.getPlayerID()).toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    //player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message(type+"er mit "+color, player.getPlayerID()))));
                    player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message(type+"er", jsonString))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
            //schick ihm seine neuen stichpunkte und priorityCards
            if(game.getGameType()==GameType._2ERSCHNOPSN) {
                try {
                    player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("stingScore", game.getTeams().get((player.getPlayerNumber() + 1) % 2).getCurrentScore()))));
                    //automatisch nachrichten zum beenden der Runde schicken und priorities zurückstellen
                    if (logic.checkIfRoundOver(game, mapper, player)) {
                        game.getPlayerCardMap().get(player).stream().forEach(card -> {
                            card.setPriority(true);
                        });
                    }
                    player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("priorityCards", game.getPlayerCardMap().get(player)))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(game.getGameType()==GameType._4ERSCHNOPSN){
                //setzte stiche so dass er keine mehr machen kann
                if((game.getCurrentHighestCall()==Call.SCHNAPSER || game.getCurrentHighestCall()==Call.KONTRASCHNAPSER) &&player.isPlaysCall()){
                    if(type==20){
                        player.setNumberOfStingsPerRound(player.getNumberOfStingsPerRound()+1);
                        game.setNumberOfStingsPerRound(game.getNumberOfStingsPerRound()+1);
                    }
                    else if(type==40){
                        player.setNumberOfStingsPerRound(player.getNumberOfStingsPerRound()+2);
                        game.setNumberOfStingsPerRound(game.getNumberOfStingsPerRound()+2);
                    }
                    //Wenn Call noch ok und fertig dann award ihn sonst den Gegner
                    if(logic.checkCall(game,player)){
                        if(game.getTeams().get((player.getPlayerNumber()+1)%2).getCurrentScore()>65){
                            logic.awardForPoints4erSchnopsn(player,game);
                            logic.sendScoreDataToPlayers4erSchnopsn(game,mapper,player);
                        }
                    }
                    else{
                        logic.awardForPoints4erSchnopsn(game.getTeams().get(player.getPlayerNumber()%2).getPlayers().get(0),game);
                        logic.sendScoreDataToPlayers4erSchnopsn(game,mapper,game.getTeams().get(player.getPlayerNumber()%2).getPlayers().get(0));
                    }
                }
                //Bei normalen spiel nur schauen ob fertig ist dann award
                else if(game.getCurrentHighestCall()==Call.NORMAL){
                    if(game.getTeams().get((player.getPlayerNumber()+1)%2).getCurrentScore()>65){
                        logic.awardForPoints4erSchnopsn(player,game);
                        logic.sendScoreDataToPlayers4erSchnopsn(game, mapper,player);
                    }
                }

            }

            return ResponseEntity.status(200).body(new Message("20er", "call 20er successful"));
        }


        return ResponseEntity.status(400).body(new Message("error", "kein König und Dame von gleicher Farbe vorhanden"));
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
        game.getPlayerCardMap().clear();
        Map<Player, List<Card>> playerCardMap = logic.giveOutCards(game, 3);
        game.setPlayerCardMap(playerCardMap);
        game.getPlayedCards().clear();
        game.getTeams().stream().forEach(team -> {
            team.setCurrentScore(0);
            team.setBuffer(0);
        });
        for (Player player : game.getPlayerCardMap().keySet()) {
            player.setActive(true);
            player.setMyTurn(false);
            player.setPlaysCall(false);
            player.setNumberOfStingsPerRound(0);
            if (player.isCaller()) {
                player.setMyTurn(true);
            }
            try {
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("forward", "./play"))));
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
    public Object startRound(@RequestParam("gameID") String gameID,@RequestParam("playerID") String playerID, @RequestParam("color") String color) {
        // if invalid playerID
        if (playerID == null || playerID.length() != 36) {
            return ResponseEntity.status(400).body(new Message("error", "Empty or invalid playerID: must be type UUID!"));
        }
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
        Player playerTrump = GameLogic.findPlayer(storage.getActivePlayers(),playerID);
        Map<Player, List<Card>> playerCardMap = logic.giveOutCards(game, 2);
        Card trumpColorCard = null;
        if(realColor==Color.RANDOM){
            trumpColorCard =playerCardMap.get(playerTrump).get(0);
            game.setCurrentTrump(trumpColorCard.getColor());
        }
        else{
            try {
                trumpColorCard = new Card("temp",1,new URL("http://link1"),realColor,true);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            game.setCurrentTrump(realColor);
        }
        playerTrump.setPlaysCall(true);
        playerTrump.setMyTurn(true);
        for (Player player : playerCardMap.keySet()) {
            game.getPlayerCardMap().get(player).addAll(playerCardMap.get(player));
            try {
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("cards", game.getPlayerCardMap().get(player))))); // return all 5 cards
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("trump", trumpColorCard))));
                player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("callTurn", player.isMyTurn()))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return ResponseEntity.status(200).body(new Message("info", "started round successfully"));
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
        boolean callPeriod= logic.callPeriod(game, mapper, player);


        //Benachrichtigen dass keine Calls mehr gemacht werden
        if(!callPeriod){

            switch(game.getCurrentHighestCall()){
                case FARBENRINGERL,TRUMPFFARBENRINGERL,KONTRATRUMPFFARBENRINGERL:
                    Player callPlayer=null;
                    for (Team team : game.getTeams()) {
                        callPlayer = team.getPlayers().stream().filter(Player::isPlaysCall).findFirst().orElse(null);
                        if (callPlayer != null) {
                            break;
                        }
                    }
                    boolean sameColor =true;
                    Color firstColor = game.getPlayerCardMap().entrySet().iterator().next().getValue().get(0).getColor();
                    switch(game.getCurrentHighestCall()){
                        case FARBENRINGERL:
                            for (Card card: game.getPlayerCardMap().get(callPlayer)) {
                                if(card.getColor()!=firstColor){
                                    sameColor=false;
                                }
                            }
                            break;
                        case TRUMPFFARBENRINGERL,KONTRATRUMPFFARBENRINGERL:
                            for (Card card: game.getPlayerCardMap().get(callPlayer)) {
                                if(card.getColor()!=firstColor || firstColor!=game.getCurrentTrump()){
                                    sameColor=false;
                                }
                            }
                            break;
                    }
                    if(sameColor){
                        logic.awardForPoints4erSchnopsn(callPlayer,game);
                        logic.sendScoreDataToPlayers4erSchnopsn(game,mapper,callPlayer);
                    }
                    break;
            }

            game.getTeams().forEach(team -> team.getPlayers().forEach(player1 -> {
            try {
                player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("myTurn", player1.isMyTurn()))));
                player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("message", "finished with Calls!"))));
            } catch (IOException e) {
                e.printStackTrace();
            }
            }));
        }
        else{
            game.getTeams().forEach(team -> team.getPlayers().forEach(player1 -> {
                try {
                    player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("highestCall", game.getCurrentHighestCall()))));
                    player1.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(new Message("callTurn", player1.isMyTurn()))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        }

        return ResponseEntity.status(200).body(new Message("info", "status: 200"));
    }

    @PostMapping(path = "/getAvailableCalls")
    public Object getAvailableCalls(@RequestParam("gameID") String gameID, @RequestParam("playerID") String playerID){
        // if invalid playerID
        if (playerID == null || playerID.length() != 36) {
            return ResponseEntity.status(400).body("Empty or invalid playerID: must be type UUID!");
        }
        // if invalid gameID
        if (gameID == null || gameID.length() != 36) {
            return ResponseEntity.status(400).body("Empty or invalid gameID: must be type UUID!");
        }

        Player player = GameLogic.findPlayer(storage.getActivePlayers(), playerID);
        if (player == null) {
            return ResponseEntity.status(404).body("No player found");
        }
        Game game = GameLogic.findGame(storage.getActiveGames(), gameID);
        List<Call> calls = new ArrayList<>();
        calls.addAll(Arrays.asList(Call.values()));
        System.out.println("managed");

        if(player.isCaller()){
            calls.remove(Call.KONTRABAUER);
            calls.remove(Call.KONTRASCHNAPSER);
            calls.remove(Call.KONTRATRUMPFFARBENRINGERL);
        }
        else{
            calls.remove(Call.SCHNAPSER);
            calls.remove(Call.BAUER);
            calls.remove(Call.TRUMPFFARBENRINGERL);
        }
        return ResponseEntity.status(200).body(new Message("availableCalls", calls));
    }

}
