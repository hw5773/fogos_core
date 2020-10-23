package FogOSSocket;


import FlexID.FlexID;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;

public class FlexIDSocket {
    private Socket socket;

    private DataInputStream dIn;
    private DataOutputStream dOut;

    public FlexIDSocket(FlexID id) {
        try {
            // convert FlexID to IP address.
            socket = new Socket(id.getLocator().getAddr(), id.getLocator().getPort());

            if((dIn = new DataInputStream(socket.getInputStream())) == null)
                System.exit(0);
            if((dOut = new DataOutputStream(socket.getOutputStream())) == null)
                System.exit(0);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public FlexIDSocket(Socket sock) {
        try {
            socket = sock;
            dIn = new DataInputStream(socket.getInputStream());
            dOut = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
            e.printStackTrace();
        }
    }

    public byte[] read() {
        try {
            int length = dIn.readInt();
//			System.out.println("received message lengeth: " + length);
            if(length > 0) {
                byte[] msg = new byte[length];
                dIn.readFully(msg, 0, msg.length);
                return msg;
            }
        } catch (Exception e) {
            System.out.println("error in read()");
            e.printStackTrace();
//			System.exit(0);
        }
        return null;
    }
    public void write(byte[] msg) {
        try {
            dOut.writeInt(msg.length);
            dOut.write(msg);
            dOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public InputStream getInputStream() {
        try {
            return socket.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null;
        }
    }
    public OutputStream getOutputStream() {
        try {
            return socket.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null;
        }
    }
    public void bind(SocketAddress bindpoint) {
        try {
            socket.bind(bindpoint);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void close() {
        try {
            socket.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void connect(SocketAddress endpoint) {
        try {
            socket.connect(endpoint);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String getInetAddress() {
        return socket.getInetAddress().getHostAddress();
    }

    public String getLocalInetAddress() {
        return socket.getLocalAddress().getHostAddress();
    }

    public int getPort() {
        return socket.getPort();
    }

    public int getLocalPort() {
        return socket.getLocalPort();
    }
}
