package FlexID;

import org.w3c.dom.Attr;

import java.security.*;
import java.util.Base64;
import java.util.logging.Level;

public class FlexID implements FlexIDInterface {
    private static final String TAG = "FogOSFlexID";
    private byte[] identity;            // The hash value of the public key
    private String sidentity;
    private byte[] priv;                // The private key corresponding to the above public key
    private byte[] pub;
    private FlexIDType type;            // The type of Flex ID
    private AttrValuePairs avps;        // The attribute-value pairs of Flex ID
    private Locator loc;                // The locator

    public FlexID() {

    }

    public FlexID(KeyPair keyPair) throws NoSuchAlgorithmException {
        this.priv = keyPair.getPrivate().getEncoded();
        this.pub = keyPair.getPublic().getEncoded();
        this.type = FlexIDType.ANY;
        this.avps = new AttrValuePairs();
        this.loc = null;
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        this.identity = digest.digest(this.pub);

        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < this.identity.length; i++)
            hexString.append(Integer.toHexString(0xFF & this.identity[i]));
        this.sidentity = hexString.toString();
    }

    public FlexID(FlexIDType type, byte[] pub) throws NoSuchAlgorithmException {
        this.priv = null;
        this.pub = pub;
        this.type = type;
        this.avps = new AttrValuePairs();
        this.loc = null;
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        this.identity = digest.digest(this.pub);
        this.sidentity = new String(this.identity);
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
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < this.identity.length; i++)
            hexString.append(Integer.toHexString(0xFF & this.identity[i]));
        this.sidentity = hexString.toString();
    }

    public FlexID(byte[] identity, FlexIDType type, AttrValuePairs avps, Locator loc) {
        this.identity = identity;
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < this.identity.length; i++)
            hexString.append(Integer.toHexString(0xFF & this.identity[i]));
        this.sidentity = hexString.toString();
        this.type = type;
        this.avps = avps;
        this.loc = loc;
        this.priv = null;
    }

    public FlexID(KeyPair keyPair, FlexIDType type, AttrValuePairs avps, Locator loc) {
        this.priv = keyPair.getPrivate().getEncoded();
        this.pub = keyPair.getPublic().getEncoded();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            digest.update(this.pub);
            this.identity = digest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        this.type = type;
        this.avps = avps;
        this.loc = loc;
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < this.identity.length; i++)
            hexString.append(Integer.toHexString(0xFF & this.identity[i]));
        this.sidentity = hexString.toString();
    }

    public FlexID(byte[] priv, byte[] pub, FlexIDType type, AttrValuePairs avps, Locator loc) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            digest.update(pub);
            this.identity = digest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        this.priv = priv;
        this.pub = pub;
        this.type = type;
        this.avps = avps;
        this.loc = loc;
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < this.identity.length; i++)
            hexString.append(Integer.toHexString(0xFF & this.identity[i]));
        this.sidentity = hexString.toString();
    }

    // TODO: Should implement this function
    public static FlexID testDeviceID() {
        byte[] identity = {
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
                0x0, 0x0, 0x0, 0x0
        };

        return new FlexID(identity, FlexIDType.DEVICE, new AttrValuePairs(), null);
    }

    public byte[] getIdentity() {
        return identity;
    }

    public String getStringIdentity() {
        return sidentity;
        /*
        Base64.Encoder encoder = Base64.getEncoder();
        String encodedIdentity = encoder.encodeToString(identity);
        // Replace "+" character to ":" to solve MQTT topic problem
        encodedIdentity = encodedIdentity.replace('+', ':');
        return encodedIdentity;
         */
    }

    public void setIdentity(byte[] identity) {
        this.identity = identity;
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < this.identity.length; i++)
            hexString.append(Integer.toHexString(0xFF & this.identity[i]));
        this.sidentity = hexString.toString();
    }

    public void setSidentity(String sidentity) {
        this.sidentity = sidentity;
        this.identity = sidentity.getBytes();
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

    public byte[] getPub() {
        return pub;
    }

    public void setPub(byte[] pub) {
        this.pub = pub;
    }
}
