package FogOSService;

import FlexID.FlexID;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public abstract class Service implements FogOSServiceAPI {
    private String name;        // The name of the service
    private Object ctx;         // The user-defined context of the service
    private FlexID flexID;      // The Flex ID of the service (Service ID)
    private final boolean proxy;    // The flag that indicates whether the service utilizes the proxying

    public Service(String name, boolean proxy) {
        this.name = name;
        this.ctx = null;
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

    public abstract void init_ctx(Object ctx);
    public abstract void process(Object ctx, InputStreamReader isr, OutputStreamWriter osw);
}
