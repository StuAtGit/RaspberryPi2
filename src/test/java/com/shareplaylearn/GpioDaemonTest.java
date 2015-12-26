package com.shareplaylearn;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;

/**
 * Unit test for simple Gpio.
 */
public class GpioDaemonTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public GpioDaemonTest(String testName) throws MqttException {
        super(testName);
        ArrayList<String> mqttBrokers = new ArrayList<String>();
        String gpioDaemonTestUser = "";
        String gpioDaemonTestPassword = "";
        String gpioDaemonTestTopic = "";
        int maxInvalid = 1;
        boolean mockGpio = true;
        String[] type = new String[mqttBrokers.size()];
        GpioDaemon gpioDaemon = new GpioDaemon( mqttBrokers.toArray(type), "Gpio Daemon Unit Test",
                gpioDaemonTestUser, gpioDaemonTestPassword.toCharArray(), gpioDaemonTestTopic,
                maxInvalid, mockGpio);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( GpioDaemonTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
}
