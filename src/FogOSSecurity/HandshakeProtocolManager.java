package FogOSSecurity;

import FogOSSocket.FlexIDSession;
import java.security.PrivateKey;
import java.security.Signature;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
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
public class HandshakeProtocolManager extends ProtocolManager {
    private static final String TAG = "FogOSSecurity";

    /**
     * Construct the HandshakeProtocolManager
     * @param securityParameters the security parameters
     * @param flexIDSession the FlexID session
     */
    HandshakeProtocolManager(SecurityParameters securityParameters, FlexIDSession flexIDSession) {
        super(securityParameters, flexIDSession);
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

        KeyAgreement ka = KeyAgreement.getInstance("ECDH");
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Start: doHandshake(): role: " + this.securityParameters.getRole().toString());

        String str_pltPubkey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAihopLt8jzGC1Ss8F6/vCzNivWZQ+7V65dDgFqBQGRjiPTzgr/ERnp0lodn7UMezDsk2UHmN7XWQ3+Lc6klrpZk6UPPYNKwf7dhNXQTbvD35eACmK+DTcI6EpAa/ioAXnzaIP85U6G6rqex21kSj/NQXvhtA4zIh2IEgtsiyqrlBVbpkQmgI3JpOdiW+Eyx5MboymwGenbxJACoEHDAhJqPrvlkLoHn0+NZSGen1mtrFsz7dJBLMuRxbuYlMFHUsJcDAX21qxdQZL627nhZkZPXFB063xTCEN9CphfSvrcFeCGAYCb6IGrGwdU66o/112irn11vBgtVi4xUmlNSrslQIDAQAB";
        String str_pltPrvkey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCKGiku3yPMYLVKzwXr+8LM2K9ZlD7tXrl0OAWoFAZGOI9POCv8RGenSWh2ftQx7MOyTZQeY3tdZDf4tzqSWulmTpQ89g0rB/t2E1dBNu8Pfl4AKYr4NNwjoSkBr+KgBefNog/zlTobqup7HbWRKP81Be+G0DjMiHYgSC2yLKquUFVumRCaAjcmk52Jb4TLHkxujKbAZ6dvEkAKgQcMCEmo+u+WQugefT41lIZ6fWa2sWzPt0kEsy5HFu5iUwUdSwlwMBfbWrF1BkvrbueFmRk9cUHTrfFMIQ30KmF9K+twV4IYBgJvogasbB1Trqj/XXaKufXW8GC1WLjFSaU1KuyVAgMBAAECggEBAIPHQtT/B+HVxiiEM1pi+hcODQSleQZ4BO7pJjykHviSr+0f5JjrjR0L5yprC4A4NOTHMjdtJiKPpvklnjZZma2N/dXN0/LjwweAnRoVmThnlLsC6SC/D/DPr5l0hAO+ntqRcjc0hiYwiV7BkqfZPu0RpOY3entxG4biWPhTtuptQpvq/OQtT1zztsewLnqpi+uBcrHYeGT7jwede5347okhHAwHHzQyZcc7SKtAMNzaBmGBhVDVIB3+NvzuPyKFPfNgB43ByHViFxv7QtwFSlgmxKpwpYrjS5pINj704Z58lNz8OoCOuYkKdgmOR5UsJTN4OU8r0y4UEQwy+5XGjQECgYEA2t0FqEkYNMeVVBw3Z0mOug2UPFsLM20UuzGoDq0UC2njdg77nhqMw3dGAuff9CdmBz9YowvyzaVkHdeh7GCdBlVEA+l+QQiF5rv/5Ho1qonV8UJyGapcm4Z0inNnpWgn1WJ1iSR+MBKIVhMSu+nq97OIrB02XK5rzieTPBb0YBkCgYEAoYkJwnHy727MbWdtRkzE5KgcveSLu9ZJaj3klTmiWhMk0K+Doe7oakOO/1Zzi2VMXbCQAY+3CkJnxBWPN6YpxOl6sDuS7LXRAVdP+G/L8Q26TvUpkYBkf4nCvbGX3QZC6txVuVTLxG0jWSIifNaegCbyfQNbwQwaV9VWOi92j90CgYEAkYbvcRORRd8Duxa7/DDb93h5/ZvoGbzJUSNWhNOvBVvWRDT4OAudV4dihSIbNuRPojgLvvZ97yGvLWypHVysbH6bqCJEsgdxyZduMkTUlF3sZOxypAA0bbF8ombUHxbfjbJXRuZ+BYb9IoayKUMD2sqE8TTHZd8Qjdagvw7gVVkCgYAjQaW/qt87IxO7TTesgFT5Ezgyug9FkB+18IxThaDJyCPg6G3yihJwHw627EeLxTBFwqOrs5Jfyt6bDZmUq8+yCsOcc2Q+BfD4OfZaYwxAMJ7ZXOkVuNA2hfrbLEfZFeTFHhIXwUo4NRnh+nFMjgtKLTX/0xvTprCZOxb23CUkgQKBgE1AXW2IHrc96Xd18T18LmRdLCwSQjC/camBuUoX8AigOeY83zFbpFN7owPjeLbhC0U+GlgLZPRCLUe1WOfwrO6ZCS9+gZ4XjL+peIr/ADU59+j4uoDl7rEeJl/YTu6Gf9pTHZqvSjOQSCYK7XJElp7iAScDSYZx4w4wX3xMuxD4";

        start = Instant.now();
        byte[] byte_pltPubkey = Base64.getDecoder().decode(str_pltPubkey);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PublicKey platformPubkey = factory.generatePublic(new X509EncodedKeySpec(byte_pltPubkey));

        byte[] byte_pltPrvkey = Base64.getDecoder().decode(str_pltPrvkey);
        PrivateKey platformPrvkey = factory.generatePrivate(new PKCS8EncodedKeySpec(byte_pltPrvkey));

        KeyPair myKeyPair = generateKeyPair(999);
        PrivateKey longPrvkey = myKeyPair.getPrivate();
        PublicKey longPubkey = myKeyPair.getPublic();

        end = Instant.now();
        timeElapsed = Duration.between(start, end).toMillis();
        System.out.println("----- Preparation Time::: " + timeElapsed);

        start = Instant.now();
        //int bitLength = 512; // 512 bits
        //SecureRandom rnd = new SecureRandom();
        //BigInteger p512 = BigInteger.probablePrime(bitLength, rnd);
        //BigInteger g512 = BigInteger.probablePrime(bitLength, rnd);

        ECGenParameterSpec p256 = new ECGenParameterSpec("secp256r1");
        //Security.addProvider(new BouncyCastleProvider());
        //ECGenParameterSpec ecSpec = new ECGenParameterSpec(SPEC);
        //DHParameterSpec dhParams = new DHParameterSpec(p512, g512);
        KeyPairGenerator g = KeyPairGenerator.getInstance("EC");
        //g.initialize(dhParams, new SecureRandom());
        g.initialize(p256);
        KeyPair keypair = g.generateKeyPair();
        PublicKey shortPubkey = keypair.getPublic(); //
        PrivateKey shortPrvkey = keypair.getPrivate(); //

        end = Instant.now();
        timeElapsed = Duration.between(start, end).toMillis();
        System.out.println("----- TIME Short-term Key Generation (ECDH)::: " + timeElapsed);

        if (isServer == 0) {
            start = Instant.now();

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(longPubkey.getEncoded());
            byte[] hashed_pubkey_A = md.digest();
            byte[] signed = sign(platformPrvkey, hashed_pubkey_A);
            String str_sign = Base64.getEncoder().encodeToString(signed);

            byte[] byte_longPubkey = longPubkey.getEncoded();
            String str_longPubkey = Base64.getEncoder().encodeToString(byte_longPubkey);

            byte[] byte_shortPubkey = shortPubkey.getEncoded();
            String str_shortPubkey = Base64.getEncoder().encodeToString(byte_shortPubkey);

            String msg = str_shortPubkey + "---Pad---" + str_longPubkey + "---Pad---" + str_sign + "---Pad---";

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

            String[] parts = receivedMsg.split("---Pad---");
            String target_shortPubkey = parts[0];
            String target_longPubkey = parts[1];
            String target_sign = parts[2];

            byte[] byte_target_sign = Base64.getDecoder().decode(target_sign);

            byte[] byte_target_shortPubkey = Base64.getDecoder().decode(target_shortPubkey);

            factory = KeyFactory.getInstance("EC");
            PublicKey decoded_target_shortPubkey = factory.generatePublic(new X509EncodedKeySpec(byte_target_shortPubkey));


            byte[] byte_target_longPubkey = Base64.getDecoder().decode(target_longPubkey);
            factory = KeyFactory.getInstance("RSA");
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

            msg = str_signed_secret + "---Pad---";
            System.out.println("----- TIME INITIATOR Generate Last MSG & MASTER KEY::: " + timeElapsed);

            flexIDSession.send(msg.getBytes());
            rcvd = -1;
            while (rcvd < 0) {
                rcvd = flexIDSession.receive(buf);
            }

        } else {
            rcvd = -1;
            while (rcvd < 0) {
                rcvd = flexIDSession.receive(buf);
            }
            // ------------------------------------------------------------------------------------------------------------------------

            start = Instant.now();

            String receivedMsg = new String(buf);

            String[] parts = receivedMsg.split("---Pad---");
            String target_shortPubkey = parts[0];
            String target_longPubkey = parts[1];
            String target_sign = parts[2];

            byte[] byte_target_sign = Base64.getDecoder().decode(target_sign);

            byte[] byte_target_shortPubkey = Base64.getDecoder().decode(target_shortPubkey);

            factory = KeyFactory.getInstance("EC");

            PublicKey decoded_target_shortPubkey = factory.generatePublic(new X509EncodedKeySpec(byte_target_shortPubkey));

            byte[] byte_target_longPubkey = Base64.getDecoder().decode(target_longPubkey);

            factory = KeyFactory.getInstance("RSA");

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

            String msg = str_shortPubkey + "---Pad---" + str_longPubkey + "---Pad---" + str_signed_secret + "---Pad---";

            end = Instant.now();
            timeElapsed = Duration.between(start, end).toMillis();
            System.out.println("----- TIME RESPONDER 1::: " + timeElapsed);
            // ------------------------------------------------------------------------------------------------------------------------
            flexIDSession.send(msg.getBytes());
            // ------------------------------------------------------------------------------------------------------------------------

            rcvd = -1;
            while (rcvd < 0) {
                rcvd = flexIDSession.receive(buf);
            }
            receivedMsg = new String(buf);
            // ------------------------------------------------------------------------------------------------------------------------

            start = Instant.now();

            parts = receivedMsg.split("---Pad---");
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

            msg = "HANDSHAKE DONE";
            flexIDSession.send(msg.getBytes());
        }

        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Finish: doHandshake(): role: " + this.securityParameters.getRole().toString());
        this.securityParameters.setMasterSecret(hashed_secret);
        return 1;
    }

    private static byte[] sign(PrivateKey prvKey, byte[] data) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        Signature signer = Signature.getInstance("SHA1withRSA");
        signer.initSign(prvKey);
        signer.update(data);
        byte[] signed = signer.sign();
        return signed;
    }

    private static boolean verifySign(PublicKey key, byte[] data, byte[] signature) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Signature signer = Signature.getInstance("SHA1withRSA");
        signer.initVerify(key);
        signer.update(data);
        return (signer.verify(signature));
    }

    private static KeyPair generateKeyPair(long seed) throws Exception {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = new SecureRandom();
        keyGenerator.initialize(1024, random);

        return (keyGenerator.generateKeyPair());
    }

    private static byte[] getSharedSecret (PrivateKey prvKey, PublicKey pubKey, KeyAgreement ka) throws Exception
    {
        ka.init(prvKey);
        ka.doPhase(pubKey, true);
        byte [] secret = ka.generateSecret();

        return secret;
    }
}
