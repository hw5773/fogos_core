package FogOSMessage;

import FlexID.FlexID;
import FogOSCore.FogOSBroker;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ReplyMessage extends Message {
    private ArrayList<ReplyEntry> replyList;
    private String queryID;
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

        String msg = new String(message);
        System.out.println(msg);
        this.addAttrValuePair("answer", msg, null);
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
        try {
            String answerStr = this.body.getValueByAttr("answer").getValue();
            JSONObject answerObj = new JSONObject(answerStr);
            int error = answerObj.getInt("error");
            if (error != 0) {
                return MessageError.PROCESS_ERROR;
            }

            queryID = answerObj.getString("queryID");
            JSONArray ids = answerObj.getJSONArray("ids");
            for (int i =0; i < ids.length(); i++)  {
                JSONObject idObj = ids.getJSONObject(i);
                String id = idObj.getString("id");
                FlexID flexID = new FlexID(id);
                String title = idObj.getString("title");
                String desc = idObj.getString("desc");
                addReplyEntry(title, desc, flexID);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return MessageError.PARSE_ERROR;
        }

        return MessageError.NONE;
    }

    public ArrayList<ReplyEntry> getReplyList() {
        return replyList;
    }
}
