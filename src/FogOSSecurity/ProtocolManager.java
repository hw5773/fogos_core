package FogOSSecurity;

import FogOSSocket.FlexIDSession;

/**
 *  Implements an API for protocol manager
 *  @author Hyeonmin Lee
 */
public class ProtocolManager {
    protected SecurityParameters securityParameters;
    protected FlexIDSession flexIDSession;

    /**
     * Construct the ProtocolManager
     * @param securityParameters the security parameters
     * @param flexIDSession the FLexID session
     */
    public ProtocolManager(SecurityParameters securityParameters, FlexIDSession flexIDSession) {
        this.securityParameters = securityParameters;
        this.flexIDSession = flexIDSession;
    }

    /**
     * Convert a byte array to hex format
     * @param a the byte array
     * @param len the length of the byte array
     * @return the hex format string
     */
    static String byteArrayToHex(byte[] a, int len) {
        if (len < 0)
            len = a.length;
        byte[] tmp = new byte[len];
        int idx = 0;
        StringBuilder sb = new StringBuilder();
        System.arraycopy(a, 0, tmp, 0, len);

        for (final byte b: tmp) {
            sb.append(String.format("0x%02x, ", b & 0xff));
            idx++;
            if (idx % 8 == 0)
                sb.append("\n");
        }
        return sb.toString();
    }
}
