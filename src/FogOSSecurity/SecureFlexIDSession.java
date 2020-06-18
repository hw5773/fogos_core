package FogOSSecurity;

import FlexID.FlexID;
import FogOSSocket.FlexIDSession;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;

/**
 *  Implements an API for secure flexID session
 *  @author Hyeonmin Lee
 */
public class SecureFlexIDSession {
    private SecurityParameters securityParameters;
    private HandshakeProtocolManager handshakeManager;
    private RecordProtocolManager recordManager;
    private FlexIDSession flexIDSession;
    private final String TAG = "FogOSSecurity";

    /**
     * Construct the SecureFlexIDSession
     * @param role the role of the entity
     * @param dFID the FlexID of the destination
     */
    public SecureFlexIDSession(Role role, FlexID dFID) {
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Start: Initialize SecureFlexIDSession");
        this.flexIDSession = FlexIDSession.accept(dFID);
        initialization(role);
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Finish: Initialize SecureFlexIDSession");
    }

    /**
     * Construct the SecureFlexIDSession
     * @param role the role of the entity
     * @param sFID the FlexID of the source
     * @param dFID the FlexID of the destination
     */
    public SecureFlexIDSession(Role role, FlexID sFID, FlexID dFID) {
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Start: Initialize SecureFlexIDSession");
        this.flexIDSession = new FlexIDSession(sFID, dFID);
        initialization(role);
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Finish: Initialize SecureFlexIDSession");
    }

    /**
     * Construct the SecureFlexIDSession
     * @param role the role of the entity
     * @param flexIDSession the FlexIDSession
     */
    public SecureFlexIDSession(Role role, FlexIDSession flexIDSession) {
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Start: Initialize SecureFlexIDSession");
        setFlexIDSession(flexIDSession);
        initialization(role);
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Finish: Initialize SecureFlexIDSession");
    }

    /**
     * Initialize the SecureFlexIDSession
     * @param role the role of the entity
     */
    private void initialization(Role role) {
        this.securityParameters = new SecurityParameters(role);
        this.handshakeManager = new HandshakeProtocolManager(this.securityParameters, this.flexIDSession);
        this.recordManager = new RecordProtocolManager(this.securityParameters, this.flexIDSession);
    }

    /**
     * Perform handshake
     * @param isServer whether the entity is a responder or not
     * @return 1 (handshake done) or -1 (error)
     * @throws Exception an exception
     */
    public int doHandshake(int isServer) throws Exception {
        return this.handshakeManager.doHandshake(isServer);
    }

    /**
     * Send the message
     * @param msg the message
     * @param len the length of the message
     * @return the length of the message
     */
    public int send(byte[] msg, int len) {
        return this.recordManager.send(msg, len);
    }

    /**
     * Send the message
     * @param msg the message
     * @return the length of the message
     */
    public int send(String msg) {
        //System.out.println("Send message (" + msg.length() + " bytes)");
        /*
        try {
            Thread.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        }

         */

        return this.recordManager.send(msg.getBytes(), msg.length());
    }

    /**
     * Receive the message
     * @param buf the buffer
     * @param len the length of the buffer
     * @return the size of the received message
     */
    public int recv(byte[] buf, int len) {
        //System.out.println("Receive message");
/*
        try {
            Thread.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        }
 */
        return this.recordManager.recv(buf, len);
    }

    /**
     * Get the security parameters
     * @return the SecurityParameters
     */
    public SecurityParameters getSecurityParameters() {
        return securityParameters;
    }

    /**
     * Get the FlexID session
     * @return the FlexIDSession
     */
    public FlexIDSession getFlexIDSession() {
        return flexIDSession;
    }

    /**
     * Set the FlexID session
     * @param flexIDSession the flexIDSession
     */
    public void setFlexIDSession(FlexIDSession flexIDSession) {
        this.flexIDSession = flexIDSession;
    }

    /**
     * Close the session
     */
    public void close() {
        this.flexIDSession.close();
    }
}
