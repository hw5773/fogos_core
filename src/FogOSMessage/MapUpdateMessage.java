package FogOSMessage;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import FlexID.FlexID;
import FogOSCore.FogOSBroker;

public class MapUpdateMessage extends Message {
	private final String TAG = "FogOSMapUpdate";
	
    public MapUpdateMessage() {
        super(MessageType.MAP_UPDATE);
        init();
        this.addAttrValuePair("mapUpdateID", Integer.toString(random.nextInt() & Integer.MAX_VALUE), null);
        this.addAttrValuePair("RequestID", Integer.toString(random.nextInt()  & Integer.MAX_VALUE), null);
    }

    public MapUpdateMessage(FlexID deviceID) {
        super(MessageType.MAP_UPDATE, deviceID);
        init();
        this.addAttrValuePair("mapUpdateID", Integer.toString(random.nextInt()  & Integer.MAX_VALUE), null);
        this.addAttrValuePair("RequestID", Integer.toString(random.nextInt()  & Integer.MAX_VALUE), null);
        this.addAttrValuePair("ID", deviceID.getStringIdentity(), null);

    }
    
    public MapUpdateMessage(FlexID deviceID,String LocatorType,String CurrLocate, String NextLocator) {
        super(MessageType.MAP_UPDATE, deviceID);
        init();
        this.addAttrValuePair("mapUpdateID", Integer.toString(random.nextInt()  & Integer.MAX_VALUE), null);
        this.addAttrValuePair("RequestID", Integer.toString(random.nextInt()  & Integer.MAX_VALUE), null);
        this.addAttrValuePair("ID", deviceID.getStringIdentity(), null);

        //this.addAttrValuePair("LocatorType", LocatorType, null);
        //this.addAttrValuePair("PrevLocator", CurrLocate, null);
        //this.addAttrValuePair("NextLocator",NextLocator, null);
        //this.addAttrValuePair("PublicKey", getDeviceID().getPub().toString(), null);
        //this.addAttrValuePair("ID", getDeviceID().getStringIdentity(), null);
    }

    @Override
    public void init() {

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

    }
}
