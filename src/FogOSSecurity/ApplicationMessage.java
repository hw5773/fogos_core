package FogOSSecurity;

/**
 *  Implements an API for application message handling
 *  @author Hyeonmin Lee
 */
public class ApplicationMessage extends SecurityMessage {

    /**
     * Construct the ApplicationMessage
     * @param message the byte of a message
     */
    ApplicationMessage(byte[] message) {
        super(message);
        contentType = ContentType.APPLICATION;
    }

    @Override
    public byte[] serialize() {
        return new byte[0];
    }

    @Override
    public void deserialize(byte[] message) {

    }
}
