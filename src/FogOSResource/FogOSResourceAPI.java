package FogOSResource;

import FlexID.FlexID;

public interface FogOSResourceAPI {
    public String getCurr();
    public boolean isOnDemand();

    public String getName();
    public String getMax();
    public String getUnit();

    public void setMax(String newmax);
    public void setCurr(String newcur);

    public abstract void monitorResource();
}
