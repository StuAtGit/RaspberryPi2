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

    public void setLow( GpioPinDigitalOutput pin ) {
        log.debug("Setting pin: " + pin.toString() + " to low state.");
        if( !this.mock ) {
            pin.setState(PinState.LOW);
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
