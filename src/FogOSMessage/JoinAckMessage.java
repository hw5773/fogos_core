package FogOSMessage;

import FlexID.FlexID;
import FlexID.Value;
import FogOSCore.FogOSBroker;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class JoinAckMessage extends Message {

    public JoinAckMessage() {
        super(MessageType.JOIN_ACK);
        init();
    }

    public JoinAckMessage(FlexID deviceID) {
        super(MessageType.JOIN_ACK, deviceID);
        init();
    }

    public JoinAckMessage(FlexID deviceID, byte[] message) throws JSONException {
        super(MessageType.JOIN_ACK, deviceID, message);
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
