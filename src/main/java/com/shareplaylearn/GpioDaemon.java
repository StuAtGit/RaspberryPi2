package com.shareplaylearn;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by stu on 11/8/15.
 */
public class GpioDaemon
    implements Runnable
{
    private Gpio gpio;
    private MqttClient mqttClient;
    private static Logger log = LoggerFactory.getLogger(GpioDaemon.class);

    public GpioDaemon( String broker, boolean mock ) throws MqttException {
        this.gpio = new Gpio( mock );
        this.mqttClient = new MqttClient( broker, Gpio.class.toString(), new MemoryPersistence() );
    }

    public void run() {

    }

    public static void main( String[] args ) {

    }
}
