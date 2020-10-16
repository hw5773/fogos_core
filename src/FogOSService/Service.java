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
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.Future;

public abstract class Service {
    private ServiceContext context;         // The user-defined context of the service
    private final int BUFFER_SIZE = 1024 * 1024;

    // Buffer with Peer
    private ByteBuffer inputBufferFromPeer;
    private ByteBuffer outputBufferToPeer;

    // Socket with Peer
    private SecureFlexIDSession secureFlexIDSession;

    // Buffer with Server, if Proxy
    private ByteBuffer inputBufferFromServer;
    private ByteBuffer outputBufferToServer;

    // Socket with Server
    // private AsynchronousSocketChannel serverSession

    public Service(ServiceContext context) {
        this.context = context;
        // Buffer with Peer
        this.inputBufferFromPeer = ByteBuffer.allocate(BUFFER_SIZE);
        this.outputBufferToPeer = ByteBuffer.allocate(BUFFER_SIZE);

        // Buffer with Server, if Proxy
        if (this.context.isProxy()) {
            this.inputBufferFromServer = ByteBuffer.allocate(BUFFER_SIZE);
            this.outputBufferToServer = ByteBuffer.allocate(BUFFER_SIZE);
        }
    }

    public boolean hasInputFromPeer() {
        return inputBufferFromPeer.hasRemaining();
    }

    public boolean hasInputFromServer() {
        return inputBufferFromServer.hasRemaining();
    }

    public boolean hasOutputToPeer() {
        return outputBufferToPeer.hasRemaining();
    }

    public boolean hasOutputToServer() {
        return outputBufferToServer.hasRemaining();
    }

    // Initialize the service (e.g., open a socket)
    public void initService() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, InterruptedException {
        System.out.println("[FogOSService] Start: initService()");
        boolean connected;
        secureFlexIDSession = new SecureFlexIDSession(Role.RESPONDER, context.getServiceID());

        if (context.isProxy()) {
            System.out.println("[FogOSService] Proxy: processInputFromProxy()");
            InetSocketAddress serverAddr;

            serverAddr = new InetSocketAddress(context.getServerLoc().getAddr(),
                        context.getServerLoc().getPort());

        }
        System.out.println("[FogOSService] Finish: processInputFromProxy()");
    }

    // Processing of the service regarding service requests from a client
    public abstract void processInputFromPeer();
    public abstract void processOutputToPeer();

    // Processing of the service regarding proxying
    // This should be overridden when the application is a proxy
    public void processInputFromServer() {
        System.out.println("[FogOSService] Start: processInputFromProxy()");
        if (context.isProxy()) {
            System.out.println("[FogOSService] Proxy: processInputFromProxy()");
        }
        System.out.println("[FogOSService] Finish: processInputFromProxy()");
    }

    public void processOutputToServer() {
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

    public SecureFlexIDSession getSecureFlexIDSession() {
        return secureFlexIDSession;
    }

    public ByteBuffer getInputFromPeer(byte[] buf) {
        return inputBufferFromPeer.get(buf);
    }

    public ByteBuffer getInputFromServer(byte[] buf) {
        return inputBufferFromServer.get(buf);
    }

    public ByteBuffer getOutputToPeer(byte[] buf) {
        return outputBufferToPeer.get(buf);
    }

    public ByteBuffer getOutputToServer(byte[] buf) {
        return outputBufferToServer.get(buf);
    }
}
