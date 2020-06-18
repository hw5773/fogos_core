package FogOSSecurity;

/**
 *  Implements an API for records
 *  @author Hyeonmin Lee
 */
public class Record {
    private ProtocolVersion protocolVersion; // 1 byte
    private ContentType contentType; // 1 byte
    private SecurityMessage securityMessage;

    /**
     * Construct Record for writing messages
     * @param protocolVersion the protocol version
     * @param securityMessage the message that wants to write
     */
    // Constructor for writing messages
    public Record(ProtocolVersion protocolVersion, SecurityMessage securityMessage) {
        this.protocolVersion = protocolVersion;
        this.contentType = securityMessage.getContentType();
        this.securityMessage = securityMessage;
    }

    /**
     * Construct Record for reading messages
     * @param protocolVersion the protocol version
     * @param message the message that wants to read
     */
    // Constructor for reading messages
    public Record(ProtocolVersion protocolVersion, byte[] message) {
        this.protocolVersion = protocolVersion;
        deserialize(message);
    }

    /**
     *  Serialize the record
     * @return the serialized record
     */
    public byte[] serialize() {
        int offset = 0;
        byte[] msg = securityMessage.serialize();
        int length = msg.length;
        byte [] ret = new byte[length];

        ret[offset++] = protocolVersion.getProtocolVersion();
        ret[offset++] = contentType.getNumber();
        ret[offset++] = (byte) ((length >> 8) & 0xff);
        ret[offset++] = (byte) (length & 0xff);

        System.arraycopy(msg, 0, ret, offset, length);

        return ret;
    }

    /**
     * Deserialize the record
     * @param message the message
     */
    public void deserialize(byte[] message) {
        int offset = 0;
        int length;
        byte[] securityMessage;
        this.protocolVersion = ProtocolVersion.get(message[offset++]);
        this.contentType = ContentType.get(message[offset++]);
        length = message[offset++] << 8 | message[offset++];
        securityMessage = new byte[length];
        System.arraycopy(message, offset, securityMessage, 0, length);

        if (message.length - 4 != length) {
            // TODO: Error
        }

        switch (this.contentType) {
            case APPLICATION:
                this.securityMessage = new ApplicationMessage(securityMessage);
                break;
            case HANDSHAKE:
                this.securityMessage = new HandshakeMessage(securityMessage);
                break;
            default:
                // TODO: Error
                System.out.println("Error");
        }
    }
}
