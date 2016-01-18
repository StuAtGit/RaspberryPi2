package com.shareplaylearn;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 *
 */
public class GpioDaemonTest
{
    String gpioDaemonTestUser = "";
    String gpioDaemonTestPassword = "";
    String gpioDaemonTestRequestTopic = "";
    String gpioDaemonTestResponseTopic = "";
    ArrayList<String> mqttBrokers = new ArrayList<String>();
    String[] brokerListType;
    GpioDaemon gpioDaemon;

    public GpioDaemonTest() throws MqttException {

        this.gpioDaemonTestUser = SecretsService.testStormpathUsername;
        this.gpioDaemonTestPassword = SecretsService.testStormpathPassword;
        this.gpioDaemonTestRequestTopic = "lightswitch";
        this.gpioDaemonTestResponseTopic = "lightswitchResponse";
        mqttBrokers.add("ssl://www.shareplaylearn.com:8883");

        int maxInvalid = 1;
        boolean mockGpio = true;
        this.brokerListType = new String[mqttBrokers.size()];
        gpioDaemon = new GpioDaemon( mqttBrokers.toArray(brokerListType), "Gpio Daemon Unit Test",
                gpioDaemonTestUser, gpioDaemonTestPassword.toCharArray(), gpioDaemonTestRequestTopic,
                gpioDaemonTestResponseTopic,
                maxInvalid, mockGpio);
    }

    @Test
    public void testApp() throws MqttException, InterruptedException {
        gpioDaemon.connectToBroker();
        MqttClient mqttClient = new MqttClient( mqttBrokers.toArray(this.brokerListType)[0], "UnitTestClient" );
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setUserName(this.gpioDaemonTestUser);
        mqttConnectOptions.setPassword(this.gpioDaemonTestPassword.toCharArray());
        mqttConnectOptions.setCleanSession(true);
        mqttClient.connect(mqttConnectOptions);
        mqttClient.publish(this.gpioDaemonTestRequestTopic, "TestMessage".getBytes(StandardCharsets.UTF_8), 1, false);
        Thread.sleep(3000);
        mqttClient.disconnect();
        gpioDaemon.disconnect();
    }
}
