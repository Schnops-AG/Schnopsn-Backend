package at.kaindorf.schnopsn.beans;

import lombok.AllArgsConstructor;
import lombok.Getter;

public enum Role {
    JONAS("verlierer",10),ARMERMEISTER("Armer Meister",100),
    WALKCHAMPION("Walkchampion",1000), RÖNTGEN_HORWATH("Röntgen Horwath",100000);
    private final String roleName;
    private final int minPoints;

    Role(String roleName, int minPoints) {
        this.roleName = roleName;
        this.minPoints = minPoints;
    }

    // region <getter>
    public String getRoleName() {
        return roleName;
    }
    public Integer getMinPoints() {
        return minPoints;
    }
    //endregion
}
