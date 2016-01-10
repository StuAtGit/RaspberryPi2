package com.shareplaylearn;

import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by stu on 1/9/16.
 */
public class MqttMessageHandler
    implements MqttCallback {

    private GpioDaemon gpioDaemon;
    private String responseTopic;
    private int invalidRepeatCounter;
    private int maxInvalid;
    private Gpio gpio;
    private CommandTranslator commandTranslator;
    private Logger log;

    public MqttMessageHandler( GpioDaemon gpioDaemon, String responseTopic,
                               int maxInvalid, Gpio gpio ) {
        this.gpioDaemon = gpioDaemon;
        this.responseTopic = responseTopic;
        this.invalidRepeatCounter = 0;
        this.maxInvalid = maxInvalid;
        this.gpio = gpio;
        this.commandTranslator = new CommandTranslator(gpio);
        this.log = LoggerFactory.getLogger( MqttMessageHandler.class );
    }

    public void connectionLost(Throwable throwable) {
        this.log.error("Lost connection to the mqtt broker: " + throwable.getMessage() );
        this.log.error(Exceptions.traceToString(throwable));
    }

    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        String message = new String( mqttMessage.getPayload(), UTF_8 );
        this.log.debug("Received message from broker: " +  message);
        CommandTranslator.Result result = commandTranslator.translateMessage(message);
        if( result.type == CommandTranslator.ResultType.INVALID_COMMAND ) {
            this.log.info("Invalid command send to client: " + message + " sending: " + result.response );
            //TODO: this will block
            //this.gpioDaemon.mqttClient.publish(responseTopic, result.response.getBytes(UTF_8), 1, false);
            this.invalidRepeatCounter++;
            if( this.invalidRepeatCounter > this.maxInvalid ) {
                this.log.warn("Max invalid messages received, resetting connection.");
                this.gpioDaemon.reconnect(1000);
            }
            this.log.debug("Done with message processing.");
            return;
        } else if( result.type == CommandTranslator.ResultType.PIN ) {
            if( result.setPinHigh ) {
                this.gpio.setHigh(result.pin);
                String response = "Set pin: " + result.pin.toString() + " to high.";
                //this.gpioDaemon.mqttClient.publish(responseTopic, response.getBytes(UTF_8), 1, false);
                this.invalidRepeatCounter = 0;
                return;
            } else {
                this.gpio.setLow(result.pin);
                String response = "Set pin: " + result.pin.toString() + " to low.";
                //this.gpioDaemon.mqttClient.publish(responseTopic, response.getBytes(UTF_8), 1, false);
                this.invalidRepeatCounter = 0;
                return;
            }
        } else if( result.type == CommandTranslator.ResultType.DAEMON_COMMAND ) {
            //Reconnect, Shutdown, etc (Restart would be kind of cool, but we'll see)
            if( result.reconnect ) {
                //this.reconnect(result.millisecondsBeforeReconnect);
                this.invalidRepeatCounter = 0;
            }
        }
        //this.gpioDaemon.mqttClient.publish(responseTopic, "Unknown issue processing command".getBytes(UTF_8), 1, false);
    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        this.log.info("Delivery complete");
    }
}
