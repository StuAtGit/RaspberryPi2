/**
 * Copyright 2015-2016 Stuart Smith
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with the paho MQTT client library (or a modified version of that library),
 * containing parts covered by the terms of EPL,
 * the licensors of this Program grant you additional permission to convey the resulting work.
 *
 */
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

        this.gpioDaemonTestUser = SecretsService.testDaemonUsername;
        this.gpioDaemonTestPassword = SecretsService.testDaemonPassword;
        this.gpioDaemonTestRequestTopic = "lightswitch";
        this.gpioDaemonTestResponseTopic = "lightswitchResponse";
        mqttBrokers.add("ssl://www.shareplaylearn.com:8883");

        int maxInvalid = 1;
        boolean mockGpio = true;
        this.brokerListType = new String[mqttBrokers.size()];
        gpioDaemon = new GpioDaemon( mqttBrokers.toArray(brokerListType), "UnitTestDaemonRequest",
                "UnitTestDaemonResponse", gpioDaemonTestUser, gpioDaemonTestPassword.toCharArray(),
                gpioDaemonTestRequestTopic, gpioDaemonTestResponseTopic,
                maxInvalid, mockGpio);
    }

    @Test
    public void testApp() throws MqttException, InterruptedException {
        gpioDaemon.connectToBroker();
        MqttClient mqttClient = new MqttClient( mqttBrokers.toArray(this.brokerListType)[0], "UnitTestClient" );
        MqttClient mqttResponseClient = new MqttClient( mqttBrokers.toArray(this.brokerListType)[0], "UnitTestResponseClient" );
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();

        mqttConnectOptions.setUserName(SecretsService.testClientId);
        mqttConnectOptions.setPassword(SecretsService.testClientSecret.toCharArray());
        mqttConnectOptions.setCleanSession(true);
        mqttClient.connect(mqttConnectOptions);

        mqttResponseClient.connect(mqttConnectOptions);
        mqttResponseClient.setCallback( new MqttMesssageResponseTestHandler() );
        try {
            System.out.println("Subscribing to: " + this.gpioDaemonTestResponseTopic);
            mqttResponseClient.subscribe(this.gpioDaemonTestResponseTopic, 1);
        } catch( Throwable t ) {
            System.out.println( t.getMessage() + "\n" + t.getCause() + "\n" + Exceptions.traceToString(t));
        }

        mqttClient.publish(this.gpioDaemonTestRequestTopic, "TestMessage".getBytes(StandardCharsets.UTF_8), 1, false);
        Thread.sleep(1000);
        gpioDaemon.disconnect();
        System.out.println("Test message published.");
    }
}
