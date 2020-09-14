package FogOSMessage;

import FlexID.FlexID;
import FogOSContent.Content;
import FogOSCore.FogOSBroker;
import FogOSResource.Resource;
import FogOSService.Service;
import FogOSStore.ContentStore;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegisterMessage extends Message {
    private final String TAG = "FogOSRegister";

    private ContentStore store;
    private String type;
    private HashMap<String, Integer> indexMap;

    public RegisterMessage() {
        super(MessageType.REGISTER);
        init();
    }

    public RegisterMessage(FlexID deviceID) {
        super(MessageType.REGISTER, deviceID);
        init();
    }

    public RegisterMessage(FlexID deviceID, Content content) {
        super(MessageType.REGISTER, deviceID);
        this.type = "Content";
        init();
    }

    public RegisterMessage(FlexID deviceID, int registerIDCounter, ContentStore store) {
        super(MessageType.REGISTER, deviceID);

        this.store = store;
        this.type = "Content";
        init();

        Content[] contentList = store.getContentList();
        try {
            JSONArray registerList = new JSONArray();
            for (int i = 0; i < contentList.length; i++) {
                String hash = Integer.toString(contentList[i].getName().hashCode());

                JSONObject obj = new JSONObject();
                obj.put("index", Integer.toString(i)); // TODO: We have to store mapping between the content and the index (for Register Ack processing)
                obj.put("hash", hash); // TODO: Need a hash of content itself; client input
                obj.put("registerType", "Content");
                obj.put("category", "none"); // TODO: Do we use category of the content?
                obj.put("attributes", "none"); // TODO: How can we get attributes of the content?
                obj.put("cache", false); // TODO: Do we use following 3 bits?
                obj.put("segment", false); // TODO
                obj.put("collisionAvoid", false); // TODO
                registerList.put(obj);
                indexMap.put(hash, i);
            }
            String registerID = Integer.toString(registerIDCounter);
            this.addAttrValuePair("registerList", registerList.toString(), null);
            this.addAttrValuePair("registerID", registerID, null);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public RegisterMessage(FlexID deviceID, Service service) {
        super(MessageType.REGISTER, deviceID);
        this.type = "Service";
        init();
    }

    public RegisterMessage(FlexID deviceID, ArrayList<Service> serviceList) {
        super(MessageType.REGISTER, deviceID);
        this.type = "Service";
        init();

        for (Service service : serviceList) {

        }

    }

    public HashMap<String, Integer> getIndexMap() {
        return indexMap;
    }

    public String getType() {
        return type;
    }

    @Override
    public void init() {
        type = "none";
        indexMap = new HashMap<String, Integer>();
    }

    @Override
    public void send(FogOSBroker broker) {
        try {
            Logger.getLogger(TAG).log(Level.INFO, "MqttMessage: " + getStringFromHashTable(this.getAttrValueTable()));
            broker.getMqttClient().publish(this.getMessageType().getTopicWithDeviceID(deviceID), new MqttMessage(getStringFromHashTable(this.getAttrValueTable()).getBytes()));
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void test(FogOSBroker broker) {
        File[] files = store.getFileList();
        JSONArray registerList = new JSONArray();
        try {
            JSONObject tmp = new JSONObject();
            for (int i = 0; i < files.length; i++){
                //JSONObject tmp = new JSONObject();
                tmp.put("index", i);
                tmp.put("hash", files[i].getName().hashCode());
                tmp.put("registerType", "Content");
                tmp.put("category", "Video");
            }
            registerList.put(tmp);

            //JSONArray relay = new JSONArray();
            //relay.put("fh2gj1g");
            //relay.put("d3hsv5a35");

            String registerID= "0";
            this.addAttrValuePair("registerList", registerList.toString(), null);
            //this.addAttrValuePair("relay", relay.toString(), null);
            this.addAttrValuePair("registerID", registerID, null);
            broker.getMqttClient().publish(this.getMessageType().getTopicWithDeviceID(deviceID), new MqttMessage(getStringFromHashTable(this.getAttrValueTable()).getBytes()));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
