package FogOSMessage;

import FlexID.FlexID;
import FogOSCore.FogOSBroker;
import org.json.JSONException;

public class MapUpdateAckMessage extends Message {

    public MapUpdateAckMessage() {
        super(MessageType.MAP_UPDATE_ACK);
        init();
    }

    public MapUpdateAckMessage(FlexID deviceID) {
        super(MessageType.MAP_UPDATE_ACK, deviceID);
        init();
    }

    public MapUpdateAckMessage(FlexID deviceID, byte[] message) throws JSONException {
        super(MessageType.MAP_UPDATE_ACK, deviceID, message);
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
