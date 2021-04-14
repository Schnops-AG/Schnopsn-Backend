package at.kaindorf.schnopsn.beans;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Role {
    JONAS("verlierer",10);
    private final String roleName;
    private final int minPoints;
}
