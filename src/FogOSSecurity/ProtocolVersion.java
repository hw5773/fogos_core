package FogOSSecurity;

/**
 *  Implements an API for a protocol version
 *  @author Hyeonmin Lee
 */
public enum ProtocolVersion {
    /**
     * Define version 1
     */
    VERSION1((byte)1);

    private final byte version;

    /**
     * Construct the ProtocolVersion
     * @param version the byte number of a ProtocolVersion
     */
    ProtocolVersion(byte version) {
        this.version = version;
    }

    /**
     * Get the byte number of a ProtocolVersion
     * @return the byte number of a ProtocolVersion
     */
    public byte getProtocolVersion() {
        return version;
    }

    /**
     * Get the ProtoclVersion of a given byte number
     * @param value the byte number
     * @return the ProtocolVersion
     */
    public static ProtocolVersion get(int value) {
        ProtocolVersion ret = ProtocolVersion.VERSION1;
        for (ProtocolVersion type : values()) {
            if (type.getProtocolVersion() == value) {
                ret = type;
            }
        }
        return ret;
    }
}
