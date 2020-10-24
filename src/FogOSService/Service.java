package FogOSService;

import FlexID.FlexID;
import FlexID.InterfaceType;
import FogOSSecurity.Role;
import FogOSSecurity.SecureFlexIDSession;
import FogOSSocket.FlexIDSession;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public abstract class Service {
    private ServiceContext context;         // The user-defined context of the service
    //private final int BUFFER_SIZE = 1024 * 1024;
    private final int BUFFER_SIZE = 16384;

    // Buffer with Peer
    private ServiceBuffer inputBufferFromPeer;
    private ServiceBuffer outputBufferToPeer;
    private boolean hasOutputToPeer = false;

    // Socket with Peer
    private SecureFlexIDSession secureFlexIDSession;

    // Buffer with Server, if Proxy
    private ServiceBuffer inputBufferFromServer;
    private ServiceBuffer outputBufferToServer;
    private boolean hasOutputToServer = false;

    private String prevStr = "";

    // Socket with Server
    // TODO: (hmlee) Please declare the variable for the socket with the server

    private ServerSession serverSession;

    public Service(ServiceContext context) {
        this.context = context;
        // Buffer with Peer
        this.inputBufferFromPeer = new ServiceBuffer(BUFFER_SIZE);
        this.outputBufferToPeer = new ServiceBuffer(BUFFER_SIZE);

        // Buffer with Server, if Proxy
        if (this.context.isProxy()) {
            this.inputBufferFromServer = new ServiceBuffer(BUFFER_SIZE);
            this.outputBufferToServer = new ServiceBuffer(BUFFER_SIZE);
        }
    }

    public boolean hasInputFromPeer() {
        byte[] buf = new byte[BUFFER_SIZE];
        int len = secureFlexIDSession.recv(buf, buf.length);

        boolean ret = (len > 0);
        if (ret) {
            inputBufferFromPeer.writeToBuffer(buf, len);
            System.out.println("[Service] Received in hasInputFromPeer()");
            System.out.print("First 5 bytes: " + buf[0] + " " + buf[1] + " " + buf[2] + " " + buf[3] + " " + buf[4]);
            System.out.println();

            System.out.print("Last 5 bytes: " + buf[len-5] + " " + buf[len-4] + " " + buf[len-3] + " " + buf[len-2] + " " + buf[len-1]);
            System.out.println();
        }

        return ret;
    }

    public boolean hasInputFromServer() throws IOException {
        // TODO: (hmlee) Please add the process of reading the socket bound with the server.

        boolean hasInput = serverSession.hasRemaining();
        int len;
        if (hasInput) {
            byte[] buf = new byte[BUFFER_SIZE];
            len = serverSession.read(buf);
            inputBufferFromServer.writeToBuffer(buf, len);
        }
        //System.out.println(hasInput);
        return hasInput;
    }

    public boolean hasOutputToPeer() {
        //boolean ret = outputBufferToPeer.hasRemaining();
        boolean ret = hasOutputToPeer;
        //System.out.println(hasOutputToPeer);
        return ret;
    }

    public boolean hasOutputToServer() {
        //boolean ret = outputBufferToServer.hasRemaining();
        boolean ret = hasOutputToServer;
        //System.out.println(hasOutputToServer);
        return ret;
    }

    // Initialize the service (e.g., open a socket)
    public void initService() throws Exception {
        System.out.println("[FogOSService] Start: initService()");
        boolean connected;
        secureFlexIDSession = new SecureFlexIDSession(Role.RESPONDER, context.getServiceID());
        int ret = secureFlexIDSession.doHandshake(1);

        if (context.isProxy()) {
            serverSession = new ServerSession(context.getServerLoc().getAddr(),
                    context.getServerLoc().getPort());

        }
        System.out.println("[FogOSService] Finish: initService()");
    }

    // Processing of the service regarding service requests from a client
    public abstract void processInputFromPeer();
    public abstract void processOutputToPeer();

    // Processing of the service regarding proxying
    // This should be overridden when the application is a proxy
    public void processInputFromServer() {
        System.out.println("[FogOSService] Start: processInputFromServer()");
        if (context.isProxy()) {
            System.out.println("[FogOSService] Proxy: processInputFromServer()");
        }
        System.out.println("[FogOSService] Finish: processInputFromServer()");
    }

    public void processOutputToServer() {
        System.out.println("[FogOSService] Start: processOutputToServer()");
        if (context.isProxy()) {
            System.out.println("[FogOSService] Proxy: processOutputToServer()");
        }
        System.out.println("[FogOSService] Finish: processOutputToServer()");
    }

    public ServiceContext getContext() {
        return context;
    }

    public void setContext(ServiceContext context) { this.context = context; }

    // TODO: (hmlee) Please complete the getter function below
    public ServerSession getServerSession() {
        return serverSession;
    }
    
    public SecureFlexIDSession getPeerSession() {
        return secureFlexIDSession;
    }

    public int getInputFromPeer(byte[] buf) {
        int ret;
        ret = inputBufferFromPeer.readFromBuffer(buf, buf.length);
        //System.out.println("[Service] Received in getInputFromPeer(): " + new String(buf));
        System.out.println("[Service] Received in getInputFromPeer()");
        System.out.print("First 5 bytes: " + buf[0] + " " + buf[1] + " " + buf[2] + " " + buf[3] + " " + buf[4]);
        System.out.println();

        System.out.print("Last 5 bytes: " + buf[ret-5] + " " + buf[ret-4] + " " + buf[ret-3] + " " + buf[ret-2] + " " + buf[ret-1]);
        System.out.println();
        return ret;
    }

    public int getInputFromServer(byte[] buf) {
        int ret = inputBufferFromServer.readFromBuffer(buf, buf.length);
        //System.out.println("[Service] Received in getInputFromServer(): " + new String(buf));
        return ret;
    }

    public int getOutputToPeer(byte[] buf) {
        int ret = outputBufferToPeer.readFromBuffer(buf, buf.length);
        hasOutputToPeer = false;
        return ret;
    }

    public int getOutputToServer(byte[] buf) {
        int ret = outputBufferToServer.readFromBuffer(buf, buf.length);
        hasOutputToServer = false;
        return ret;
    }

    public void putOutputToPeer(byte[] buf, int len) {
        outputBufferToPeer.writeToBuffer(buf, len);
        hasOutputToPeer = true;
    }

    public void putOutputToServer(byte[] buf, int len) {
        outputBufferToServer.writeToBuffer(buf, len);
        hasOutputToServer = true;
    }


    public class ServerSession {
        private Socket serverSocket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public ServerSession(String host, int port) throws IOException {
            InetSocketAddress serverAddr;

            serverAddr = new InetSocketAddress(host, port);

            serverSocket = new Socket();
            serverSocket.connect(serverAddr);
            inputStream = serverSocket.getInputStream();
            outputStream = serverSocket.getOutputStream();
        }

        public void send(byte[] buffer, int len) throws IOException {
            //System.out.println(new String(buffer).trim());

            outputStream.write(buffer, 0, len);
            outputStream.flush();
        }

        public int read(byte[] buffer) throws IOException {
            return inputStream.read(buffer);
        }


        public boolean hasRemaining() throws IOException {
            int count = inputStream.available();
            if (count == 0)
                return false;
            return true;
        }
    }
}
