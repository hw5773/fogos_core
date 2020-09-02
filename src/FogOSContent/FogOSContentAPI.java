package FogOSContent;

import FlexID.FlexID;

public interface FogOSContentAPI {
    public String getName();
    public String getPath();
    public int getNumOfSegments();
    public FlexID[] getFlexID();
    public boolean isShared();
}
