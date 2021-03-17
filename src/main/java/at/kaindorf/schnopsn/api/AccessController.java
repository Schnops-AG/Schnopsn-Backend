package at.kaindorf.schnopsn.api;

import at.kaindorf.schnopsn.beans.User;
import at.kaindorf.schnopsn.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1")
@CrossOrigin(origins= "http://localhost:3000", methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.OPTIONS, RequestMethod.HEAD, RequestMethod.PUT})
public class AccessController {

    private final UserService userService;

    @Autowired
    public AccessController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = "/test")
    public Object test(){
        return ResponseEntity.status(200).body("test");
    }

    @PostMapping(path = "/test")
    public Object postTest(@RequestBody User user){
        User u1 = userService.getUserByID(user.getUserID());

        return ResponseEntity.status(200).body("Hello " + user);
    }
}
