package FogOSProxy;

import FlexID.*;
import FogOSCore.FogOSCore;
import FogOSResource.Resource;

import java.io.IOException;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;

public class FogProxy {
    private FogOSCore core;
    private FlexID deviceID;
    private String name;

    public FogProxy() throws IOException, NoSuchAlgorithmException {
        initialization(null, null, null);
    }

    public FogProxy(String name) throws IOException, NoSuchAlgorithmException {
        initialization(name, null, null);
    }

    public FogProxy(String name, byte[] priv, byte[] pub) throws IOException, NoSuchAlgorithmException {
        initialization(name, priv, pub);
    }

    private void initialization(String name, byte[] priv, byte[] pub) throws IOException, NoSuchAlgorithmException {
        core = new FogOSCore();
        this.name = name;
        generateDeviceID(priv, pub);
    }

    // TODO: This function creates a fresh keypair and generates FlexID
    private void generateDeviceID(byte[] priv, byte[] pub) {
        if (priv == null || pub == null) {
            // create a fresh keypair: priv: a private key, pub: a public key
        }
        this.deviceID = new FlexID(priv, pub, FlexIDType.DEVICE, null, null);
    }

    public void addResource(Resource resource) {
        AttrValuePairs avps = this.deviceID.getAvps();
        Value val = new Value(resource.getMax(), resource.getUnit());
        if (avps == null) {
            avps = new AttrValuePairs();
            this.deviceID.setAvps(avps);
        }

        avps.addAttrValuePair(resource.getName(), val);

    }

    public void addLocatorInfo(InterfaceType type, String addr, int port) {
        Locator loc = new Locator(type, addr, port);
        this.deviceID.setLocator(loc);
    }
}
