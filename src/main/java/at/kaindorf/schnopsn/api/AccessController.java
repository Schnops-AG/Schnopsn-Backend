package at.kaindorf.schnopsn.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
@CrossOrigin(origins= "http://localhost:3000", methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.OPTIONS, RequestMethod.HEAD, RequestMethod.PUT})
public class AccessController {

    @GetMapping(path = "/test")
    public Object test(){
        return ResponseEntity.status(200).body("It's just a simple test.");
    }
}
