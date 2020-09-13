package FogOSMessage;

import FlexID.FlexID;
import FlexID.Value;
import FogOSCore.FogOSBroker;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Base64.Decoder;
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
        String msg = new String(message);
        this.addAttrValuePair("answer", msg, null);
    }

    @Override
    public void init() {

    }

    // TODO: Implement processing the received message with AVPs (this.body)
    public MessageError process() {

        try {
            String answerStr = this.body.getValueByAttr("answer").getValue();
            JSONObject answerObj = new JSONObject(answerStr);
            String newIDStr = answerObj.getString("id");
            int error = answerObj.getInt("error");

            if (error != 0) {
                return MessageError.PROCESS_ERROR;
            }

            // Save newID
            Decoder decoder = Base64.getDecoder();
            byte[] newIdentity = decoder.decode(newIDStr);
            FlexID flexID = this.getDeviceID();
            flexID.setIdentity(newIdentity);
            this.setDeviceID(flexID);

        } catch (JSONException e) {
            e.printStackTrace();
            return MessageError.PARSE_ERROR;
        }

        return MessageError.NONE;
    }

    @Override
    public void test(FogOSBroker broker) {

    }
}
