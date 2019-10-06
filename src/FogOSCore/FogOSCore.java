package FogOSCore;

import FogOSMessage.*;
import FlexID.FlexID;
import FlexID.FlexIDFactory;
import FlexID.Locator;
import FlexID.InterfaceType;
import FogOSSecurity.Role;
import FogOSSecurity.SecureFlexIDSession;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FogOSCore {
    private final String cloudName = "www.versatile-cloud.com";
    private final int cloudPort = 3333;
    private LinkedList<FogOSBroker> brokers;
    private FogOSBroker broker;
    private FlexIDFactory factory;
    private FlexID deviceID;
    private ContentStore store;
    private MqttClient mqttClient;
    private HashMap<String, Queue<Message>> receivedMessages;

    // Session and Mobility-related
    private LinkedList<SecureFlexIDSession> sessionList;

    // A thread that periodically detects any change of a locator (e.g., IP address)
    private Runnable mobilityDetector;

    // A thread that periodically reports a status of resources in the device
    private Runnable resourceReporter;

    // QoS Interpreter
    private QoSInterpreter qosInterpreter;

    private static final String TAG = "FogOSCore";

    public FogOSCore() {
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Start: Initialize FogOSCore");

        retrieveBrokerList();
        broker = findBestFogOSBroker();
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Result: findBestFogOSBroker() " + broker.getName());
        store = new ContentStore();

        sessionList = new LinkedList<>();
        initReceivedMessages();

        // Initialize and run the mobility detector
        mobilityDetector = new MobilityDetector(this);
        new Thread(mobilityDetector).start();

        // Initilaize the resource reporter
        resourceReporter = new ResourceReporter(this);
        new Thread(resourceReporter).start();

        // Initialize and run the QoS interpreter
        qosInterpreter = new QoSInterpreter(this);

        // Generate the Flex ID of the device
        // factory = new FlexIDFactory();
        // deviceID = factory.generateDeviceID();
        deviceID = new FlexID("versatile");

        // Initialize the MQTT client
        connect(deviceID);

        // Initialize the subscription to necessary messages
        initSubscribe(deviceID);

        // Send the JOIN message
        // TODO: Need to generalize the message
        Message msg = new JoinMessage(deviceID);
        msg.test(broker); // This should be commented out after being generalized.

        try {

        } catch (Exception e) {
            e.printStackTrace();
        }

        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Finish: Initialize FogOSCore");
    }

    public LinkedList<SecureFlexIDSession> getSessionList() {
        return sessionList;
    }

    public QoSInterpreter getQosInterpreter() {
        return qosInterpreter;
    }

    private void initReceivedMessages() {
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Start: Initialize Received Messages");
        receivedMessages = new HashMap<String, Queue<Message>>();
        receivedMessages.put(MessageType.JOIN_ACK.getTopic(), new LinkedList<>());
        receivedMessages.put(MessageType.LEAVE_ACK.getTopic(), new LinkedList<>());
        receivedMessages.put(MessageType.MAP_UPDATE_ACK.getTopic(), new LinkedList<>());
        receivedMessages.put(MessageType.REGISTER_ACK.getTopic(), new LinkedList<>());
        receivedMessages.put(MessageType.REPLY.getTopic(), new LinkedList<>());
        receivedMessages.put(MessageType.RESPONSE.getTopic(), new LinkedList<>());
        receivedMessages.put(MessageType.STATUS_ACK.getTopic(), new LinkedList<>());
        receivedMessages.put(MessageType.UPDATE_ACK.getTopic(), new LinkedList<>());
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Finish: Initialize Received Messages");
    }

    public FogOSBroker getBroker() {
        return broker;
    }

    // Access to the cloud to get the list of the FogOS brokers
    void retrieveBrokerList() {
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Start: retrieveBrokerList()");
        // request the list to the cloud
        // parse the response and add brokers to "brokers"
        NetworkThread thread = new NetworkThread();
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Finish: retrieveBrokerList()");
    }

    // Ping test and select the best FogOS broker
    FogOSBroker findBestFogOSBroker()
    {
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Start: findBestFogOSBroker()");

        Double rtt1 = 0.0;
        Double rtt2 = 0.0;
        try {
            Process process = Runtime.getRuntime().exec("/system/bin/ping -c 3 " + brokers.get(0).getName());
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;

            while((line = reader.readLine()) != null){
                builder.append(line);
            }
            String result1 = builder.toString();

            process = Runtime.getRuntime().exec("/system/bin/ping -c 3 " + brokers.get(1).getName());
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            builder = new StringBuilder();

            while((line = reader.readLine()) != null){
                builder.append(line);
            }
            String result2 = builder.toString();

            rtt1 = getPingStats(result1);
            rtt2 = getPingStats(result2);
        } catch (IOException e) {
            e.printStackTrace();
        }

        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Finish: findBestFogOSBroker()");

        if (rtt1 <= rtt2)
            return brokers.get(0);
        else
            return brokers.get(1);
    }

    class NetworkThread extends Thread{

        @Override
        public void run() {
            brokers = new LinkedList<FogOSBroker>();
            java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "The structure of brokers is initialized.");
            try {
                String requestBrokerListURL = "http://" + cloudName + ":" + cloudPort + "/brokers";
                java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Request URL: " + requestBrokerListURL);
                URL url = new URL(requestBrokerListURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "After openConnection: " + conn);
                //conn.setRequestMethod("GET");

                if(conn.getResponseCode() == 200){
                    InputStream stream = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "utf-8"));
                    StringBuilder builder = new StringBuilder();
                    String line;

                    while((line = reader.readLine()) != null){
                        builder.append(line);
                    }

                    JSONObject object = new JSONObject(builder.toString());
                    if(object.length() != 0){
                        JSONArray array = object.getJSONArray("brokers");

                        for(int i = 0; i < array.length(); i++){
                            String name = array.getJSONObject(i).getString("name");
                            brokers.add(new FogOSBroker(name));
                            java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "The broker is added: " + name);
                        }
                    }
                }
            } catch (MalformedURLException | ProtocolException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                brokers.add(new FogOSBroker("www.versatile-broker-1.com"));
                brokers.add(new FogOSBroker("www.versatile-broker-2.com"));
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    Double getPingStats(String result){
        Double rtt = 1000.0;

        if(result.contains("unknown host") || result.contains("100% packet loss")){

        } else if(result.contains("% packet loss")){
            int start = result.indexOf("rtt min/avg/max/mdev");
            int end = result.indexOf("ms", start);
            String stats = result.substring(start + 23, end);
            rtt = Double.parseDouble(stats.split("/")[1]);
        } else{

        }

        return rtt;
    }

    public FlexID getDeviceID() {
        return deviceID;
    }

    // Initialize subscriptions with the selected broker
    void initSubscribe(FlexID deviceID) {
        Logger.getLogger(TAG).log(Level.INFO, "Start: initSubscribe()");

        subscribe(MessageType.JOIN_ACK.getTopicWithDeviceID(deviceID));
        subscribe(MessageType.LEAVE_ACK.getTopicWithDeviceID(deviceID));
        subscribe(MessageType.STATUS_ACK.getTopicWithDeviceID(deviceID));
        subscribe(MessageType.REGISTER_ACK.getTopicWithDeviceID(deviceID));
        subscribe(MessageType.UPDATE_ACK.getTopicWithDeviceID(deviceID));
        subscribe(MessageType.MAP_UPDATE_ACK.getTopicWithDeviceID(deviceID));

        Logger.getLogger(TAG).log(Level.INFO, "Finish: initSubscribe()");
    }

    // Generate the Edge Utilization Messages for an application
    public Message generateMessage(MessageType messageType) {
        Message msg = null;
        switch (messageType) {
            case REGISTER:
                msg = new RegisterMessage(deviceID);
                break;
            case UPDATE:
                break;
            case MAP_UPDATE:
                break;
            case QUERY:
                msg = new QueryMessage(deviceID);
                break;
            case REPLY:
                msg = new ReplyMessage(deviceID);
                break;
            case REQUEST:
                msg = new RequestMessage(deviceID);
                break;
            case RESPONSE:
                msg = new ResponseMessage(deviceID);
                break;
        }

        return msg;
    }

    public void connect(FlexID deviceID){
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Start: connect()");
        try {
            java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "broker.getName(): " + broker.getName() + " / broker.getPort(): " + broker.getPort() + " / deviceID.getStringIdentity(): " + deviceID.getStringIdentity());
            mqttClient = new MqttClient("tcp://" + broker.getName() + ":" + broker.getPort(), deviceID.getStringIdentity(), new MemoryPersistence());
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(true);
            mqttConnectOptions.setKeepAliveInterval(15);
            mqttConnectOptions.setConnectionTimeout(30);
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    Logger.getLogger(TAG).log(Level.INFO, "Mqtt: connectionLost");
                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    Logger.getLogger(TAG).log(Level.INFO, "Mqtt: messageArrived");

                    if (s.startsWith(MessageType.JOIN_ACK.getTopic())) {

                    } else if (s.startsWith(MessageType.LEAVE_ACK.getTopic())) {

                    } else if (s.startsWith(MessageType.MAP_UPDATE_ACK.getTopic())) {

                    } else if (s.startsWith(MessageType.REGISTER_ACK.getTopic())) {

                    } else if (s.startsWith(MessageType.STATUS_ACK.getTopic())) {

                    } else if (s.startsWith(MessageType.REPLY.getTopic())) {

                    } else if (s.startsWith(MessageType.RESPONSE.getTopic())) {

                    } else if (s.startsWith(MessageType.UPDATE_ACK.getTopic())) {

                    } else {
                        // No recognized message.
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    Logger.getLogger(TAG).log(Level.INFO, "Mqtt: deliveryComplete");
                }
            });

            mqttClient.connect(mqttConnectOptions);
            broker.setMqttClient(mqttClient);
        } catch (MqttSecurityException ex) {
            ex.printStackTrace();
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Finish: connect()");
    }

    public void disconnect(){
        try {
            mqttClient.disconnect();
            Logger.getLogger(TAG).log(Level.INFO, "Mqtt: disconnect()");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic){
        try {
            mqttClient.subscribe(topic, new IMqttMessageListener() {
                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    Logger.getLogger(TAG).log(Level.INFO, "Mqtt: messageArrived()");
                    Logger.getLogger(TAG).log(Level.INFO, "Mqtt: " + s + mqttMessage);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public Message getReceivedMessage(String topic) {
        return receivedMessages.get(topic).poll();
    }

    public void sendMessage(Message msg) {
        if (msg.getMessageType() == MessageType.QUERY) {
            qosInterpreter.checkQueryMessage((QueryMessage) msg);
        } else if (msg.getMessageType() == MessageType.REQUEST) {
            qosInterpreter.checkRequestMessage((RequestMessage) msg);
        }
        msg.send(broker);
    }

    public void testMessage(Message msg) {
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Start: testMessage(): MessageType: " + msg.getMessageType().toString());

        /* Test Returns */
        if (msg.getMessageType() == MessageType.QUERY && msg.getValueByAttr("keywords").equals("test")) {
            java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Make a test list started.");
            ReplyMessage replyMessage;
            FlexID id;
            replyMessage = (ReplyMessage) generateMessage(MessageType.REPLY);
            id = new FlexID("test");
            java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "id 1: " + id + " / ID 1: " + new String(id.getIdentity()));
            replyMessage.addReplyEntry("환호하는 손흥민", "손흥민이 한독전에서 골을 넣고 환호하고 있다.", id);
            id = new FlexID("test");
            java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "id 2: " + id + " / ID 2: " + new String(id.getIdentity()));
            replyMessage.addReplyEntry("기뻐하는 김영권 영상", "온국민을 환호하게 만든 김영권의 첫골을 다시보자.", id);
            id = new FlexID("test");
            java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "id 3: " + id + " / ID 3: " + new String(id.getIdentity()));
            replyMessage.addReplyEntry("좌절에 빠진 독일 팬들", "예기치 못한 패배에 독일 팬들은 모두 울상을 짓고 있다.", id);
            receivedMessages.get(MessageType.REPLY.getTopic()).add(replyMessage);
            java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Make a test list finished.");
            java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Finish: testMessage()");
        } else if (msg.getMessageType() == MessageType.REQUEST) {
            RequestMessage requestMessage;
            requestMessage = (RequestMessage) msg;
            java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Make a test response message started.");
            java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Peer ID: " + new String(requestMessage.getPeerID().getIdentity()));
            ResponseMessage responseMessage;
            Locator locator = new Locator(InterfaceType.WIFI, "192.168.0.128", 3333);
            responseMessage = (ResponseMessage) generateMessage(MessageType.RESPONSE);
            responseMessage.setPeerID(new FlexID(msg.getValueByAttr("id")));
            responseMessage.getPeerID().setLocator(locator);
            receivedMessages.get(MessageType.RESPONSE.getTopic()).add(responseMessage);
            java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Make a test response message finished.");
        }
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Finish: sendMessage()");
    }

    public SecureFlexIDSession createSecureFlexIDSession(Role role, FlexID sFID, FlexID dFID)
    {
        SecureFlexIDSession secureFlexIDSession = new SecureFlexIDSession(role, sFID, dFID);
        sessionList.add(secureFlexIDSession);
        return secureFlexIDSession;
    }

    public void destroySecureFlexIDSession(SecureFlexIDSession secureFlexIDSession)
    {
        secureFlexIDSession.getFlexIDSession().close();
        sessionList.remove(secureFlexIDSession);
    }
}
