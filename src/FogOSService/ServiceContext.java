package FogOSService;

import FlexID.InterfaceType;
import FlexID.Locator;
import FlexID.ServiceID;

import java.net.InetAddress;

public class ServiceContext {
    private String name;
    private ServiceID serviceID;
    private boolean isProxy;
    private Locator proxyLoc;
    private Object userContext;

    public ServiceContext(String name, byte[] priv, byte[] pub, Locator serviceLoc,
                          boolean isProxy, Locator proxyLoc) {
        this.name = name;
        this.serviceID = new ServiceID(priv, pub, serviceLoc);
        this.isProxy = isProxy;
        this.proxyLoc = proxyLoc;
        this.userContext = null;
    }

    public ServiceID getServiceID() {
        return serviceID;
    }

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

    public Locator getProxyLoc() {
        return proxyLoc;
    }

    public void setProxyLoc(Locator proxyLoc) {
        this.proxyLoc = proxyLoc;
    }

    public Object getUserContext() {
        return userContext;
    }

    public void setUserContext(Object userContext) {
        this.userContext = userContext;
    }
}
