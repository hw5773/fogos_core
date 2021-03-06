package FogOSMessage;

import FlexID.FlexID;
import FogOSCore.FogOSBroker;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JoinMessage extends Message {
    private final String TAG = "FogOSJoin";

    public JoinMessage() {
        super(MessageType.JOIN);
        init();
    }

    public JoinMessage(FlexID deviceID) {
        super(MessageType.JOIN, deviceID);
        init();
    }

    @Override
    public void init() {

    }

    @Override
    public void test(FogOSBroker broker) {
        try {
            JSONArray uniqueCodes = new JSONArray();
            JSONObject interface1 = new JSONObject();
            JSONObject interface2 = new JSONObject();

            interface1.put("ifaceType", "wifi");
            interface1.put("hwAddress", "00:1a:e9:8d:08:73");
            interface1.put("ipv4", "143.248.30.13");

            interface2.put("ifaceType", "lte");
            interface2.put("hwAddress", "00:1a:e9:8d:08:74");
            interface2.put("ipv4", "10.0.3.15");

            uniqueCodes.put(interface1);
            uniqueCodes.put(interface2);

            JSONArray relay = new JSONArray();
            relay.put("fh2gj1g");
            relay.put("d3hsv5a35");

            JSONArray neighbors = new JSONArray();
            JSONObject neighbor1 = new JSONObject();
            JSONObject neighbor2 = new JSONObject();

            neighbor1.put("neighborIface", "wifi");
            neighbor1.put("neighborIpv4", "10.0.0.42");
            neighbor1.put("neighborFlexID", "asdf");

            neighbor2.put("neighborIface", "blue tooth");
            neighbor2.put("neighborHwAddress", "00:11:22:33:aa:bb");
            neighbor2.put("neighborFlexID", "asdf12");

            neighbors.put(neighbor1);
            neighbors.put(neighbor2);

            String pubkey = "a32adf";

            this.addAttrValuePair("uniqueCodes", uniqueCodes.toString(), null);
            this.addAttrValuePair("relay", relay.toString(), null);
            this.addAttrValuePair("neighbors", neighbors.toString(), null);
            this.addAttrValuePair("pubKey", pubkey, null);

            Logger.getLogger(TAG).log(Level.INFO, "MqttMessage: " + getStringFromHashTable(this.getAttrValueTable()));
            broker.getMqttClient().publish(this.getMessageType().getTopicWithDeviceID(deviceID), new MqttMessage(getStringFromHashTable(this.getAttrValueTable()).getBytes()));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
