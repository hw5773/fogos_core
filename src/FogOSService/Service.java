package FogOSService;

import FlexID.FlexID;

public class Service {
    private String name;        // The name of the service
    private FlexID flexID;      // The Flex ID of the service (Service ID)
    private final boolean proxy;    // The flag that indicates whether the service utilizes the proxying

    public Service(String name, boolean proxy) {
        this.name = name;
        this.proxy = proxy;
    }

    public String getName() {
        return name;
    }

    public FlexID getFlexID() {
        return flexID;
    }

    public boolean isProxy() {
        return proxy;
    }
}
