package FogOSMessage;

import FlexID.FlexID;
import FogOSCore.FogOSBroker;
import FogOSService.Service;
import FogOSStore.ContentStore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

public class RegisterAckMessage extends Message {
    private final String TAG = "FogOSRegisterAck";
    private String registerID;
    private HashMap<String, String> idMap;

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
        String msg = new String(message);
        this.addAttrValuePair("answer", msg, null);
    }
    @Override
    public void init() {

    }

    public String getRegisterID() {
        return registerID;
    }

    public HashMap<String, String> getIdMap() {
        return idMap;
    }

    // TODO: Implement processing the received message with AVPs (this.body)
    public MessageError process() {
        idMap = new HashMap<String, String>();

        try {
            String answerStr = this.body.getValueByAttr("answer").getValue();
            JSONObject answerObj = new JSONObject(answerStr);
            int error = answerObj.getInt("error");
            if (error != 0) {
                return MessageError.PROCESS_ERROR;
            }

            registerID = answerObj.getString("registerID");
            JSONArray idList = answerObj.getJSONArray("idList");
            for (int i =0; i < idList.length(); i++)  {
                JSONObject idObj = idList.getJSONObject(i);
                String idx = Integer.toString(i);
                String id = idObj.getString(idx);
                idMap.put(idx, id);
            }

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
