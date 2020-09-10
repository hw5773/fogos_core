package FlexID;

public class ServiceID extends FlexID {
    public ServiceID(byte[] priv, byte[] pub, Locator loc) {
        super(priv, pub, FlexIDType.SERVICE, new AttrValuePairs(), loc);
    }
}