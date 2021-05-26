package at.kaindorf.schnopsn.dao;

import at.kaindorf.schnopsn.beans.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository{ //extends JpaRepository<User, UUID> {
    /*Optional<User> findUserByUserID(UUID userID);
    Optional<User> findUserByAge(int age);*/
}
