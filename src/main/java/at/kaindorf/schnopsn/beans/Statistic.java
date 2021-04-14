package at.kaindorf.schnopsn.beans;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Statistic {
    private int wins;
    private int looses;
    private int games;
    private GameType gameType;
    private Role rolename;
    private int pointsOverall;
}
