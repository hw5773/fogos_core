package FogOSResource;

public enum ResourceType {
    Unknown(0, "unknown"),
    NetworkInterface(1, "network-interface"),
    CPU(2, "cpu"),
    Memory(3, "mem");

    int num;
    String resourceType;

    ResourceType(int num, String resourceType) {
        this.num = num;
        this.resourceType = resourceType;
    }

    int getNum() { return num; }
    String getType() {
        return resourceType;
    }
}