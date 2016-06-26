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

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;

/**
 * Created by stu on 11/14/15.
 * Not sure about this. But the idea is we eventually may want a way to map
 * a set of commands like "POWER_STRIP_1_ON" to PIN_16 -> HIGH.
 * Or "LIGHT_SWITCH_1_OFF -> PIN_12 -> LOW.
 * etc.
 *
 * Actually, this should be an externally configurable thing (the mapping of higher-level commands). Yeah. Obvious. :D.
 *
 * Yeah, this could become an interface with different implementations, but one thing at a time..
 */
public class CommandTranslator {

    Gpio gpio;

    public CommandTranslator( Gpio gpio ) {
        this.gpio = gpio;
    }

    /**
     * Eventually, this may get pulled into a JSON schema or something.
     */
    public static class AvailableCommands {
        public static final String RECONNECT = "RECONNECT";
        public static final String RAW_PIN = "RAW_PIN";
    }

    public enum ResultType {
        PIN,
        DAEMON_COMMAND,
        INVALID_COMMAND
    }
    public static class Result {
        public Result( ResultType type, String response ) {
            this.type = type;
            this.response = response;
        }
        public ResultType type;
        public String response;
        public GpioPinDigitalOutput pin;   
        public boolean setPinHigh;
        public boolean reconnect;
        public int millisecondsBeforeReconnect;
        public boolean shutdown;
        //etc.
    }

    public Result translateMessage( String message ) {
        if( message.trim().startsWith(AvailableCommands.RECONNECT) ) {
            String[] commandArgs = message.split(" ");
            if( commandArgs.length != 2 ) {
                return new Result(ResultType.INVALID_COMMAND, "Wrong number of arguments: " + message );
            }
            int milliseconds = Integer.parseInt(commandArgs[1]);
            if( milliseconds < 0 ) {
                return new Result( ResultType.INVALID_COMMAND, "Invalid wait time for restart: " + milliseconds );
            }
            Result result = new Result( ResultType.DAEMON_COMMAND, "Reconnecting in: " + milliseconds + " ms ");
            result.reconnect = true;
            result.millisecondsBeforeReconnect = milliseconds;
        } else if( message.trim().startsWith(AvailableCommands.RAW_PIN) ) {
            String[] commandArgs = message.split(" ");
            if( commandArgs.length != 3 ) {
                return new Result( ResultType.INVALID_COMMAND, "Wrong number of arguments: " + message );
            }

            GpioPinDigitalOutput pin = gpio.getOutputPin( RaspiPin.getPinByName(commandArgs[1]) );
            if( commandArgs[2].equals("HIGH") || commandArgs[2].equals("LOW") ) {
                //in this case, we may not send the response message to the broker (since we want to wait until
                //we actually set it!)
                Result result = new Result(ResultType.PIN, "Setting pin." );
                result.pin = pin;
                result.setPinHigh = commandArgs[2].equals("HIGH");
                return result;
            } else {
                return new Result( ResultType.INVALID_COMMAND, "Incorrect PIN or PIN state: " + message );
            }
        }
        return new Result(ResultType.INVALID_COMMAND, "Unknown command: " + message );
    }

}