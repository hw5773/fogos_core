package FogOSClient;

import FlexID.FlexID;
import FogOSCore.FogOSCore;
import FogOSMessage.*;
import FogOSResource.Resource;
import FogOSSecurity.Role;
import FogOSSecurity.SecureFlexIDSession;
import FogOSContent.*;
import FogOSService.Service;
import FogOSStore.ContentStore;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.logging.Level;


public class FogOSClient implements FogOSClientAPI {
    private FogOSCore core;
    private ContentStore contentStore;
    private ArrayList<Service> serviceList;
    private ArrayList<Resource> resourceList;
    private String rootPath;
    private static final String TAG = "FogOSClient";

    public FogOSClient() {
        this.rootPath = "";
        init();
        //core = new FogOSCore();
    }

    public FogOSClient(String path) {
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Start: Initialize FogOSClient");
        this.rootPath = path;
        init();
        //core = new FogOSCore(path);
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Finish: Initialize FogOSClient");
    }

    private void init() {
        this.contentStore = new ContentStore(rootPath);
        this.serviceList = new ArrayList<Service>();
        this.resourceList = new ArrayList<Resource>();
    }

    // Added for the settings
    public void begin() {
        core = new FogOSCore(contentStore, serviceList, resourceList);
    }
    public void exit() throws InterruptedException {
        this.core.finalization();
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
    public void proxyQueryMessage(QueryMessage queryMessage) throws IOException, NoSuchAlgorithmException {
        core.proxyMessage(queryMessage);
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
    public void proxyRequestMessage(RequestMessage requestMessage) throws IOException, NoSuchAlgorithmException { core.proxyMessage(requestMessage); }

    public void sendRequestMessage(RequestMessage requestMessage) {
        core.sendMessage(requestMessage);
    }

    public ResponseMessage getResponseMessage() {
        return (ResponseMessage) core.getReceivedMessage(MessageType.RESPONSE.getTopic());
    }

    public SecureFlexIDSession createSecureFlexIDSession(Role role, FlexID sFID, FlexID dFID) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        return core.createSecureFlexIDSession(role, sFID, dFID);
    }

    public Content[] getContentList() {
        return core.getContentList();
    }
    public ArrayList<Service> getServiceList() { return core.getServiceList(); }
    public ArrayList<Resource> getResourceList() { return core.getResourceList(); }

    // TODO: (hmlee or syseok) Please complete this function.
    public void addContent(Content content) {
        contentStore.add(content);
    }

    public void registerContent(String name, String path) {
        core.registerContent(name, path);
    }

    /*
    public void removeContent(FlexID flexID) {
        FlexID[] flexIDList = {flexID};
        core.deregister(flexIDList);
    }
     */

    public void removeContent(String name) {
        Content content = contentStore.get(name);
        core.deregister(content.getFlexID());
        contentStore.remove(name);
    }

    public void addService(Service service) {
        serviceList.add(service);
    }

    public void removeService(FlexID flexID) {
        FlexID[] flexIDList = {flexID};
        core.deregister(flexIDList);
    }

    public void removeService(String name) {
        for (Service service : serviceList) {
            String contentName = service.getContext().getName();
            if (name.equals(contentName)) {
                FlexID[] flexIDList = {service.getContext().getServiceID()};
                core.deregister(flexIDList);
            }
            break;
        }
    }

    public void addResource(Resource resource) {
        resourceList.add(resource);
    }

    /* hmlee - resource does not use FlexID
    public void removeResource(FlexID flexID) {
        FlexID[] flexIDList = {flexID};
        core.deregister(flexIDList);
    }
    */

    // How can i remove resource?
    public void removeResource(String name) {
        for (Resource resource : resourceList) {
            String contentName = resource.getName();
            if (name.equals(contentName)) {
                core.leave(resource.getFlexID());
            }
            break;
        }
    }

    public void ContentUpdate() {
    	core.ContentUpdate();
    }
}
