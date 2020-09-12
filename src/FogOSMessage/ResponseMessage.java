package FogOSMessage;

import FlexID.FlexID;
import FogOSCore.FogOSBroker;
import org.json.JSONException;

public class ResponseMessage extends Message {
    private FlexID peerID;

    public ResponseMessage() {
        super(MessageType.RESPONSE);
        peerID = null;
    }

    public ResponseMessage(FlexID peerID)
    {
        super(MessageType.RESPONSE, peerID);
        this.peerID = peerID;
    }

    public ResponseMessage(FlexID peerID, byte[] message) throws JSONException {
        super(MessageType.RESPONSE, peerID, message);
        this.peerID = peerID;
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

    public void setPeerID(FlexID peerID) {
        this.peerID = peerID;
    }

    public FlexID getPeerID() {
        return peerID;
    }
}
