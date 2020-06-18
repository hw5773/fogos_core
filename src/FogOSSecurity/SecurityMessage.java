package FogOSSecurity;

/**
 *  Implements an API for security message handling
 *  @author Hyeonmin Lee
 */
public abstract class SecurityMessage {
    protected ContentType contentType;

    /**
     * Construct the ApplicationMessage
     * @param message the byte of a message
     */
    SecurityMessage(byte[] message) {
        deserialize(message);
    }

    /**
     * Serialize the message
     * @return the serialized message (byte)
     */
    public abstract byte[] serialize();

    /**
     * Deserialize the message
     * @param message the message
     */
    public abstract void deserialize(byte [] message);

    /**
     * Get the ContentType of the message
     * @return the ContentType
     */
    public ContentType getContentType() {
        return contentType;
    }
}
