package FogOSSecurity;

/**
 *  Implements an API for content type of messages
 *  @author Hyeonmin Lee
 */
public enum ContentType {
    NONE(0),
    /**
     * For handshake messages
     */
    HANDSHAKE(22),
    /**
     * For application messages
     */
    APPLICATION(20);

    private final byte number;

    /**
     * Construct the ContentType
     * @param number the byte number of a ContentType
     */
    ContentType(int number) {
        this.number = (byte) number;
    }

    /**
     * Get the byte number of a ContentType
     * @return the byte number of a ContentType
     */
    public byte getNumber() {
        return number;
    }

    /**
     * Get the ContentType of a given byte number
     * @param value the byte number
     * @return the ContentType
     */
    public static ContentType get(int value) {
        ContentType ret = ContentType.NONE;
        for (ContentType type : values()) {
            if (type.getNumber() == value) {
                ret = type;
            }
        }
        return ret;
    }
}
