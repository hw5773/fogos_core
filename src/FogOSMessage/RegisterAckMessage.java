package FogOSMessage;

import FlexID.FlexID;
import FogOSCore.FogOSBroker;
import org.json.JSONException;

public class RegisterAckMessage extends Message {

    public RegisterAckMessage() {
        super(MessageType.REGISTER_ACK);
        init();
    }

    public RegisterAckMessage(FlexID deviceID) {
        super(MessageType.REGISTER_ACK, deviceID);
        init();
    }

    public RegisterAckMessage(FlexID deviceID, byte[] message) throws JSONException {
        super(MessageType.REGISTER_ACK, deviceID, message);
        init();
    }
    @Override
    public void init() {

    }

    // TODO: Implement processing the received message with AVPs (this.body)
    public MessageError process() {
        return MessageError.NONE;
    }

    @Override
    public void test(FogOSBroker broker) {

    }
}
