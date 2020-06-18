package FogOSSecurity;

/**
 *  Define a role of an entity
 *  @author Hyeonmin Lee
 */
public enum Role {
    INITIATOR(0, "initiator"),
    RESPONDER(1, "responder");

    private final int role;
    private final String str;

    /**
     * Construct the Role
     * @param role role number; ex) 0: Initiator, 1: Responder
     * @param str the role; ex) initiator
     */
    Role(int role, String str) {
        this.role = role;
        this.str = str;
    }
}
