package FogOSSocket;

import FlexID.FlexID;
import FlexID.Locator;
import FlexID.InterfaceType;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.logging.Level;

public class FlexIDSession {
    private static int port = 3335; // serverSock's default port
    //int lock = 0;

    private static final String TAG = "FogOSSession";

    private FlexID SFID; // source FlexID
    private FlexID DFID; // destination FlexID
    private byte[] connID;

    private SessionLogger sessionLogger = null;
    private boolean rebinding = false;
    private String prevIP = null;
    private long start, end;

    private int isServer;
    private boolean readyToConnect = false;
    private boolean retransmission = false;
    private boolean clientIPChanged = false;
    private boolean serverIPChanged = false;

    // vars to mange data sequence at the inbound, outbound function.
    private int sentSEQ = 0; // SEQ of my data
    private int recvACK = 0;
    private int recvSEQ = 0;
    private int sentACK = 0; // send ACK for receiving data

    private CircularQueue rbuf = new CircularQueue();
    private CircularQueue wbuf = new CircularQueue();

    private FlexIDSocket socket;
    private Thread inThread;
    private Thread outThread;

    public void createConnection(FlexIDSocket sock) {
        try {
            MessageDigest sh = MessageDigest.getInstance("SHA-1");
            sh.update(SFID.getIdentity());
            sh.update(DFID.getIdentity());
            connID = sh.digest();
            System.out.println("ConnID: ");
            for (int i=0; i<connID.length; i++) {
                if (i == 10)
                    System.out.println();
                System.out.print(connID[i]);
            }
            System.out.println();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if(sock != null) {
            socket = sock;
            isServer = 1;
        } else {
            socket = new FlexIDSocket(DFID); // connect to server
            java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Client's IP address: " + socket.getLocalInetAddress() + ":" + socket.getLocalPort());

            if (SFID.getLocator() == null) {
                SFID.setLocator(new Locator(InterfaceType.ETH, socket.getInetAddress(), socket.getPort()));
            }

            if (socket != null)
                System.out.println("[FlexIDSession] Connect Success");
            else
                System.out.println("[FlexIDSession] Connect Failure");

            isServer = 0;
        }
    }

    public FlexIDSession(FlexID sFID, FlexID dFID, FlexIDSocket sock) {
        SFID = sFID;
        DFID = dFID;
        createConnection(sock);

        inThread = new Thread(new inbound());
        inThread.setDaemon(true);
        inThread.start();

        outThread = new Thread(new outbound());
        outThread.setDaemon(true);
        outThread.start();
    }

    public FlexIDSession(FlexID sFID, FlexID dFID) {
        this(sFID, dFID, null); // client case
    }

    public FlexIDSession(FlexID sFID, FlexID dFID, FlexIDSocket sock, boolean sessionLogger) {
        this(sFID, dFID, sock);
        if (sessionLogger)
            this.sessionLogger = new SessionLogger();
    }
    public SessionLogger getSessionLogger() {
        return sessionLogger;
    }
    /*
    public void mobility() {
        port = 3337; // change port.
        FlexIDServerSocket server = new FlexIDServerSocket(port);
        System.out.println("Server waits a reconnection.");
        // socket.close();

        socket = server.accept();
        System.out.println("ReConnected.");
        retransmission = true;
    }
    */

    public static FlexIDSession accept(FlexID flexID) {
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Start: FlexIDSession accept");
        FlexIDServerSocket server;

        if (flexID != null) {
            server = new FlexIDServerSocket(flexID.getLocator().getPort());
        } else {
            server = new FlexIDServerSocket(port);
        }
        System.out.println("Server waits a connection.");
        FlexIDSocket sock = server.accept();
        if(sock == null) {
            System.out.println("accept failed.");
            return null;
        }

        System.out.println("Connected.");

        FlexID sFID = new FlexID();
        Locator sLoc = new Locator(InterfaceType.WIFI, sock.getInetAddress(), sock.getPort());
        System.out.println("Source IP Address: " + sock.getInetAddress() + "  Port: " + sock.getPort());
        sFID.setIdentity("0x5555".getBytes());
        sFID.setLocator(sLoc);

        FlexID dFID = new FlexID();
        Locator dLoc = new Locator(InterfaceType.ETH, server.getInetAddress(), server.getPort());
        System.out.println("Destination IP Address: " + server.getInetAddress() + "  Port: " + server.getPort());
        dFID.setIdentity("0x1111".getBytes());
        dFID.setLocator(dLoc);

        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Finish: FlexIDSession accept");
        return new FlexIDSession(sFID, dFID, sock);
    }

    // send, receive: functions used in application layer
    public int send(byte[] msg) {
        return wbuf.write(msg);
    }
    public int receive(byte[] b) {
        return rbuf.read(b);
    }

    private class inbound implements Runnable {
        public void run() {
            try {
                while(!inThread.isInterrupted()) {
                    if (isServer == 0) {
                        if (clientIPChanged) { // When client's IP changed
                            while(!getReadyToConnect()) {}
                            setReadyToConnect(false);

                            System.out.println("[FlexIDSession] We are going to access to " + DFID.getLocator().getAddr() + ":" + DFID.getLocator().getPort());
                            createConnection(null);
                            setClientIPChanged(false);
                        } else if (serverIPChanged) { // When source changed
                            while(!getReadyToConnect()) {}
                            setReadyToConnect(false);

                            System.out.println("[FlexIDSession] We are going to access to " + DFID.getLocator().getAddr() + ":" + DFID.getLocator().getPort());
                            createConnection(null);
                            setServerIPChanged(false);
                        }
                    }

                    byte[] message;
//                    if(lock != 1) {
//                        lock = 1;
                    if((message = getRecvMsg()) != null) {
                        if (rebinding) {
                            end = System.currentTimeMillis();
                            System.out.println("[Rebinding] Rebinding Time End: " + end);
                            if (sessionLogger != null)
                            {
                                System.out.println("[Rebinding] Rebinding: " + (end - start));
                                sessionLogger.addSessionLog(LogType.REBINDING, end - start, prevIP, SFID.getLocator().getAddr());
                                System.out.println("Log is added: REBINDING / " + (end - start) + " / " + prevIP + " / " + SFID.getLocator().getAddr());
                                rebinding = false;
                            }
                        }
                        if (sessionLogger != null) {
                            sessionLogger.addSessionLog(LogType.DATA, message.length, DFID.getLocator().getAddr(), SFID.getLocator().getAddr());
                        }
                        byte[] header = getHeader(message); // length(2B) + connID(20B) + seq(4B), ack(4B)
                        byte[] data = getData(message);
                        byte[] msgConnID = Arrays.copyOfRange(header, 2, 22);

                        /*
                        if(!Arrays.equals(connID, msgConnID)) {
                            System.out.println("connID unmatched.");
                            lock = 0;
                            continue;
                        }
                        */

                        int length = Conversion.byte2ToInt(Arrays.copyOfRange(header, 0, 2)); // total length
                        int msgSeq = Conversion.byte4ToInt(Arrays.copyOfRange(header, 22, 26));
                        int msgAck = Conversion.byte4ToInt(Arrays.copyOfRange(header, 26, 30));

                        if(length > 30) { // received data message.
                            if(sentACK <= msgSeq) {
                                recvSEQ = msgSeq;
                                sentACK = msgSeq+1;
                                rbuf.write(data);
                            }

                            // send ACK message.
                            byte[] ACKmessage = setHeader(null);
//								System.out.println("Wait for sending ACK (2s)");
//								Thread.sleep(2000);
                            //System.out.println("Send ACK message #: " + sentACK + " to " + DFID.getLocator().getAddr());

//								Conversion.byteToAscii(ACKmessage);
                            socket.write(ACKmessage);
//								System.out.println("ACK write done.");
                        }
                        else { // received ACK message.
                            //System.out.println("Received ACK message");
                            if(msgAck >= (sentSEQ+1)) {
                                recvACK = msgAck;
                            }
                        }
                    }
//                    lock = 0;
                }
            } catch (Exception e) {
                e.printStackTrace();
//				System.exit(0);
            } finally {
//                lock = 0;
            }

            System.out.println("inThread done.");
        }
    // Polling: Get msg from socket. After that, write the msg to the rbuf.
        public byte[] getRecvMsg() {
            try {
                byte[] message = null;

                if (socket != null)
                    message = socket.read();

                if(message != null) {
                    //System.out.println("Received) " + message.length + "  sentSEQ) " + sentSEQ + "  sentACK) " + sentACK + "  recvSEQ) " + recvSEQ + "  recvACK) " + recvACK);
                    return message;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private class outbound implements Runnable {
        public void run() {
            try {
                byte[] message = new byte[30];

                while(!outThread.isInterrupted()) {
					/* if ip changed, creates new FlexIDSocket, connects,
					   and retransmits the last message if necessary(stop-and-wait)   */
                    if(retransmission == true) {
                        // System.out.println("Retransmission is true.");
                        // System.out.println("Message: " + new String(message));
                        socket.write(message); // stop-and-wait
                        //lock = 0;
                        retransmission = false;
                        continue;
                    }

                    //System.out.println("recvACK: " + recvACK + "  sentSEQ: " + sentSEQ);
                    if((recvACK != (sentSEQ+1)) && (sentSEQ != 0)) {
                        //lock = 0;
                        continue;
                    }

                    if(!wbuf.isEmpty(1)) {
                        //lock = 1;
                        byte[] data = new byte[32768];
                        int dataLen = wbuf.read(data);
                        //System.out.println("Messages to be sent: " + dataLen);
                        data = Arrays.copyOfRange(data, 0, dataLen);

                        byte[] header = setHeader(data);
                        message = new byte[30 + data.length];

                        System.arraycopy(header, 0, message, 0, 30);
                        System.arraycopy(data, 0, message, 30, data.length);
//						System.out.println("MSG: ");
//						Conversion.byteToAscii(message);
                        socket.write(message);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
//				System.exit(0);
            } finally {
//                lock = 0;
            }

            System.out.println("outThread done.");
        }
    }

    public int checkMsgToSend() {
        if(wbuf.isEmpty(1)) {
            return -1;
        }
        else return 1;
    }

    public byte[] getHeader(byte[] message) {
        return Arrays.copyOfRange(message, 0, 30);
    }

    public byte[] setHeader(byte[] message) {
        try {
            // Header(30) = length(2B) + connID(20B) + seq(4B), ack(4B)
            byte[] length = new byte[2];
            byte[] seq = new byte[4];
            byte[] ack = new byte[4];
            byte[] header = new byte[30];

            int msgLength;

            if(message == null) { // ack
                msgLength = 30;
//				System.out.println("ACK: " + sentACK);
                ack = Conversion.int32ToByteArray(sentACK);

            }
            else { // data
                msgLength = message.length + 30;
                sentSEQ += message.length;
                byte[] temp = Conversion.int32ToByteArray(sentSEQ);
                System.arraycopy(temp, 0, seq, 0, temp.length);
            }

            length = Conversion.int16ToByteArray(msgLength);
            System.arraycopy(length, 0, header, 0, 2);
            System.arraycopy(connID, 0, header, 2, 20);
            System.arraycopy(seq, 0, header, 22, 4);
            System.arraycopy(ack, 0, header, 26, 4);

            return header;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        return null;
    }
    public byte[] getData(byte[] message) {
        int dataLength = Conversion.byte2ToInt(Arrays.copyOfRange(message, 0, 2)) - 30;
        byte[] data = new byte[dataLength];
        System.arraycopy(message, 30, data, 0, dataLength);
        return data;
    }

    // Connect to peer's signal server and rebuild/terminate a session
    class ConnectToSignalServerThread extends Thread {
        String type;
        boolean complete = false;
        Socket socket = null;
        public ConnectToSignalServerThread(String type) {
            this.type = type;
        }
        @Override
        public void run() {
            try {
                int ssPort = 3334; // SignalServer's default port.
                System.out.println("[FlexIDSession] Try to access IP: " + DFID.getLocator().getAddr() + "  Port: " + ssPort);

                while (socket == null)
                    socket = new Socket(DFID.getLocator().getAddr(), ssPort);

                while (complete == false) {
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                    JSONObject request = new JSONObject();
                    request.put("type", type);
                    request.put("flex_id", new String(SFID.getIdentity()));
                    out.println(request);

                    JSONObject response = new JSONObject(input.readLine());
                    String flex_id = response.getString("flex_id");
                    if (type == "reconnectACK") {
                        String ip = response.getString("ip");
                        int port = response.getInt("port");

                        System.out.println("[FlexIDSession] Received from Signal Server) ID: " + flex_id + " / ip: " + ip + " / port: " + port);
                        Locator locator = new Locator(InterfaceType.WIFI, ip, port);
                        DFID.setLocator(locator);
                        setReadyToConnect(true);
                    }
                    else if(type == "terminateACK") {
                        System.out.println("[FlexIDSession] Received terminateACK from Signal Server.");
                    }

                    complete = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    // Server-side function
    public void  handleReconnect(int newport) {
        FlexIDServerSocket server = new FlexIDServerSocket(newport);
        System.out.println("Server waits a reconnection.");

        socket = server.accept();
        System.out.println("Reconnected.");
        retransmission = true;
    }

    // Client-side class
    /*
    private class MobilityManager {
        boolean checkAddress(FlexID id) {
            boolean ret;
            String ip = "";

            while (ip.equals("")) {
                ip = getLocalIpAddress();
            }

            System.out.println("Previous IP Address: " + id.getLocator().getAddr());
            System.out.println("Current IP Address: " + ip);

            if (id.getLocator().getAddr().equals(ip)) {
                ret = false;
                System.out.println("The IP Address is not changed.");
            } else {
                ret = true;
                System.out.println("The IP Address is changed.");
                while (ready == false) {}
                start = System.currentTimeMillis();
                System.out.println("[Rebinding] Rebinding Time Start: " + start);
                rebinding = true;
                prevIP = id.getLocator().getAddr();
                id.getLocator().setAddr(getLocalIpAddress());
                ConnectToSignalServerThread connectToSignalServerThread = new ConnectToSignalServerThread("reconnect");
                connectToSignalServerThread.start();
            }

            return ret;
        }

        String getLocalIpAddress() {
            String ip = "";

            try {
                Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();

                while (enumNetworkInterfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                    Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();

                    while (enumInetAddress.hasMoreElements()) {
                        InetAddress inetAddress = enumInetAddress.nextElement();

                        if (inetAddress.isSiteLocalAddress()) {
                            ip = inetAddress.getHostAddress();
                        }
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }

            return ip;
        }
    }
     */

    public int clientIPChange(FlexID sFID) {
        setClientIPChanged(true);
        try {
            ConnectToSignalServerThread connectToSignalServerThread = new ConnectToSignalServerThread("reconnect");
            connectToSignalServerThread.start();

            setSFID(sFID);
            return 1;
        } catch(Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    public int serverIPChange(FlexID dFID) {
        setServerIPChanged(true);
        try {
            ConnectToSignalServerThread connectToSignalServerThread = new ConnectToSignalServerThread("terminate"); // terminate a old session
            connectToSignalServerThread.start();
            setDFID(dFID);
            setReadyToConnect(true);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void close() {
        try {
            inThread.join();
            outThread.join();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public FlexID getSFID() {
        return SFID;
    }
    public void setSFID(FlexID sFID) {
        SFID = sFID;
    }
    public FlexID getDFID() {
        return DFID;
    }
    public void setDFID(FlexID dFID) {
        DFID = dFID;
    }
    public byte[] getConnID() {
        return connID;
    }
    public boolean getReadyToConnect() {
        return this.readyToConnect;
    }
    public void setReadyToConnect(boolean b) {
        this.readyToConnect = b;
    }
    public void setClientIPChanged(boolean b) {
        this.clientIPChanged = b;
    }
    public void setServerIPChanged(boolean b) {
        this.serverIPChanged = b;
    }
}
