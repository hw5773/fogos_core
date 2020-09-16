package FogOSService;

import FlexID.FlexID;
import FlexID.InterfaceType;
import FogOSSecurity.Role;
import FogOSSecurity.SecureFlexIDSession;
import FogOSSocket.FlexIDSession;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.Future;

public abstract class Service {
    private ServiceContext context;         // The user-defined context of the service
    private SecureFlexIDSession session;    // The secure FlexID session with the client
    private AsynchronousSocketChannel proxySession; // The session with the server

    public Service(ServiceContext context) {
        this.context = context;
    }

    public boolean hasInputFromPeer() {
        return false;
    }

    public boolean hasInputFromProxy() {
        return false;
    }

    public boolean hasOutputToPeer() {
        return false;
    }

    public boolean hasOutputToProxy() {
        return false;
    }

    // Initialize the service (e.g., open a socket)
    public void initService() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, InterruptedException {
        System.out.println("[FogOSService] Start: initService()");
        boolean connected;
        session = new SecureFlexIDSession(Role.RESPONDER, context.getServiceID());

        if (context.isProxy()) {
            System.out.println("[FogOSService] Proxy: processInputFromProxy()");
            InetSocketAddress proxyAddr;

            proxyAddr = new InetSocketAddress(context.getProxyLoc().getAddr(),
                        context.getProxyLoc().getPort());

            try {
                proxySession = AsynchronousSocketChannel.open();
                Future<Void> future = proxySession.connect(proxyAddr);

                // TODO: Need to implement all
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("[FogOSService] Finish: processInputFromProxy()");
    }

    // Processing of the service regarding service requests from a client
    public abstract void processInputFromPeer();
    public abstract void processOutputToPeer();

    // Processing of the service regarding proxying
    // This should be overridden when the application is a proxy
    public void processInputFromProxy() {
        System.out.println("[FogOSService] Start: processInputFromProxy()");
        if (context.isProxy()) {
            System.out.println("[FogOSService] Proxy: processInputFromProxy()");
        }
        System.out.println("[FogOSService] Finish: processInputFromProxy()");
    }

    public void processOutputToProxy() {
        System.out.println("[FogOSService] Start: processOutputToProxy()");
        if (context.isProxy()) {
            System.out.println("[FogOSService] Proxy: processOutputToProxy()");
        }
        System.out.println("[FogOSService] Finish: processOutputToProxy()");
    }

    public ServiceContext getContext() {
        return context;
    }

    public void setContext(ServiceContext context) { this.context = context; }
}
