package at.kaindorf.schnopsn.beans;

import lombok.Data;
import java.util.UUID;

@Data
public class Player {
    private UUID playerid;
    private String playername;
    private boolean caller;
    private boolean active;
}
