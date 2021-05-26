package at.kaindorf.schnopsn.service;

import at.kaindorf.schnopsn.beans.User;
import at.kaindorf.schnopsn.dao.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    //private final UserRepository userRepository;

    /*public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserByID(UUID userID){
        return userRepository.findUserByUserID(userID).orElse(null);
    }*/
}
