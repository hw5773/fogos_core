package FogOSClient;

import FlexID.FlexID;
import FogOSCore.FogOSCore;
import FogOSMessage.*;
import FogOSSecurity.Role;
import FogOSSecurity.SecureFlexIDSession;

import java.util.logging.Level;

public class FogOSClient implements FogOSClientAPI {
    private FogOSCore core;
    private static final String TAG = "FogOSClient";

    public FogOSClient() {
        core = new FogOSCore();
    }

    public FogOSClient(String path) {
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Start: Initialize FogOSClient");
        core = new FogOSCore(path);
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Finish: Initialize FogOSClient");
    }

    public QueryMessage makeQueryMessage() {
        return (QueryMessage) core.generateMessage(MessageType.QUERY);
    }

    public QueryMessage makeQueryMessage(String query) {
        QueryMessage queryMessage = (QueryMessage) core.generateMessage(MessageType.QUERY);
        queryMessage.addAttrValuePair("keywords", query, null);
        return queryMessage;
    }

    // TODO: Currently, this function returns the test values
    public void testQueryMessage(QueryMessage queryMessage) {
        core.testMessage(queryMessage);
    }

    public void sendQueryMessage(QueryMessage queryMessage) {
        core.sendMessage(queryMessage);
    }

    public ReplyMessage getReplyMessage() {
        return (ReplyMessage) core.getReceivedMessage(MessageType.REPLY.getTopic());
    }

    public RequestMessage makeRequestMessage() {
        RequestMessage requestMessage = (RequestMessage) core.generateMessage(MessageType.REQUEST);
        return requestMessage;
    }

    public RequestMessage makeRequestMessage(FlexID id) {
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Start: makeRequestMessage()");
        RequestMessage requestMessage = (RequestMessage) core.generateMessage(MessageType.REQUEST);
        requestMessage.setPeerID(id);
        requestMessage.addAttrValuePair("id", id.getStringIdentity(), null);
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Finish: makeRequestMessage()");
        return requestMessage;
    }

    public void testRequestMessage(RequestMessage requestMessage) {
        core.testMessage(requestMessage);
    }

    public void sendRequestMessage(RequestMessage requestMessage) {
        core.sendMessage(requestMessage);
    }

    public ResponseMessage getResponseMessage() {
        return (ResponseMessage) core.getReceivedMessage(MessageType.RESPONSE.getTopic());
    }

    public SecureFlexIDSession createSecureFlexIDSession(Role role, FlexID sFID, FlexID dFID)
    {
        return core.createSecureFlexIDSession(role, sFID, dFID);
    }
}
