package FogOSMessage;

import FlexID.FlexID;
import FogOSCore.FogOSBroker;

public class LeaveMessage extends Message {

    public LeaveMessage() {
        super(MessageType.LEAVE);
        init();
    }

    public LeaveMessage(FlexID deviceID) {
        super(MessageType.LEAVE, deviceID);
        init();
    }

    public LeaveMessage(FlexID deviceID, FlexID flexID) {
        super(MessageType.LEAVE, deviceID);
        init();
    }

    @Override
    public void init() {

    }

    @Override
    public void test(FogOSBroker broker) {

    }
}
