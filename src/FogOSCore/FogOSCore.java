package FogOSCore;

import FogOSMessage.*;
import FlexID.FlexID;
import FlexID.FlexIDFactory;
import FlexID.Locator;
import FlexID.InterfaceType;
import FogOSQoS.QoSInterpreter;
import FogOSResource.MobilityDetector;
import FogOSResource.Resource;
import FogOSResource.ResourceReporter;
import FogOSResource.ResourceType;
import FogOSSecurity.Role;
import FogOSSecurity.SecureFlexIDSession;
import FogOSService.Service;
import FogOSContent.Content;
import FogOSService.ServiceRunner;
import FogOSStore.ContentStore;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.*;

import java.io.*;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FogOSCore {
    // private final String cloudName = "www.versatile-cloud.com";
    private final String cloudName = "147.46.114.86";
    private final int cloudPort = 3333;
    private final String DEFAULT_CONTENT_STORE_PATH = "";
    private LinkedList<FogOSBroker> brokers;
    private FogOSBroker broker;
    private FlexIDFactory factory;
    private FlexID deviceID;
    private ContentStore contentStore;
    private ArrayList<Resource> resourceList;
    private ArrayList<Service> serviceList;
    private MqttClient mqttClient;
    private HashMap<String, Queue<Message>> receivedMessages;

    // Session and Mobility-related
    private LinkedList<SecureFlexIDSession> sessionList;

    // A thread that periodically detects any change of a locator (e.g., IP address)
    private Runnable mobilityDetector;

    // A thread that periodically reports a status of resources in the device
    private Runnable resourceReporter;

    // An instance that runs services
    private ServiceRunner serviceRunner;

    // QoS Interpreter
    private QoSInterpreter qosInterpreter;

    private static final String TAG = "FogOSCore";

    public FogOSCore() {
        this.contentStore = new ContentStore(DEFAULT_CONTENT_STORE_PATH);
        init();
    }

    public FogOSCore(ContentStore contentStore, ArrayList<Service> serviceList,
                     ArrayList<Resource> resourceList) {
        this.contentStore = contentStore;
        this.serviceList = serviceList;
        this.resourceList = resourceList;
        init();
    }

    private void init() {
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Start: Initialize FogOSCore");

        retrieveBrokerList();
        broker = findBestFogOSBroker();
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Result: findBestFogOSBroker() " + broker.getName());

        sessionList = new LinkedList<>();

        // Initialize and run the mobility detector
        mobilityDetector = new MobilityDetector(this);
        new Thread(mobilityDetector).start();

        // Initilaize the resource reporter
        resourceReporter = new ResourceReporter(this);
        new Thread(resourceReporter).start();

        // TODO: Initialize the service runner
        serviceRunner = new ServiceRunner(this);
        new Thread(serviceRunner).start();

        // Initialize and run the QoS interpreter
        qosInterpreter = new QoSInterpreter(this);

        // Generate the Flex ID of the device
        factory = new FlexIDFactory();
        deviceID = factory.generateDeviceID();

        // Initialize the MQTT client
        connect(deviceID);

        // Initialize the subscription to necessary messages
        initSubscribe(deviceID);

        // Send the JOIN message
        join();

        // Send REGISTER message
        register();

        try {

        } catch (Exception e) {
            e.printStackTrace();
        }

        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Finish: Initialize FogOSCore");
    }

    public String getLocalIpAddr() {
        String ip = "";

        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();

                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return ip;
    }

    private static String getMacAddress(String ip) {
        String mac = "";

        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();

                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        if (ip.equals(inetAddress.getHostAddress())) {
                            byte[] hardwareAddress = networkInterface.getHardwareAddress();

                            String[] hexadecimal = new String[hardwareAddress.length];
                            for (int i = 0; i < hardwareAddress.length; i++) {
                                hexadecimal[i] = String.format("%02X", hardwareAddress[i]);
                            }
                            mac = String.join("-", hexadecimal);
                            return mac;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return mac;
    }

    public void join() {

        // Add Ethernet interface
        Resource ifaceTypeResource = new Resource("ifaceType", ResourceType.NetworkInterface,
                "", "Ethernet", false) {
            @Override
            public void monitorResource() {
                this.setCurr(Integer.toString(Integer.parseInt(this.getCurr()) + 1));
                System.out.println("[Ethernet] " + this.getCurr() + " " + this.getUnit());
            }
        };
        resourceList.add(ifaceTypeResource);

        // find IPv4 address of the device
        String ipv4 = getLocalIpAddr();
        Resource ipResource = new Resource("ipv4", ResourceType.NetworkInterface, "", ipv4, false) {
            @Override
            public void monitorResource() {
                this.setCurr(Integer.toString(Integer.parseInt(this.getCurr()) + 1));
                System.out.println("[IPv4] " + this.getCurr() + " " + this.getUnit());
            }
        };
        resourceList.add(ipResource);

        // find HW address of the device
        String hwAddress = getMacAddress(ipv4);
        Resource hwAddrResource = new Resource("hwAddress", ResourceType.NetworkInterface, "", hwAddress, false) {
            @Override
            public void monitorResource() {
                this.setCurr(Integer.toString(Integer.parseInt(this.getCurr()) + 1));
                System.out.println("[MAC] " + this.getCurr() + " " + this.getUnit());
            }
        };
        resourceList.add(hwAddrResource);

        Message msg = new JoinMessage(deviceID, resourceList, deviceID.getPub());
        msg.send(broker);
        //msg.test(broker); // This should be commented out after being generalized.
    }

    public void register() {
        // TODO: Need to generalize the message
        Message rmsg = new RegisterMessage(deviceID, this.contentStore);
        rmsg.test(broker); // This should be commented out after being generalized.
    }

    public LinkedList<SecureFlexIDSession> getSessionList() {
        return sessionList;
    }

    public QoSInterpreter getQosInterpreter() {
        return qosInterpreter;
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
    FogOSBroker findBestFogOSBroker() {
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Start: findBestFogOSBroker()");

        Double rtt1 = 0.0;
        Double rtt2 = 0.0;
        try {
            Process process = Runtime.getRuntime().exec("/system/bin/ping -c 3 " + brokers.get(0).getName());
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            String result1 = builder.toString();

            process = Runtime.getRuntime().exec("/system/bin/ping -c 3 " + brokers.get(1).getName());
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            builder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            String result2 = builder.toString();

            rtt1 = getPingStats(result1);
            rtt2 = getPingStats(result2);
        } catch (IOException e) {
            e.printStackTrace();
            rtt1 = 0.0; // test code for the Java application
            rtt2 = 1.0; // test code for the Java application
        }

        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Finish: findBestFogOSBroker()");

        if (rtt1 <= rtt2)
            return brokers.get(0);
        else
            return brokers.get(1);
    }

    class NetworkThread extends Thread {

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

                if (conn.getResponseCode() == 200) {
                    InputStream stream = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "utf-8"));
                    StringBuilder builder = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }

                    JSONObject object = new JSONObject(builder.toString());
                    if (object.length() != 0) {
                        JSONArray array = object.getJSONArray("brokers");

                        for (int i = 0; i < array.length(); i++) {
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

    Double getPingStats(String result) {
        Double rtt = 1000.0;

        if (result.contains("unknown host") || result.contains("100% packet loss")) {

        } else if (result.contains("% packet loss")) {
            int start = result.indexOf("rtt min/avg/max/mdev");
            int end = result.indexOf("ms", start);
            String stats = result.substring(start + 23, end);
            rtt = Double.parseDouble(stats.split("/")[1]);
        } else {

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

    public void connect(FlexID deviceID) {
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Start: connect()");
        try {
            java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "broker.getName(): " + broker.getName() + " / broker.getPort(): " + broker.getPort() + " / deviceID.getStringIdentity(): " + deviceID.getStringIdentity());
            //mqttClient = new MqttClient("tcp://" + broker.getName() + ":" + broker.getPort(), deviceID.getStringIdentity(), new MemoryPersistence());
            mqttClient = new MqttClient("tcp://147.46.114.86:" + broker.getPort(), deviceID.getStringIdentity(), new MemoryPersistence());

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

    public void disconnect() {
        try {
            mqttClient.disconnect();
            Logger.getLogger(TAG).log(Level.INFO, "Mqtt: disconnect()");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic) {
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
        if (msg.getMessageType() == MessageType.QUERY && msg.getValueByAttr("keywords").equals("public")) {
            java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Make a test list started.");
            ReplyMessage replyMessage;
            FlexID id;
            replyMessage = (ReplyMessage) generateMessage(MessageType.REPLY);
            id = new FlexID("0x950FE925AA360933FCD2");
            java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "id 1: " + id + " / ID 1: " + new String(id.getIdentity()));
            replyMessage.addReplyEntry("대중교통 공익광고", "대중교통을 이용합시다!", id);
            id = new FlexID("public");
            java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "id 2: " + id + " / ID 2: " + new String(id.getIdentity()));
            replyMessage.addReplyEntry("금연 실천 비디오", "모두의 건강을 지키는 것이 공익입니다", id);
            id = new FlexID("test");
            java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "id 3: " + id + " / ID 3: " + new String(id.getIdentity()));
            replyMessage.addReplyEntry("공익 실천 프로젝트", "너도 나도 함께하는 자그마한 공익 실천!", id);
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
            responseMessage.setPeerID(new FlexID(msg.getValueByAttr("id").getValue()));
            responseMessage.getPeerID().setLocator(locator);
            receivedMessages.get(MessageType.RESPONSE.getTopic()).add(responseMessage);
            java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Make a test response message finished.");
        }
        java.util.logging.Logger.getLogger(TAG).log(Level.INFO, "Finish: sendMessage()");
    }

    public SecureFlexIDSession createSecureFlexIDSession(Role role, FlexID sFID, FlexID dFID) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        SecureFlexIDSession secureFlexIDSession = new SecureFlexIDSession(role, sFID, dFID);
        sessionList.add(secureFlexIDSession);
        return secureFlexIDSession;
    }

    public Content[] getContentList() {
        return contentStore.getContentList();
    }
    public ArrayList<Service> getServiceList() { return serviceList; }
    public ArrayList<Resource> getResourceList() {
        return resourceList;
    }
    
    public void ContentUpdate() {
    	contentStore.ContentUpdate();
    }

    public void destroySecureFlexIDSession(SecureFlexIDSession secureFlexIDSession) {
        secureFlexIDSession.getFlexIDSession().close();
        sessionList.remove(secureFlexIDSession);
    }

    public void deregister(FlexID[] flexIDList) {
        boolean deregisterFlag = true;
        Message msg = new UpdateMessage(deviceID, flexIDList, deregisterFlag);
        msg.send(broker);
    }

    public void leave(FlexID flexID) {
        Message msg = new LeaveMessage(deviceID, flexID);
        msg.send(broker);
    }
}
