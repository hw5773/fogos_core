package FogOSMessage;

import FlexID.FlexID;
import FogOSCore.FogOSBroker;
import org.json.JSONException;

public class LeaveAckMessage extends Message {

    public LeaveAckMessage() {
        super(MessageType.LEAVE_ACK);
        init();
    }

    public LeaveAckMessage(FlexID deviceID) {
        super(MessageType.LEAVE_ACK, deviceID);
        init();
    }

    public LeaveAckMessage(FlexID deviceID, byte[] message) throws JSONException {
        super(MessageType.LEAVE_ACK, deviceID, message);
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
