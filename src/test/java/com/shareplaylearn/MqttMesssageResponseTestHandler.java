package com.shareplaylearn;

import junit.framework.Assert;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * Created by stu on 1/18/16.
 */
public class MqttMesssageResponseTestHandler
    implements MqttCallback
{
    private Logger log;
    private String verifyMessage;

    public MqttMesssageResponseTestHandler() {
        this.log = LoggerFactory.getLogger(MqttMesssageResponseTestHandler.class);
        this.verifyMessage = null;
    }

    public MqttMesssageResponseTestHandler( String verifyMessage ) {
        this.log = LoggerFactory.getLogger(MqttMesssageResponseTestHandler.class);
        this.verifyMessage = verifyMessage;
    }

    public void connectionLost(Throwable throwable) {
        this.log.error("Connection lost: " + throwable.getMessage());
        this.log.error(Exceptions.traceToString(throwable));
    }

    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        String message = new String( mqttMessage.getPayload(), StandardCharsets.UTF_8 );
        this.log.info("Got message in test client: " +  message);
        if( verifyMessage != null ) {
            Assert.assertEquals(message,verifyMessage);
        }
    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        this.log.info("Delivery complete message in test client handler.");
    }
}
