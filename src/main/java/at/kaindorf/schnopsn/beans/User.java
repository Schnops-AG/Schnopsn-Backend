package at.kaindorf.schnopsn.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

public class User extends Player{

    private String password;
    private String email;
    private Statistic statistic;

    public User(UUID playerID, String playerName, boolean caller, boolean playsCall, int playerNumber, boolean admin, String password, String email, Statistic statistic) {
        super(playerID, playerName, caller, playsCall, playerNumber, admin);
        this.password = password;
        this.email = email;
        this.statistic = statistic;
    }

    // region <getter, setter, toString>
    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public Statistic getStatistic() {
        return statistic;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setStatistic(Statistic statistic) {
        this.statistic = statistic;
    }

    @Override
    public String toString() {
        return "User{" +
                "password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", statistic=" + statistic +
                '}';
    }
    //endregion
}
