package FogOSContent;

import FlexID.FlexID;

public class Content {
    private String name;        // The name of the content
    private String path;        // The path of the content
    private int numOfSegments;  // The number of segments of the content
    private FlexID[] flexID;    // The array of the Flex IDs of the content (Content ID)
    private boolean shared;     // The flag that indicates whether the content is shared (or not)

    public Content(String name, String path, boolean shared) {
        this.name = name;
        this.path = path;
        this.shared = shared;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public int getNumOfSegments() {
        return numOfSegments;
    }

    public FlexID[] getFlexID() {
        return flexID;
    }

    public boolean isShared() {
        return shared;
    }
}
