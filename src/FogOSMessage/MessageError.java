package FogOSMessage;

public enum MessageError {
    NONE("none"),
    PARSE_ERROR("Error in parsing"),
    PROCESS_ERROR("Error in processing");

    String errMessage;

    MessageError(String errMessage) { this.errMessage = errMessage; }
    public String getErrMessage() {
        return errMessage;
    }
}
