package FogOSService;

import FlexID.AttrValuePairs;
import FlexID.InterfaceType;
import FlexID.Locator;
import FlexID.ServiceID;
import FogOSSecurity.SecureFlexIDSession;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.KeyPair;

public class ServiceContext {
    private String name;
    private ServiceID serviceID;
    private boolean isProxy;
    private Locator serverLoc;
    private Object userContext;
    private ServiceType serviceType;
    private AttrValuePairs avps;

    public ServiceContext(String name, ServiceType serviceType, KeyPair keyPair,
                          Locator serviceLoc, boolean isProxy, Locator proxyLoc) {
        init(name, serviceType, serviceLoc, isProxy, proxyLoc);
        this.serviceID = new ServiceID(keyPair.getPrivate().getEncoded(),
                keyPair.getPublic().getEncoded(), serviceLoc);
    }

    public ServiceContext(String name, ServiceType serviceType, byte[] priv, byte[] pub,
                          Locator serviceLoc, boolean isProxy, Locator proxyLoc) {
        init(name, serviceType, serviceLoc, isProxy, proxyLoc);
        this.serviceID = new ServiceID(priv, pub, serviceLoc);
    }

    private void init(String name, ServiceType serviceType, Locator serviceLoc, boolean isProxy, Locator serverLoc) {
        this.name = name;
        this.serviceType = serviceType;
        this.isProxy = isProxy;
        this.serverLoc = serverLoc;
        this.userContext = null;
    }

    public ServiceID getServiceID() {
        return serviceID;
    }

    public void setServiceID(ServiceID serviceID) { this.serviceID = serviceID; }

    public ServiceType getServiceType() { return serviceType; }

    public boolean isProxy() {
        return isProxy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Locator getServiceLoc() {
        return serviceID.getLocator();
    }

    public void setServiceLoc(Locator serviceLoc) {
        serviceID.setLocator(serviceLoc);
    }

    public Locator getServerLoc() {
        return serverLoc;
    }

    public void setServerLoc(Locator serverLoc) {
        this.serverLoc = serverLoc;
    }

    public Object getUserContext() {
        return userContext;
    }

    public void setUserContext(Object userContext) {
        this.userContext = userContext;
    }

    public AttrValuePairs getAvps() {
        return avps;
    }
}
