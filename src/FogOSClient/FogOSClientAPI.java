package FogOSClient;

import FlexID.FlexID;
import FogOSContent.Content;
import FogOSMessage.QueryMessage;
import FogOSMessage.ReplyMessage;
import FogOSMessage.RequestMessage;
import FogOSMessage.ResponseMessage;
import FogOSResource.Resource;
import FogOSService.Service;

import java.util.ArrayList;

public interface FogOSClientAPI {

    // Starting/quitting the FogOS core
    void begin();
    void exit() throws InterruptedException;

    // Setting Content/Service
    void addContent(Content content);
    void addService(Service service);
    void addResource(Resource resource);

    //void removeContent(FlexID flexID);
    void removeContent(String name);
    void removeService(FlexID flexID);
    void removeService(String name);

    Content[] getContentList();
    ArrayList<Service> getServiceList();
    ArrayList<Resource> getResourceList();

    // QueryMessage-related
    QueryMessage makeQueryMessage();
    QueryMessage makeQueryMessage(String keywords);
    void sendQueryMessage(QueryMessage queryMessage);

    // ReplyMessage-related
    ReplyMessage getReplyMessage();

    // RequestMessage-related
    RequestMessage makeRequestMessage();
    RequestMessage makeRequestMessage(FlexID id);
    void sendRequestMessage(RequestMessage requestMessage);

    // ResponseMessage-related
    ResponseMessage getResponseMessage();
}