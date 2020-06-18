package FogOSSecurity;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *  Implements an API for security parameters
 *  @author Hyeonmin Lee
 */
public class SecurityParameters {
    // parameters
    private Role role;
    private final int KEY_LENGTH = 16;
    private byte[] masterSecret;
    private SecretKeySpec i2rSecret; // The session key from an initiator to a responder
    private SecretKeySpec r2iSecret; // The session key from an responder to an initiator
    private int writeSequence;
    private int readSequence;

    /**
     * Construct the SecurityParameters
     * @param role the role of the entity
     */
    SecurityParameters(Role role) {
        this.role = role;
        i2rSecret = null;
        r2iSecret = null;
        masterSecret = null;
    }

    /**
     * Get the role of the entity
     * @return the role
     */
    public Role getRole() {
        return role;
    }

    /**
     * Set the master secret and generate session keys
     * @param masterSecret the master secret
     */
    public void setMasterSecret(byte[] masterSecret) {
        this.masterSecret = masterSecret;
        generateKeys();
    }

    /**
     * Get the master secret
     * @return the master secret
     */
    public byte[] getMasterSecret() {
        return masterSecret;
    }

    /**
     * Generate session keys which are used by an initiator and a responder
     */
    public void generateKeys() {
        byte[] tmp;
        byte[] key = new byte[KEY_LENGTH];
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            digest.update(masterSecret);
            tmp = digest.digest();
            System.arraycopy(tmp, 0, key, 0, KEY_LENGTH);
            i2rSecret = new SecretKeySpec(key, "AES");

            System.out.println("I2R AES Key (" + key.length + " bytes)");
            System.out.println ("{ " + byteArrayToHex(key) + " }");
            System.out.println("");

            digest.reset();
            digest.update(tmp);
            tmp = digest.digest();
            System.arraycopy(tmp, 0, key, 0, KEY_LENGTH);
            r2iSecret = new SecretKeySpec(key, "AES");

            System.out.println("R2I AES Key (" + key.length + " bytes)");
            System.out.println ("{ " + byteArrayToHex(key) + " }");
            System.out.println("");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        setWriteSequence(0);
        setReadSequence(0);
    }

    /**
     * Get write sequence (used for Record encryption)
     * @return the write sequence
     */
    public int getWriteSequence() {
        return writeSequence;
    }

    /**
     * Set write sequence (used for Record encryption)
     * @param writeSequence the write sequence
     */
    public void setWriteSequence(int writeSequence) {
        this.writeSequence = writeSequence;
    }

    /**
     * Get read sequence (used for Record decryption)
     * @return the write sequence
     */
    public int getReadSequence() {
        return readSequence;
    }
    /**
     * Set read sequence (used for Record decryption)
     * @param readSequence the write sequence
     */
    public void setReadSequence(int readSequence) {
        this.readSequence = readSequence;
    }

    /**
     * Convert a byte array to hex format
     * @param a the byte array
     * @return the hex format string
     */
    private String byteArrayToHex(byte[] a) {
        int idx = 0;
        StringBuilder sb = new StringBuilder();
        for (final byte b: a) {
            sb.append(String.format("0x%02x, ", b & 0xff));
            idx++;
            if (idx % 8 == 0)
                sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Get the session key used by an initiator
     * @return the session key
     */
    public SecretKeySpec getI2rSecret() {
        return i2rSecret;
    }

    /**
     * Get the session key used by an responder
     * @return the session key
     */
    public SecretKeySpec getR2iSecret() {
        return r2iSecret;
    }
}
