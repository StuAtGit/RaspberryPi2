/**
 * Copyright 2015-2016 Stuart Smith
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version. Or under the the Eclipse Public License v1.0
 * as published by the Eclipse Foundation or (per the licensee's choosing)
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with the paho MQTT client library (or a modified version of that library),
 * containing parts covered by the terms of EPL,
 * the licensors of this Program grant you additional permission to convey the resulting work.
 */
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
