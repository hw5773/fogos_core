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
    // TODO: (hmlee) Please declare the variable for the socket with the server

    private ServerSession serverSession;

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
        byte[] buf = new byte[16384];
        int len = secureFlexIDSession.recv(buf, buf.length);

        if (len > 0) {
            inputBufferFromPeer.clear();
            inputBufferFromPeer.put(buf);
            inputBufferFromPeer.flip();
        }

        return inputBufferFromPeer.hasRemaining();
    }

    public boolean hasInputFromServer() throws IOException {
        // TODO: (hmlee) Please add the process of reading the socket bound with the server.


        boolean hasInput = serverSession.hasRemaining();
        if (hasInput) {
            byte[] buf = new byte[16384];
            inputBufferFromServer.clear();
            serverSession.read(buf);
            inputBufferFromServer.put(buf);
            inputBufferFromServer.flip();
        }

        return hasInput;
    }

    public boolean hasOutputToPeer() {
        outputBufferToPeer.flip();
        return outputBufferToPeer.hasRemaining();
    }

    public boolean hasOutputToServer() {
        outputBufferToServer.flip();
        return outputBufferToServer.hasRemaining();
    }

    // Initialize the service (e.g., open a socket)
    public void initService() throws Exception {
        System.out.println("[FogOSService] Start: initService()");
        boolean connected;
        secureFlexIDSession = new SecureFlexIDSession(Role.RESPONDER, context.getServiceID());
        int ret = secureFlexIDSession.doHandshake(1);

        if (context.isProxy()) {
            // TODO: (hmlee) Please initialize the socket bound with the server
            System.out.println("[FogOSService] Proxy: processInputFromProxy()");
            //InetSocketAddress serverAddr;

            //serverAddr = new InetSocketAddress(context.getServerLoc().getAddr(),
            //            context.getServerLoc().getPort());

            serverSession = new ServerSession(context.getServerLoc().getAddr(),
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

    // TODO: (hmlee) Please complete the getter function below
    public ServerSession getServerSession() {
        return serverSession;
    }
    
    public SecureFlexIDSession getPeerSession() {
        return secureFlexIDSession;
    }

    public ByteBuffer getInputFromPeer(byte[] buf) {
        ByteBuffer ret = inputBufferFromPeer.get(buf);
        inputBufferFromPeer.clear();
        return ret;

        //return inputBufferFromPeer.get(buf);
    }

    public ByteBuffer getInputFromServer(byte[] buf) {
        ByteBuffer ret = inputBufferFromServer.get(buf);
        inputBufferFromServer.clear();
        return ret;
        //return inputBufferFromServer.get(buf);
    }

    public ByteBuffer getOutputToPeer(byte[] buf) {
        ByteBuffer ret = outputBufferToPeer.get(buf);
        outputBufferToPeer.clear();
        //System.out.println("DWWWWWWWWWWWWWWWWWW");
        //System.out.println(new String(ret.array()).trim());
        return ret;
        //return outputBufferToPeer.get(buf);
    }

    public ByteBuffer getOutputToServer(byte[] buf) {
        ByteBuffer ret = outputBufferToServer.get(buf);
        outputBufferToServer.clear();
        return ret;
        //return outputBufferToServer.get(buf);
    }

    public void putOutputToPeer(ByteBuffer buf) {
        outputBufferToPeer.clear();
        outputBufferToPeer.put(buf.array());
    }

    public void putOutputToServer(ByteBuffer buf) {
        outputBufferToServer.clear();
        outputBufferToServer.put(buf.array());
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
            outputStream.write(buffer, 0, len);
        }

        public void read(byte[] buffer) throws IOException {
            inputStream.read(buffer);

        }

        public boolean hasRemaining() throws IOException {
            int count = inputStream.available();

            if (count == 0)
                return false;
            return true;
        }
    }
}
