package FogOSMessage;

import FlexID.FlexID;
import FogOSCore.FogOSBroker;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QueryMessage extends Message {
    private final String TAG = "FogOSQuery";
    JSONArray requirements;

    public QueryMessage() {
        super(MessageType.QUERY);
        init();
    }

    public QueryMessage(FlexID deviceID) {
        super(MessageType.QUERY, deviceID);
        init();
    }

    public QueryMessage(FlexID deviceID, int queryIDCounter, String queryType, String queryCategory, String order, boolean desc, int limit) {
        super(MessageType.QUERY, deviceID);
        init();

        this.addAttrValuePair("queryID", Integer.toString(queryIDCounter), null);
        this.addAttrValuePair("queryType", queryType,null);
        this.addAttrValuePair("queryCategory", queryCategory, null);
        this.addAttrValuePair("order", order, null);
        this.addAttrValuePair("desc", Boolean.toString(desc), null);
        this.addAttrValuePair("limit", Integer.toString(limit), null);
    }

    public void setAttribute(String attributeType, String value, String unit, String operator) {
        try {
            JSONObject tmpObj = new JSONObject();
            tmpObj.put("attributeType", attributeType);
            tmpObj.put("value", value);
            tmpObj.put("unit", unit);
            tmpObj.put("operator", operator);
            requirements.put(tmpObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() {
        requirements = new JSONArray();
    }

    public void send(FogOSBroker broker) {
        try {
            this.addAttrValuePair("requirements", requirements.toString(), null);

            Logger.getLogger(TAG).log(Level.INFO, "MqttMessage: " + getStringFromHashTable(this.getAttrValueTable()));
            broker.getMqttClient().publish(this.getMessageType().getTopicWithDeviceID(deviceID), new MqttMessage(getStringFromHashTable(this.getAttrValueTable()).getBytes()));
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void test(FogOSBroker broker) {

    }
}
