package FogOSMessage;
import FlexID.FlexID;
import FlexID.AttrValuePairs;
import FlexID.Value;
import FogOSCore.FogOSBroker;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.Random;

public abstract class Message {
    MessageType messageType;
    FlexID deviceID;
    AttrValuePairs body;
    Random random;

    public Message(FlexID deviceID) {
        this.messageType = null;
        this.deviceID = deviceID;
        body = new AttrValuePairs();
        random = new Random();
    }

    public Message(MessageType messageType) {
        this.messageType = messageType;
        this.deviceID = null;
        this.body = new AttrValuePairs();
        random = new Random();
    }

    public Message(MessageType messageType, FlexID deviceID) {
        this.messageType = messageType;
        this.deviceID = deviceID;
        this.body = new AttrValuePairs();
        random = new Random();
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

    public String getStringFromHashTable(Hashtable hashtable) {
        JSONObject jsonObject = new JSONObject(hashtable);
        return jsonObject.toString();
    }

    public void send(FogOSBroker broker) {
        try {
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
