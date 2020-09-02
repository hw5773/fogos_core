package FogOSService;

import FlexID.FlexID;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public interface FogOSServiceAPI {
    public String getName();
    public FlexID getFlexID();
    public boolean isProxy();

    public abstract void init_ctx(Object ctx);
    public abstract void process(Object ctx, InputStreamReader isr, OutputStreamWriter osw);
}
