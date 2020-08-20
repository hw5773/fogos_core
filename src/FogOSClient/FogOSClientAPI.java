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

    // Setting Content/Service
    //void addContent(Content content);
    //void addService(Service service);

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
