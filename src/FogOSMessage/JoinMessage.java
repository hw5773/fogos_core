package FogOSMessage;

import FlexID.FlexID;
import FogOSCore.FogOSBroker;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.logging.Level;

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
            JSONArray uniqueCodes = new JSONArray("[{\"tmpDeviceID\":\"0x4A8FC943011CBAD86228\",\"ifaceType\":\"wifi\",\"hwAddress\":\"00-1a-e9-8d-08-73\",\"ipv4\":\"143.248.30.13\"},{ \"ifaceType\":\"lte\",\"hwAddress\":\"00:1a:e9:8d:08:74\",\"ipv4\":\"10.0.3.15\"}]");
            JSONArray relay = new JSONArray("[\"fh2gj1g\", \"d3hsv5a35\"]");
            JSONArray neighbors = new JSONArray("[{\"neighborIface\":\"wifi\", \"neighborIpv4\":\"10.0.0.42\", \"neighborFlexID\":\"asdf\"}, {\"neighborIface\":\"blue tooth\", \"neighborHwAddress\":\"00:11:22:33:aa:bb\", \"neighborFlexID\":\"asdf12\"}]");
            String pubkey= "a32adf";
            this.addAttrValuePair("uniqueCodes", uniqueCodes.toString(), null);
            this.addAttrValuePair("relay", relay.toString(), null);
            this.addAttrValuePair("neighbors", neighbors.toString(), null);
            this.addAttrValuePair("pubKey", pubkey, null);

            broker.getMqttClient().publish(this.getMessageType().getTopic(), new MqttMessage(getStringFromHashTable(this.getAttrValueTable()).getBytes()));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
