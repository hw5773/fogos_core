package FogOSSecurity;

import FogOSSocket.FlexIDSession;
import java.security.PrivateKey;
import java.security.Signature;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.logging.Level;
import java.security.KeyFactory;
import javax.crypto.KeyAgreement;

/**
 *  Implements an API for handshake protocol manager
 *  @author Hyeonmin Lee
 */
public class HandshakeProtocolManager<signed_longPubkey> extends ProtocolManager {
    private static final String TAG = "FogOSSecurity";
    private static PublicKey platformPubkey, longPubkey;
    private static PrivateKey platformPrvkey, longPrvkey;
    private static KeyAgreement ka;
    private static KeyFactory factory;
    private String signed_longPubkey;

    /**
     * Construct the HandshakeProtocolManager
     * @param securityParameters the security parameters
     * @param flexIDSession the FlexID session
     */
    HandshakeProtocolManager(SecurityParameters securityParameters, FlexIDSession flexIDSession) throws NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        super(securityParameters, flexIDSession);

        ka = KeyAgreement.getInstance("ECDH");

        String str_pltPubkey = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAElKA5aHtbGcdaxX/2Si9JnAtqcBLjAJiXJm9rNlsiJ3MItQd+3zIgB4bWCvzf3S0N2jkTLUiCKg3+PFj52DGcUQ==";
        String str_pltPrvkey = "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCDXAr2E5mZ+MtGFcMTkMTibvn2qDnNJZPPEELv4PvJ17w==";

        factory = KeyFactory.getInstance("EC");
        byte[] byte_pltPubkey = Base64.getDecoder().decode(str_pltPubkey);
        byte[] byte_pltPrvkey = Base64.getDecoder().decode(str_pltPrvkey);
        platformPubkey = factory.generatePublic(new X509EncodedKeySpec(byte_pltPubkey));
        platformPrvkey = factory.generatePrivate(new PKCS8EncodedKeySpec(byte_pltPrvkey));

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(256);
        KeyPair myKeyPair = kpg.generateKeyPair();
        longPrvkey = myKeyPair.getPrivate();
        longPubkey = myKeyPair.getPublic();

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(longPubkey.getEncoded());
        byte[] hashed_pubkey_A = md.digest();
        byte[] signed = sign(platformPrvkey, hashed_pubkey_A);
        signed_longPubkey = Base64.getEncoder().encodeToString(signed);
    }

    /**
     * Perform handshake
     * @param isServer whether the entity is a responder or not
     * @return 1 (handshake done) or -1 (error)
     * @throws Exception an exception
     */
    public int doHandshake(int isServer) throws Exception {
        byte[] buf = new byte[16384];
        int rcvd;
        byte[] hashed_secret;
        Instant start, end;
        long timeElapsed;

        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Start: doHandshake(): role: " + this.securityParameters.getRole().toString());

        start = Instant.now();

        ECGenParameterSpec p256 = new ECGenParameterSpec("secp256r1");
        KeyPairGenerator g = KeyPairGenerator.getInstance("EC");
        g.initialize(p256);
        KeyPair keypair = g.generateKeyPair();
        PublicKey shortPubkey = keypair.getPublic(); //
        PrivateKey shortPrvkey = keypair.getPrivate(); //

        end = Instant.now();
        timeElapsed = Duration.between(start, end).toMillis();
        System.out.println("----- TIME Short-term Key Generation (ECDH)::: " + timeElapsed);

        // client
        if (isServer == 0) {
            start = Instant.now();

            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] byte_longPubkey = longPubkey.getEncoded();
            String str_longPubkey = Base64.getEncoder().encodeToString(byte_longPubkey);

            byte[] byte_shortPubkey = shortPubkey.getEncoded();
            String str_shortPubkey = Base64.getEncoder().encodeToString(byte_shortPubkey);

            String msg = str_shortPubkey + "--Pad--" + str_longPubkey + "--Pad--" + signed_longPubkey + "--Pad--";

            end = Instant.now();
            timeElapsed = Duration.between(start, end).toMillis();
            System.out.println("----- TIME INITIATOR Generate First MSG::: " + timeElapsed);

            start = Instant.now();
            // ------------------------------------------------------------------------------------------------------------------------
            flexIDSession.send(msg.getBytes());

            rcvd = -1;
            while (rcvd < 0) {
                rcvd = flexIDSession.receive(buf);
            }
            String receivedMsg = new String(buf);
            end = Instant.now();
            timeElapsed = Duration.between(start, end).toMillis();
            System.out.println("----- TIME INITIATOR Send/Receive::: " + timeElapsed);
            // ------------------------------------------------------------------------------------------------------------------------

            start = Instant.now();

            String[] parts = receivedMsg.split("--Pad--");
            String target_shortPubkey = parts[0];
            String target_longPubkey = parts[1];
            String target_sign = parts[2];

            byte[] byte_target_sign = Base64.getDecoder().decode(target_sign);

            byte[] byte_target_shortPubkey = Base64.getDecoder().decode(target_shortPubkey);

            factory = KeyFactory.getInstance("EC");
            PublicKey decoded_target_shortPubkey = factory.generatePublic(new X509EncodedKeySpec(byte_target_shortPubkey));


            byte[] byte_target_longPubkey = Base64.getDecoder().decode(target_longPubkey);
            factory = KeyFactory.getInstance("EC");
            PublicKey decoded_target_longPubkey = factory.generatePublic(new X509EncodedKeySpec(byte_target_longPubkey));

            byte[] shared_secret = getSharedSecret(shortPrvkey, decoded_target_shortPubkey, ka);

            boolean verified = verifySign(decoded_target_longPubkey, shared_secret, byte_target_sign);
            if (!verified) {
                return -1;
            }

            end = Instant.now();
            timeElapsed = Duration.between(start, end).toMillis();
            System.out.println("----- TIME INITIATOR Verify Responder's MSG::: " + timeElapsed);

            start = Instant.now();

            byte[] signed_secret = sign(longPrvkey, shared_secret);
            String str_signed_secret = Base64.getEncoder().encodeToString(signed_secret);

            md = MessageDigest.getInstance("SHA-256");
            md.update(shared_secret);
            hashed_secret = md.digest();

            end = Instant.now();
            timeElapsed = Duration.between(start, end).toMillis();

            msg = str_signed_secret + "--Pad--";
            System.out.println("----- TIME INITIATOR Generate Last MSG & MASTER KEY::: " + timeElapsed);

            flexIDSession.send(msg.getBytes());
            rcvd = -1;
            while (rcvd < 0) {
                rcvd = flexIDSession.receive(buf);
            }

        } else { // Server
            rcvd = -1;
            while (rcvd < 0) {
                rcvd = flexIDSession.receive(buf);
            }
            // ------------------------------------------------------------------------------------------------------------------------

            start = Instant.now();

            String receivedMsg = new String(buf);

            String[] parts = receivedMsg.split("--Pad--");
            String target_shortPubkey = parts[0];
            String target_longPubkey = parts[1];
            String target_sign = parts[2];

            byte[] byte_target_sign = Base64.getDecoder().decode(target_sign);

            byte[] byte_target_shortPubkey = Base64.getDecoder().decode(target_shortPubkey);

            factory = KeyFactory.getInstance("EC");

            PublicKey decoded_target_shortPubkey = factory.generatePublic(new X509EncodedKeySpec(byte_target_shortPubkey));

            byte[] byte_target_longPubkey = Base64.getDecoder().decode(target_longPubkey);

            factory = KeyFactory.getInstance("EC");

            PublicKey decoded_target_longPubkey = factory.generatePublic(new X509EncodedKeySpec(byte_target_longPubkey));

            MessageDigest md = MessageDigest.getInstance("SHA-256");

            md.update(decoded_target_longPubkey.getEncoded());

            byte[] hashed_target_pubkey_A = md.digest();

            boolean verified = verifySign(platformPubkey, hashed_target_pubkey_A, byte_target_sign);
            if (!verified) {
                return -1;
            }

            byte[] byte_shortPubkey = shortPubkey.getEncoded();

            String str_shortPubkey = Base64.getEncoder().encodeToString(byte_shortPubkey);
            byte[] byte_longPubkey = longPubkey.getEncoded();

            String str_longPubkey = Base64.getEncoder().encodeToString(byte_longPubkey);

            byte[] shared_secret = getSharedSecret(shortPrvkey, decoded_target_shortPubkey, ka);

            byte[] signed_secret = sign(longPrvkey, shared_secret);

            String str_signed_secret = Base64.getEncoder().encodeToString(signed_secret);

            String msg = str_shortPubkey + "--Pad--" + str_longPubkey + "--Pad--" + str_signed_secret + "--Pad--";

            end = Instant.now();
            timeElapsed = Duration.between(start, end).toMillis();
            System.out.println("----- TIME RESPONDER 1::: " + timeElapsed);

            start = Instant.now();
            // ------------------------------------------------------------------------------------------------------------------------
            flexIDSession.send(msg.getBytes());
            // ------------------------------------------------------------------------------------------------------------------------

            rcvd = -1;
            while (rcvd < 0) {
                rcvd = flexIDSession.receive(buf);
            }
            receivedMsg = new String(buf);

            end = Instant.now();
            timeElapsed = Duration.between(start, end).toMillis();
            System.out.println("----- TIME Send/Receive::: " + timeElapsed);
            // ------------------------------------------------------------------------------------------------------------------------

            start = Instant.now();

            parts = receivedMsg.split("--Pad--");
            target_sign = parts[0];
            byte_target_sign = Base64.getDecoder().decode(target_sign);

            verified = verifySign(decoded_target_longPubkey, shared_secret, byte_target_sign);
            if (!verified) {
                return -1;
            }

            md = MessageDigest.getInstance("SHA-256");
            md.update(shared_secret);
            hashed_secret = md.digest();

            end = Instant.now();
            timeElapsed = Duration.between(start, end).toMillis();
            System.out.println("----- TIME RESPONDER 2::: " + timeElapsed);

            msg = "1";
            flexIDSession.send(msg.getBytes());
        }

        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Finish: doHandshake(): role: " + this.securityParameters.getRole().toString());
        this.securityParameters.setMasterSecret(hashed_secret);
        return 1;
    }

    private static byte[] sign(PrivateKey prvKey, byte[] data) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(prvKey);
        signer.update(data);
        byte[] signed = signer.sign();
        return signed;
    }

    private static boolean verifySign(PublicKey key, byte[] data, byte[] signature) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initVerify(key);
        signer.update(data);
        return (signer.verify(signature));
    }
/*
    private static KeyPair generateKeyPair(long seed) throws Exception {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = new SecureRandom();
        keyGenerator.initialize(1024, random);

        return (keyGenerator.generateKeyPair());
    }
*/
    private static byte[] getSharedSecret (PrivateKey prvKey, PublicKey pubKey, KeyAgreement ka) throws Exception
    {
        ka.init(prvKey);
        ka.doPhase(pubKey, true);
        byte [] secret = ka.generateSecret();

        return secret;
    }
}
