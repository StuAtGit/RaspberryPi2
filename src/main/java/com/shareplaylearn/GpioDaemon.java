package com.shareplaylearn;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private String username;
    private char[] password;

    public GpioDaemon( String[] brokerList, String clientName, String username,
                       char[] password, String topic, int maxInvalid, boolean mockGpio ) throws MqttException {
        this.gpio = new Gpio( mockGpio );
        this.commandTranslator = new CommandTranslator( this.gpio );
        if( brokerList == null || brokerList.length < 1 ) {
            throw new IllegalArgumentException("Invalid broker list, must list at least 1 mqtt broker.");
        }
        if( clientName == null || clientName.length() == 0 ) {
            throw new IllegalArgumentException("Invalid client name: " + clientName);
        }
        this.mqttClient = new MqttClient( brokerList[0], clientName, new MemoryPersistence() );
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        this.brokerList = brokerList;
        this.topic = topic;
        this.username = username;
        this.password = password;
        connectOptions.setServerURIs(this.brokerList);
        connectOptions.setCleanSession(true);
        connectOptions.setUserName(this.username);
        connectOptions.setPassword(this.password);
        this.mqttClient.connect(connectOptions);
        this.log.info("Connected to mqtt broker: " + this.mqttClient.getServerURI() );
        this.mqttClient.setCallback(this);
        this.mqttClient.subscribe(this.topic,1);
        this.log.info("Subscribed to topic: " + this.topic);
        this.invalidRepeatCounter = 0;
        this.isRunning = true;
    }

    public void run() {
        while( this.mqttClient.isConnected() && this.isRunning ) {

        }
        try {
            this.mqttClient.disconnect();
        } catch (MqttException e) {
            this.log.error("Error disconnecting: " + e.getMessage());
            this.log.error(Exceptions.traceToString(e));
        }
    }

    public static void main( String[] args ) {

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
            return;
        } else if( result.type == CommandTranslator.ResultType.PIN ) {
            if( result.setPinHigh ) {
                this.gpio.setHigh(result.pin);
                String response = "Set pin: " + result.pin.toString() + " to high.";
                this.mqttClient.publish(topic, response.getBytes(UTF_8), 1, false);
                this.invalidRepeatCounter = 0;
                return;
            } else {
                //set low.. need this in gpio.
                this.invalidRepeatCounter = 0;
                //return;
            }
        } //Reconnect, Shutdown, etc (Restart would be kind of cool, but we'll see)
        this.mqttClient.publish(topic, "Unknown issue processing command".getBytes(UTF_8), 1, false);
    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
