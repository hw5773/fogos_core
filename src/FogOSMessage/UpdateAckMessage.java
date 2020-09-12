package FogOSMessage;

import FlexID.FlexID;
import FogOSCore.FogOSBroker;
import org.json.JSONException;

public class UpdateAckMessage extends Message {

    public UpdateAckMessage() {
        super(MessageType.UPDATE_ACK);
        init();
    }

    public UpdateAckMessage(FlexID deviceID) {
        super(MessageType.UPDATE_ACK, deviceID);
        init();
    }

    public UpdateAckMessage(FlexID deviceID, byte[] message) throws JSONException {
        super(MessageType.UPDATE_ACK, deviceID, message);
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
