package FlexID;

import java.util.logging.Level;

public class FlexID implements FlexIDInterface {
    private static final String TAG = "FogOSFlexID";
    private byte[] identity;            // The hash value of the public key
    private String sidentity;
    private byte[] priv;                // The private key corresponding to the above public key
    private FlexIDType type;            // The type of Flex ID
    private AttrValuePairs avps;        // The attribute-value pairs of Flex ID
    private Locator loc;                // The locator

    public FlexID() {

    }

    public FlexID(String id) {
        sidentity = id;
        identity = id.getBytes();
        priv = null;
        type = FlexIDType.ANY;
        avps = new AttrValuePairs();
        loc = null;
    }

    public FlexID(byte[] id) {
        identity = id;
        priv = null;
        type = FlexIDType.ANY;
        avps = new AttrValuePairs();
        loc = null;
    }

    public FlexID(byte[] identity, FlexIDType type, AttrValuePairs avps, Locator loc) {
        this.identity = identity;
        this.sidentity = new String(identity);
        this.type = type;
        this.avps = avps;
        this.loc = loc;
        this.priv = null;
    }

    // TODO: Should implement this function
    public static FlexID generateDeviceID() {
        byte[] testID = new byte[] {
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0
        };

        return new FlexID(testID, FlexIDType.DEVICE, new AttrValuePairs(), null);
    }

    public byte[] getIdentity() {
        return identity;
    }

    public String getStringIdentity() {
        return new String(identity);
    }

    public void setIdentity(byte[] identity) {
        this.identity = identity;
    }

    public FlexIDType getType() {
        return type;
    }

    public void setType(FlexIDType type) {
        this.type = type;
    }

    public AttrValuePairs getAvps() {
        return avps;
    }

    public void setAvps(AttrValuePairs avps) {
        this.avps = avps;
    }

    public void setLocator(Locator loc) {
        this.loc = loc;
    }

    @Override
    public String getValueByAttr(String attr) {
        return null;
    }

    @Override
    public Locator getLocator() {
        return loc;
    }

    @Override
    public void updateLocator(Locator loc) {

    }

    public byte[] getPriv() {
        return priv;
    }

    public void setPriv(byte[] priv) {
        this.priv = priv;
    }
}
