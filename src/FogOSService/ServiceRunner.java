package FogOSService;

import FogOSCore.FogOSCore;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;

public class ServiceRunner implements Runnable {
    // TODO: Open the service port
    // If the service is proxied, then this should control the proxying as well.
    private FogOSCore core;
    private ArrayList<Service> services;
    private static final String TAG = "FogOSServiceRunner";
    private final int PERIOD = 1000;

    public ServiceRunner(FogOSCore core) {
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Start: Initialize ServiceRunner");
        this.core = core;
        this.services = core.getServiceList();
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Finish: Initialize ServiceRunner");
    }

    public void run() {
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Start: Run ServiceRunner");

        try {
            initService();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        while (true)
        {
            try {
                Thread.sleep(PERIOD);
                processService();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void initService() throws InvalidKeySpecException, InterruptedException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        if (services != null && services.size() > 0) {
            Iterator<Service> iterator = services.iterator();
            Service service;

            // Open a FogOSSocket for the service
            while (iterator.hasNext()) {
                service = iterator.next();
                service.initService();
            }
        }
    }

    private void processService() {

        if (services != null && services.size() > 0) {
            Iterator<Service> iterator = services.iterator();
            Service service;

            while (iterator.hasNext()) {
                service = iterator.next();
                if (service.hasInputFromPeer()) {
                    service.processInputFromPeer();
                }

                if (service.getContext().isProxy()) {
                    if (service.hasOutputToServer()) {
                        service.processOutputToServer();
                    }

                    if (service.hasInputFromServer()) {
                        service.processInputFromServer();
                    }
                }

                if (service.hasOutputToPeer())
                    service.processOutputToPeer();
            }
        }
    }
}