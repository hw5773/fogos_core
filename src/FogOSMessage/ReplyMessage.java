package FogOSMessage;

import FlexID.FlexID;
import FogOSCore.FogOSBroker;
import org.json.JSONException;

import java.util.ArrayList;

public class ReplyMessage extends Message {
    private ArrayList<ReplyEntry> replyList;
    private static final String TAG = "FogOSMessage";

    public ReplyMessage() {
        super(MessageType.REPLY);
        init();
    }

    public ReplyMessage(FlexID deviceID) {
        super(MessageType.REPLY, deviceID);
        init();
    }

    public ReplyMessage(FlexID deviceID, byte[] message) throws JSONException {
        super(MessageType.REPLY, deviceID, message);
        init();
    }

    @Override
    public void init() {
        replyList = new ArrayList<ReplyEntry>();
    }

    @Override
    public void test(FogOSBroker broker) {

    }

    public void addReplyEntry(String title, String desc, FlexID flexID) {
        ReplyEntry entry = new ReplyEntry(title, desc, flexID);
        this.replyList.add(entry);
    }

    // TODO: Implement processing the received message with AVPs (this.body)
    public MessageError process() {
        return MessageError.NONE;
    }

    public ArrayList<ReplyEntry> getReplyList() {
        return replyList;
    }
}
