package FogOSResource;

import FlexID.FlexID;
import FlexID.Value;
import FogOSCore.FogOSCore;
import FogOSMessage.MapUpdateMessage;
import FogOSMessage.Message;
import FogOSMessage.MessageType;
import FogOSMessage.StatusMessage;
import FogOSSecurity.SecureFlexIDSession;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;

public class ResourceReporter implements Runnable {
    private FogOSCore core;
    private static final String TAG = "FogOSResourceReporter";
    private LinkedList<Value> statusIDList;
    private final int PERIOD = 1000;

    public ResourceReporter(FogOSCore core) {
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Start: Initialize ResourceReporter");
        this.core = core;
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Finish: Initialize ResourceReporter");
    }

    public void run() {
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Start: Run ResourceReporter");

        // Please remove the following thread-related statements if we don't need to process the STATUS_ACK messages
        //StatusACKThread thread = new StatusACKThread();
        //thread.start();

        while (true)
        {
            try {
                Thread.sleep(PERIOD);
                monitorResource();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void monitorResource() {
        // Prepare Resource related variables
        ArrayList<Resource> resources = core.getResourceList();
        if (resources != null && resources.size() > 0) {
            Iterator<Resource> iterator = resources.iterator();
            Resource resource;

            // Prepare a STATUS message
            Message msg = new StatusMessage(core.getDeviceID());

            while (iterator.hasNext()) {
                resource = iterator.next();
                if (!resource.isOnDemand()) {
                    resource.monitorResource();
                    msg.addAttrValuePair(resource.getName(), resource.getCurr(), resource.getUnit());
                }
            }

            // Send the STATUS message
            msg.send(core.getBroker());
        }
    }
}
