package FogOSMessage;

import FlexID.FlexID;
import FogOSContent.Content;
import FogOSCore.FogOSBroker;
import FogOSService.Service;
import FogOSService.ServiceContext;
import FlexID.ServiceID;
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
    private HashMap<String, String> indexMap;
    private String registerID;

    public RegisterMessage() {
        super(MessageType.REGISTER);
        init();
    }

    public RegisterMessage(FlexID deviceID) {
        super(MessageType.REGISTER, deviceID);
        init();
    }

    public RegisterMessage(FlexID deviceID, int registerIDCounter, Content content) {
        super(MessageType.REGISTER, deviceID);
        init();
        type = "Content";
        registerID = Integer.toString(registerIDCounter);

        try {
            JSONArray registerList = new JSONArray();
            String hash = content.getHash();
            indexMap.put("0", content.getName());

            JSONObject obj = new JSONObject();
            obj.put("index", "0"); // TODO: We have to store mapping between the content and the index (for Register Ack processing)
            obj.put("hash", hash); // TODO: Need a hash of content itself; client input
            obj.put("registerType", type);
            obj.put("category", "none"); // TODO: Do we use category of the content?
            obj.put("attributes", "none"); // TODO: How can we get attributes of the content?
            obj.put("cache", false);
            obj.put("segment", false);
            obj.put("collisionAvoid", true); // TODO
            registerList.put(obj);

            this.addAttrValuePair("registerList", registerList.toString(), null);
            this.addAttrValuePair("registerID", registerID, null);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public RegisterMessage(FlexID deviceID, int registerIDCounter, ContentStore store) {
        super(MessageType.REGISTER, deviceID);
        init();
        registerID = Integer.toString(registerIDCounter);
        this.store = store;
        type = "Content";


        Content[] contentList = store.getContentList();
        try {
            JSONArray registerList = new JSONArray();
            for (int i = 0; i < contentList.length; i++) {
                String hash = Integer.toString(contentList[i].getName().hashCode());
                indexMap.put(Integer.toString(i), contentList[i].getName());

                JSONObject obj = new JSONObject();
                obj.put("index", Integer.toString(i));
                obj.put("hash", hash);
                obj.put("registerType", type);
                obj.put("category", "none"); // TODO: Need user input
                obj.put("attributes", "none"); // TODO: Need user input
                obj.put("cache", false);
                obj.put("segment", false);
                obj.put("collisionAvoid", true); // TODO
                registerList.put(obj);
            }
            this.addAttrValuePair("registerList", registerList.toString(), null);
            this.addAttrValuePair("registerID", registerID, null);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public RegisterMessage(FlexID deviceID, int registerIDCounter, Service service) {
        super(MessageType.REGISTER, deviceID);
        init();

        type = "Service";
        registerID = Integer.toString(registerIDCounter);

        try {
            JSONArray registerList = new JSONArray();

            ServiceContext serviceCtxt = service.getContext();
            ServiceID serviceID = serviceCtxt.getServiceID();

            String hash = serviceID.getStringIdentity();
            indexMap.put("0", serviceCtxt.getName());

            JSONObject obj = new JSONObject();
            obj.put("index", "0");
            obj.put("hash", hash);
            obj.put("registerType", type);
            obj.put("category", serviceCtxt.getServiceType());
            obj.put("attributes", "none"); // TODO: Need user input
            obj.put("cache", false);
            obj.put("segment", false);
            obj.put("collisionAvoid", true); // TODO
            registerList.put(obj);

            this.addAttrValuePair("registerList", registerList.toString(), null);
            this.addAttrValuePair("registerID", registerID, null);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public RegisterMessage(FlexID deviceID, int registerIDCounter, ArrayList<Service> serviceList) {
        super(MessageType.REGISTER, deviceID);
        this.type = "Service";
        init();

        for (Service service : serviceList) {

        }

    }

    public String getRegisterID() {
        return registerID;
    }

    public HashMap<String, String> getIndexMap() {
        return indexMap;
    }

    public String getType() {
        return type;
    }


    @Override
    public void init() {
        type = "none";
        indexMap = new HashMap<String, String>();
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
