package FogOSMessage;

import FlexID.FlexID;
import FogOSCore.FogOSBroker;

public class UpdateMessage extends Message {

    public UpdateMessage() {
        super(MessageType.UPDATE);
        init();
    }

    public UpdateMessage(FlexID deviceID) {
        super(MessageType.UPDATE, deviceID);
        init();
    }

    // TODO: I think we need this function (from hmlee)
    public UpdateMessage(FlexID deviceID, FlexID[] flexIDList, boolean deregisterFlag) {
        super(MessageType.UPDATE, deviceID);
        init();
    }

    @Override
    public void init() {

    }

    @Override
    public void test(FogOSBroker broker) {

    }
}
