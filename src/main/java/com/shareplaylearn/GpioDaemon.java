package com.shareplaylearn;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.*;

import static java.nio.charset.StandardCharsets.*;

/**
 * Created by stu on 11/8/15.
 */
public class GpioDaemon
    implements Runnable, MqttCallback
{
    private Gpio gpio;
    private CommandTranslator commandTranslator;
    private MqttClient mqttClient;
    private boolean isRunning;
    private String topic;
    private String[] brokerList;
    private static Logger log = LoggerFactory.getLogger(GpioDaemon.class);
    //if we get more than maxInvalid message IN A ROW, sleep & reconnect.
    private int maxInvalid;
    private int invalidRepeatCounter;
    private long sleepTime;
    private String username;
    private char[] password;

    public static void main( String[] args ) throws MqttException {
        //TODO: command line arguments & test
        ArrayList<String> brokers = new ArrayList<String>();
        brokers.add("ssl://www.shareplaylearn.com:8883");
        String[] brokerList = brokers.toArray(new String[brokers.size()]);
        String clientName = "ClientId-Gpio-Daemon";
        String username = SecretsService.testStormpathUsername;
        char[] password = SecretsService.testStormpathPassword.toCharArray();
        String topic = "lightswitch";
        int maxInvalid = 50;
        boolean mockGpio = true;
        long sleepTime = 1000 * 3;
        ScheduledExecutorService gpioExecutor = null;
        //while( true ) {
            try {
                Thread gpioThread = new Thread(new GpioDaemon(brokerList, clientName, username,
                        password, topic, maxInvalid, sleepTime, mockGpio));
                gpioThread.start();
                gpioThread.join();

            } catch( InterruptedException e ) {
                log.error("Execution of gpio daemon was interrupted: " + e.getMessage());
                log.error( Exceptions.traceToString(e) );
                Thread.currentThread().interrupt();
                gpioExecutor.shutdown();
                //break;
            }
        //}

    }

    public GpioDaemon( String[] brokerList, String clientName, String username,
                       char[] password, String topic, int maxInvalid, long sleepTime,
                       boolean mockGpio ) throws MqttException {
        this.gpio = new Gpio( mockGpio );
        this.sleepTime = sleepTime;
        this.commandTranslator = new CommandTranslator( this.gpio );
        if( brokerList == null || brokerList.length < 1 ) {
            throw new IllegalArgumentException("Invalid broker list, must list at least 1 mqtt broker.");
        }
        if( clientName == null || clientName.length() == 0 ) {
            throw new IllegalArgumentException("Invalid client name: " + clientName);
        }
        log.info(clientName + " connecting to mqtt broker.");
        this.mqttClient = new MqttClient( brokerList[0], clientName, new MemoryPersistence() );
        this.brokerList = brokerList;
        this.topic = topic;
        this.username = username;
        this.password = password;
        this.invalidRepeatCounter = 0;
        this.maxInvalid = maxInvalid;
        this.isRunning = true;
    }

    public void connectToBroker() throws MqttException {
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setServerURIs(this.brokerList);
        connectOptions.setCleanSession(true);
        connectOptions.setUserName(this.username);
        connectOptions.setPassword(this.password);
        this.mqttClient.connect(connectOptions);
        this.log.info("Connected to mqtt broker: " + this.mqttClient.getServerURI() + " as: " + this.username );
        this.mqttClient.setCallback(this);
        this.mqttClient.subscribe(this.topic,1);
        this.log.info("Subscribed to topic: " + this.topic);
    }

    public void disconnect() throws MqttException {
        this.mqttClient.unsubscribe(this.topic);
        this.mqttClient.disconnect();
    }

    public void run() {
        log.info("Gpio daemon listener starting up...");
        try {
            this.connectToBroker();
        } catch (MqttException e) {
            log.error("Failed to connect to mqtt broker " + e.getMessage());
            log.error(Exceptions.traceToString(e));
            return;
        }
        while( this.isRunning ) {
            try {
                Thread.sleep( this.sleepTime );
            } catch (InterruptedException e) {
                log.warn("Daemon thread was interrupted:\n" + Exceptions.traceToString(e));
                Thread.currentThread().interrupt();
                break;
            }
        }
        try {
            this.mqttClient.disconnect();
        } catch (MqttException e) {
            this.log.error("Error disconnecting: " + e.getMessage());
            this.log.error(Exceptions.traceToString(e));
        }
    }

    public void connectionLost(Throwable throwable) {
        this.log.error("Lost connection to the mqtt broker: " + throwable.getMessage() );
        this.log.error(Exceptions.traceToString(throwable));
        this.isRunning = false;
    }

    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String message = new String( mqttMessage.getPayload(), UTF_8 );
        this.log.debug("Received message from broker: " +  message);
        CommandTranslator.Result result = commandTranslator.translateMessage(message);
        if( result.type == CommandTranslator.ResultType.INVALID_COMMAND ) {
            this.log.info("Invalid command send to client: " + message + " sending: " + result.response );
            this.mqttClient.publish(topic, result.response.getBytes(UTF_8), 1, false);
            this.invalidRepeatCounter++;
            if( this.invalidRepeatCounter > this.maxInvalid ) {
                this.reconnect(1000);
            }
            return;
        } else if( result.type == CommandTranslator.ResultType.PIN ) {
            if( result.setPinHigh ) {
                this.gpio.setHigh(result.pin);
                String response = "Set pin: " + result.pin.toString() + " to high.";
                this.mqttClient.publish(topic, response.getBytes(UTF_8), 1, false);
                this.invalidRepeatCounter = 0;
                return;
            } else {
                this.gpio.setLow(result.pin);
                this.invalidRepeatCounter = 0;
                return;
            }
        } else if( result.type == CommandTranslator.ResultType.DAEMON_COMMAND ) {
            //Reconnect, Shutdown, etc (Restart would be kind of cool, but we'll see)
            if( result.reconnect ) {
                this.reconnect(result.millisecondsBeforeReconnect);
                this.invalidRepeatCounter = 0;
            }
        }
        this.mqttClient.publish(topic, "Unknown issue processing command".getBytes(UTF_8), 1, false);
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

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
