package FogOSMessage;
import FlexID.FlexID;
import FlexID.AttrValuePairs;
import FlexID.Value;
import FogOSCore.FogOSBroker;
import jdk.nashorn.internal.parser.JSONParser;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Message {
    private final String TAG = "FogOSMessage";
    MessageType messageType;
    FlexID deviceID;
    AttrValuePairs body;
    Random random;

    public Message(FlexID deviceID) {
        initFields(messageType, deviceID);
    }

    public Message(MessageType messageType) {
        initFields(messageType, deviceID);
    }

    public Message(MessageType messageType, FlexID deviceID) {
        initFields(messageType, deviceID);
    }

    public Message(MessageType messageType, FlexID deviceID, byte[] message) throws JSONException {
        initFields(messageType, deviceID);
        JSONObject payload = new JSONObject(new String(message));
        Iterator<String> iterator = payload.keys();

        while (iterator.hasNext()) {
            String key = iterator.next();
            this.body.addAttrValuePair(key, new Value(payload.get(key).toString(), ""));
        }
    }

    private void initFields(MessageType messageType, FlexID deviceID) {
        this.messageType = messageType;
        this.deviceID = deviceID;
        this.body = new AttrValuePairs();
        this.random = new Random();
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public FlexID getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(FlexID deviceID) {
        this.deviceID = deviceID;
    }

    public void addAttrValuePair(String attr, String value, String unit) {
        Value val = new Value(value, unit);
        this.body.addAttrValuePair(attr, val);
    }

    public Value getValueByAttr(String attr) {
        return this.body.getValueByAttr(attr);
    }

    public Hashtable getAttrValueTable(){ return this.body.getTable(); }

    public String getStringFromHashTable(Hashtable<String, Value> hashtable) {
        JSONObject jsonObject = new JSONObject();
        Iterator<String> itr = hashtable.keySet().iterator();
        String ret;
        String key;
        Value val;

        while (itr.hasNext()) {
            key = itr.next();
            val = hashtable.get(key);
            try {
                jsonObject.put(key, val.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (jsonObject.length() == 0)
            ret = "";
        else
            ret = jsonObject.toString();

        Logger.getLogger(TAG).log(Level.INFO, "Result: " + ret);
        return ret;
    }

    public void send(FogOSBroker broker) {
        try {
            if (broker != null)
                broker.getMqttClient().publish(this.getMessageType().getTopic(), new MqttMessage(getStringFromHashTable(this.getAttrValueTable()).getBytes()));
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // TODO: Initialization function
    public abstract void init();

    // TODO: Test Value
    public abstract void test(FogOSBroker broker);
}
