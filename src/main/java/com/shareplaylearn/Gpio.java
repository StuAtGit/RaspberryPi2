package com.shareplaylearn;

import com.pi4j.io.gpio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vaguely simplifies the interface, but also allows for simulated runs/mocks the gpio.
 *
 */
public class Gpio
{
    private final GpioController gpio;
    private static final Logger log =LoggerFactory.getLogger(Gpio.class);
    private boolean mock;

    public Gpio() {
        this.gpio = GpioFactory.getInstance();
        this.mock = false;
    }

    public Gpio( boolean mock ) {
        if( mock ) {
            this.mock = true;
            this.gpio = null;
            log.warn("Running gpio controller in mock mode - will only log operations, and return mock pins.");
            return;
        }
        this.gpio = GpioFactory.getInstance();
        this.mock = false;
    }

    public GpioPinDigitalOutput getOutputPin( Pin pin ) {
        log.debug("Retrieving reference to pin: " + pin.toString());
        if( !this.mock ) {
            final GpioPinDigitalOutput outputPin = this.gpio.provisionDigitalOutputPin(pin, pin.toString(), PinState.LOW);
            outputPin.setShutdownOptions(true, PinState.LOW);
            return outputPin;
        }
        return new MockDigitalOutputPin( pin );
    }

    //Only real benefit to this is logging
    public void setHigh( GpioPinDigitalOutput pin ) {
        log.debug("Setting pin: " + pin.toString() + " to high state.");
        if( !this.mock ) {
            pin.setState(PinState.HIGH);
        }
    }

    public void shutdown() {
        log.info("Shutting down gpio.");
        if( !this.mock ) {
            this.gpio.shutdown();
        }
    }

    public static void main( String[] args ) throws InterruptedException {
        Gpio gpio = new Gpio(true);
        GpioPinDigitalOutput pin = gpio.getOutputPin( RaspiPin.GPIO_16 );
        gpio.setHigh(pin);
        Thread.sleep(10000);
        gpio.shutdown();
    }
}
