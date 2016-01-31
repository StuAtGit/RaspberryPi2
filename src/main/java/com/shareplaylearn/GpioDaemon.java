package com.shareplaylearn;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by stu on 11/8/15.
 */
public class GpioDaemon
{
    private Gpio gpio;
    private CommandTranslator commandTranslator;
    public MqttClient mqttClient;
    public MqttClient responseClient;
    public boolean isRunning;
    private String requestTopic;
    private String responseTopic;
    private String[] brokerList;
    private static Logger log = LoggerFactory.getLogger(GpioDaemon.class);
    //if we get more than maxInvalid message IN A ROW, sleep & reconnect.
    private int maxInvalid;
    private String username;
    private char[] password;

    public static void main( String[] args ) throws MqttException, InterruptedException {
        //TODO: command line arguments & test
        ArrayList<String> brokers = new ArrayList<String>();
        brokers.add("ssl://www.shareplaylearn.com:8883");
        String[] brokerList = brokers.toArray(new String[brokers.size()]);
        String requestClientName = "ClientId-Gpio-Daemon";
        String responseClientName = requestClientName + "-ResponseClient";
        String username = SecretsService.testDaemonUsername;
        char[] password = SecretsService.testDaemonPassword.toCharArray();
        String requestTopic = "lightswitch";
        String responseTopic = "lightswitchResponse";
        int maxInvalid = 50;
        boolean mockGpio = true;
        long sleepTime = 1000 * 1;
        log.info("Gpio daemon listener starting up...");
        GpioDaemon gpioDaemon = new GpioDaemon( brokerList, requestClientName, responseClientName,
                username, password, requestTopic, responseTopic, maxInvalid, mockGpio );
        try {
            gpioDaemon.connectToBroker();
        } catch (MqttException e) {
            log.error("Failed to connect to mqtt broker " + e.getMessage());
            log.error(Exceptions.traceToString(e));
        }
        while( gpioDaemon.isRunning ) {
            Thread.sleep(sleepTime);
        }
        try {
            gpioDaemon.disconnect();
        } catch (MqttException e) {
            log.error("Error disconnecting: " + e.getMessage());
            log.error(Exceptions.traceToString(e));
        }
    }

    public GpioDaemon( String[] brokerList, String requestClientName, String responseClientName,
                       String username, char[] password, String requestTopic,
                       String responseTopic, int maxInvalid,
                       boolean mockGpio ) throws MqttException {

        if( requestTopic.equals(responseTopic) ) {
            throw new IllegalArgumentException("Request topic cannot be the same as response topic");
        }
        if( requestClientName.equals(responseClientName) ) {
            throw new IllegalArgumentException("Request client name cannot be the same as the response client name");
        }
        this.gpio = new Gpio( mockGpio );
        this.commandTranslator = new CommandTranslator( this.gpio );
        if( brokerList == null || brokerList.length < 1 ) {
            throw new IllegalArgumentException("Invalid broker list, must list at least 1 mqtt broker.");
        }
        if( requestClientName == null || requestClientName.length() == 0 ) {
            throw new IllegalArgumentException("Invalid client name: " + requestClientName);
        }
        log.info(requestClientName + " connecting to mqtt broker.");
        this.mqttClient = new MqttClient( brokerList[0], requestClientName, new MemoryPersistence() );
        this.responseClient = new MqttClient( brokerList[0], responseClientName, new MemoryPersistence() );
        this.brokerList = brokerList;
        this.requestTopic = requestTopic;
        this.responseTopic = responseTopic;
        this.username = username;
        this.password = password;
        this.maxInvalid = maxInvalid;
        this.isRunning = true;
    }

    public void connectToBroker() throws MqttException {
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setServerURIs(this.brokerList);
        connectOptions.setCleanSession(true);
        connectOptions.setUserName(this.username);
        connectOptions.setPassword(this.password);
        connectOptions.setKeepAliveInterval(5);
        connectOptions.setConnectionTimeout(600);
        this.mqttClient.connect(connectOptions);
        this.responseClient.connect(connectOptions);
        this.log.info("Connected to mqtt broker: " + this.mqttClient.getServerURI() + " as: " + this.username );
        this.mqttClient.setCallback( new MqttMessageHandler(this, this.responseClient, this.responseTopic, this.maxInvalid, this.gpio) );
        this.log.info("Subscribing to requestTopic: " + this.requestTopic);
        this.mqttClient.subscribe(this.requestTopic,1);
        this.log.info("Subscribed to requestTopic: " + this.requestTopic);
    }

    public void disconnect() throws MqttException {
        this.mqttClient.unsubscribe(this.requestTopic);
        this.mqttClient.disconnect();
    }

    public void reconnect( long sleepTime ) {
        try {
            this.disconnect();
        } catch( MqttException e ) {
            this.log.error( "Error disconnecting MQTT client: " + e.getMessage());
            this.log.error( Exceptions.traceToString(e) );
        }
        try {
            Thread.sleep(sleepTime);
        } catch( InterruptedException e ) {
            this.log.error("Interrupted while sleeping prior to reconnect: " + e.getMessage());
            this.log.error( Exceptions.traceToString(e));
            Thread.currentThread().interrupt();
        }
        try {
            this.connectToBroker();
        } catch( MqttException e ) {
            this.log.error("Error reconnecting to mqtt broker: " + e.getMessage() );
            this.log.error( Exceptions.traceToString(e) );
            this.log.error("Shutting down Gpio Daemon");
            this.isRunning = false;
        }
    }
}
