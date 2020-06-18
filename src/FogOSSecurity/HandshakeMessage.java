package FogOSSecurity;

/**
 *  Implements an API for handshake message handling
 *  @author Hyeonmin Lee
 */
public class HandshakeMessage extends SecurityMessage {

    /**
     * Construct the ApplicationMessage
     * @param message the byte of a message
     */
    HandshakeMessage(byte[] message) {
        super(message);
        contentType = ContentType.HANDSHAKE;
    }

    @Override
    public byte[] serialize() {
        return new byte[0];
    }

    @Override
    public void deserialize(byte[] message) {

    }
}
