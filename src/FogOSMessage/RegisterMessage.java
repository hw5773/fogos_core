package FogOSMessage;

import FlexID.FlexID;
import FogOSCore.FogOSBroker;
import FogOSStore.ContentStore;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class RegisterMessage extends Message {

    private ContentStore store;

    public RegisterMessage() {
        super(MessageType.REGISTER);
        init();
    }

    public RegisterMessage(FlexID deviceID) {
        super(MessageType.REGISTER, deviceID);
        init();
    }

    public RegisterMessage(FlexID deviceID, ContentStore store) {
        super(MessageType.REGISTER, deviceID);
        this.store = store;
        init();
    }

    @Override
    public void init() {

    }

    @Override
    public void test(FogOSBroker broker) {
        File[] files = store.getFileList();
        JSONArray registerList = new JSONArray();
        try {
            for (int i = 0; i < files.length; i++){
                JSONObject tmp = new JSONObject();
                tmp.put("index", i);
                tmp.put("hash", files[i].getName().hashCode());
                tmp.put("registerType", "Content");
                tmp.put("category", "Video");
                registerList.put(tmp);
            }
            JSONArray relay = new JSONArray();
            relay.put("fh2gj1g");
            relay.put("d3hsv5a35");

            String registerID= "0";
            this.addAttrValuePair("registerList", registerList.toString(), null);
            this.addAttrValuePair("relay", relay.toString(), null);
            this.addAttrValuePair("registerID", registerID, null);
            broker.getMqttClient().publish(this.getMessageType().getTopic(), new MqttMessage(getStringFromHashTable(this.getAttrValueTable()).getBytes()));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
