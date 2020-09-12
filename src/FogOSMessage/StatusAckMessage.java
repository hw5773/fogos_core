package FogOSMessage;

import FlexID.FlexID;
import FogOSCore.FogOSBroker;
import org.json.JSONException;

public class StatusAckMessage extends Message {

    public StatusAckMessage() {
        super(MessageType.STATUS_ACK);
        init();
    }

    public StatusAckMessage(FlexID deviceID) {
        super(MessageType.STATUS_ACK, deviceID);
        init();
    }

    public StatusAckMessage(FlexID deviceID, byte[] message) throws JSONException {
        super(MessageType.STATUS_ACK, deviceID, message);
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
