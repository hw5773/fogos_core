package FogOSSecurity;

import FogOSSocket.FlexIDSession;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.logging.Level;

/**
 *  Implements an API for record handling
 *  @author Hyeonmin Lee
 */
public class RecordProtocolManager extends ProtocolManager {
    private static final String TAG = "FogOSSecurity";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    /**
     * Construct the RecordProtocolManager
     * @param securityParameters the SecurityParameters
     * @param flexIDSession the FlexIDSession
     */
    RecordProtocolManager(SecurityParameters securityParameters, FlexIDSession flexIDSession) {
        super(securityParameters, flexIDSession);
    }

    /**
     * Send the message through flexIDSession
     * @param msg the message
     * @param len the length of the message
     * @return the length of the message
     */
    public int send(byte[] msg, int len) {
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Start: send()");
        try {
            java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "msg: " + new String(msg, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        int ret;
        int sent;
        byte[] ciph = null;
        if (this.securityParameters.getRole() == Role.INITIATOR) {
            ciph = encrypt(this.securityParameters.getI2rSecret(), msg, len);
        } else {
            ciph = encrypt(this.securityParameters.getR2iSecret(), msg, len);
        }
        sent = this.flexIDSession.send(ciph);
        //System.out.println("Sent: " + sent + " bytes");
        //System.out.println("Sent Message");
        //System.out.println(byteArrayToHex(ciph, sent));

        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Finish: send()");
        return len;
    }

    /**
     * Receive the message through flexIDSession
     * @param msg the buffer
     * @param len (not used currently)
     * @return the size of the received message
     */
    public int recv(byte[] msg, int len) {
        byte[] ciph = new byte[16384];
        int rcvd = this.flexIDSession.receive(ciph);
        byte[] ret;
        if (rcvd > 0) {
            //System.out.println("Received: " + rcvd + " bytes");
            //System.out.println("Received Message");
            //System.out.println(byteArrayToHex(ciph, rcvd));

            if (this.securityParameters.getRole() == Role.INITIATOR) {
                ret = decrypt(this.securityParameters.getR2iSecret(), ciph, rcvd);
            } else {
                ret = decrypt(this.securityParameters.getI2rSecret(), ciph, rcvd);
            }
            System.arraycopy(ret, 0, msg, 0, ret.length);
            rcvd = ret.length;

            /*
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }

             */
        }
        return rcvd;
    }

    /**
     * Encrypt the message
     * @param key the key used to encryption
     * @param msg the message
     * @param len the length of the message
     * @return the encrypted message
     */
    byte[] encrypt(SecretKeySpec key, byte[] msg, int len) {
        byte[] ret = null;
        byte[] buf = new byte[len];
        System.arraycopy(msg, 0, buf, 0, len);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            digest.update((byte) this.securityParameters.getWriteSequence());
            digest.update(this.securityParameters.getMasterSecret());
            byte[] ivtmp = digest.digest();
            byte[] iv = new byte[GCM_IV_LENGTH];
            //System.arraycopy(ivtmp, 0, iv, 0, GCM_IV_LENGTH);
            //System.out.println("Key");
            //System.out.println(byteArrayToHex(key.getEncoded(), -1));
            //System.out.println("Initialization Vector");
            //System.out.println(byteArrayToHex(iv, GCM_IV_LENGTH));

            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv));
            ret = cipher.doFinal(msg);
            this.securityParameters.setWriteSequence(this.securityParameters.getWriteSequence() + 1);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidParameterException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        if (ret == null) {
            System.out.println("encrypted value is null");
        } else {
            System.out.println("encrypted value is not null");
            System.out.println("Encrypted Message (" + ret.length + " bytes)");
            System.out.println(byteArrayToHex(ret, -1));
        }
        return ret;
    }

    /**
     * Decrypt the messsage
     * @param key the key used to decrypt
     * @param ciph the encrypted message
     * @param len the length of the encrypted message
     * @return the decrypted message
     */
    byte[] decrypt(SecretKeySpec key, byte[] ciph, int len) {
        byte[] ret = null;
        byte[] buf = new byte[len];
        System.arraycopy(ciph, 0, buf, 0, len);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            digest.update((byte) this.securityParameters.getReadSequence());
            digest.update(this.securityParameters.getMasterSecret());
            byte[] ivtmp = digest.digest();
            byte[] iv = new byte[GCM_IV_LENGTH];
            //System.arraycopy(ivtmp, 0, iv, 0, GCM_IV_LENGTH);
            //System.out.println("Key");
            //System.out.println(byteArrayToHex(key.getEncoded(), -1));
            //System.out.println("Initialization Vector");
            //System.out.println(byteArrayToHex(iv, GCM_IV_LENGTH));

            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv));
            ret = cipher.doFinal(buf);

            this.securityParameters.setReadSequence(this.securityParameters.getReadSequence() + 1);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidParameterException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        if (ret == null) {
            System.out.println("decrypted value is null");
        } else {
            System.out.println("decrypted value is not null");
            System.out.println("Decrypted Message (" + ret.length + " bytes)");
            System.out.println(byteArrayToHex(ret, -1));
        }
        return ret;
    }
}
