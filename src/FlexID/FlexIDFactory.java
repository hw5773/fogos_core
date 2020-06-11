package FlexID;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

public class FlexIDFactory implements FlexIDFactoryInterface {
    private DeviceID dev;
    private final String TAG = "FogOSFlexIDFactory";

    public FlexIDFactory() { this.dev = null; }
    public FlexIDFactory(DeviceID dev) {
        this.dev = dev;
    }

    public FlexID generateDeviceID() {
        byte[] identity;
        Key pub = null;
        Key priv = null;
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            pub = kp.getPublic();
            priv = kp.getPrivate();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return new FlexID(priv.getEncoded(), pub.getEncoded(), FlexIDType.DEVICE, new AttrValuePairs(), null);
    }

    @Override
    public FlexID getMyFlexID(FlexID peer) {
        String identity = "0x4A8FC943011CBAD86228";
        String addr = getLocalIpAddress();
        AttrValuePairs avps = new AttrValuePairs();
        Locator loc = new Locator(InterfaceType.WIFI, addr, 3332);
        FlexID id = new FlexID(identity.getBytes(), FlexIDType.SERVICE, avps, loc);

        return id;
    }

    public String getLocalIpAddress() {
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

    @Override
    public FlexID setPeerFlexID(Locator loc, AttrValuePairs avps) {
        return null;
    }
}
