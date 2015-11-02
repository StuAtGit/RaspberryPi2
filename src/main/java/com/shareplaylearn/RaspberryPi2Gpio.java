package com.shareplaylearn;

import com.pi4j.io.gpio.*;
import com.sun.org.apache.xpath.internal.SourceTree;

/**
 * Hello world!
 *
 */
public class RaspberryPi2Gpio
{
    public static void main( String[] args ) throws InterruptedException {
        final GpioController gpio = GpioFactory.getInstance();
        System.out.println( "Setting Pin 16 to low");
        final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin( RaspiPin.GPIO_16, "pin16", PinState.LOW);
        pin.setShutdownOptions(true, PinState.LOW);
        Thread.sleep(10000);
        System.out.println("Setting pin 16 to high");
        pin.high();
        Thread.sleep(10000);
        System.out.println("shutting down");
        gpio.shutdown();
    }
}
