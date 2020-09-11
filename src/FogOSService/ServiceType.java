package FogOSService;

public enum ServiceType {
    Unknown(0, "unknown"),
    Streaming(1, "streaming"),
    Computing(2, "computing");

    int num;
    String serviceType;

    ServiceType(int num, String serviceType) {
        this.num = num;
        this.serviceType = serviceType;
    }

    int getNum() { return num; }
    String getType() {
        return serviceType;
    }
}
