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
 *
 * If you modify this Program, or any covered work, by linking or combining
 * it with the paho MQTT client library (or a modified version of that library),
 * containing parts covered by the terms of EPL,
 * the licensors of this Program grant you additional permission to convey the resulting work.
 */
package com.shareplaylearn;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Created by stu on 11/8/15.
 */
public class MockDigitalOutputPin
        implements GpioPinDigitalOutput
{
    private static final Logger log = LoggerFactory.getLogger( MockDigitalOutputPin.class );
    private String name;
    private boolean isHigh;
    private PinState pinState;

    public MockDigitalOutputPin( Pin pin ) {
        this.name = pin.toString();
    }

    public void setState(PinState pinState) {
        log.debug("Setting pin " + this.name + " to " + pinState.toString() );
        this.pinState = pinState;
    }

    public boolean isHigh() {
        return this.pinState == PinState.HIGH;
    }

    public boolean isLow() {
        return this.pinState == PinState.LOW;
    }

    public PinState getState() {
        return this.pinState;
    }

    public boolean isState(PinState pinState) {
        this.setState(pinState);
        //TODO - what are we expected to return here??
        return false;
    }

    public void setName(String s) {
        this.name = s;
    }

    public String getName() {
        return this.name;
    }

    public void setState(boolean b) {

    }

    @Override
    public String toString() {
        return this.name;
    }

    public void high() {

    }

    public void low() {

    }

    public void toggle() {

    }

    public Future<?> blink(long l) {
        return null;
    }

    public Future<?> blink(long l, PinState pinState) {
        return null;
    }

    public Future<?> blink(long l, long l1) {
        return null;
    }

    public Future<?> blink(long l, long l1, PinState pinState) {
        return null;
    }

    public Future<?> pulse(long l) {
        return null;
    }

    public Future<?> pulse(long l, Callable<Void> callable) {
        return null;
    }

    public Future<?> pulse(long l, boolean b) {
        return null;
    }

    public Future<?> pulse(long l, boolean b, Callable<Void> callable) {
        return null;
    }

    public Future<?> pulse(long l, PinState pinState) {
        return null;
    }

    public Future<?> pulse(long l, PinState pinState, Callable<Void> callable) {
        return null;
    }

    public Future<?> pulse(long l, PinState pinState, boolean b) {
        return null;
    }

    public Future<?> pulse(long l, PinState pinState, boolean b, Callable<Void> callable) {
        return null;
    }

    public GpioProvider getProvider() {
        return null;
    }

    public Pin getPin() {
        return null;
    }

    public void setTag(Object o) {

    }

    public Object getTag() {
        return null;
    }

    public void setProperty(String s, String s1) {

    }

    public boolean hasProperty(String s) {
        return false;
    }

    public String getProperty(String s) {
        return null;
    }

    public String getProperty(String s, String s1) {
        return null;
    }

    public Map<String, String> getProperties() {
        return null;
    }

    public void removeProperty(String s) {

    }

    public void clearProperties() {

    }

    public void export(PinMode pinMode) {

    }

    public void export(PinMode pinMode, PinState pinState) {

    }

    public void unexport() {

    }

    public boolean isExported() {
        return false;
    }

    public void setMode(PinMode pinMode) {

    }

    public PinMode getMode() {
        return null;
    }

    public boolean isMode(PinMode pinMode) {
        return false;
    }

    public void setPullResistance(PinPullResistance pinPullResistance) {

    }

    public PinPullResistance getPullResistance() {
        return null;
    }

    public boolean isPullResistance(PinPullResistance pinPullResistance) {
        return false;
    }

    public Collection<GpioPinListener> getListeners() {
        return null;
    }

    public void addListener(GpioPinListener... gpioPinListeners) {

    }

    public void addListener(List<? extends GpioPinListener> list) {

    }

    public boolean hasListener(GpioPinListener... gpioPinListeners) {
        return false;
    }

    public void removeListener(GpioPinListener... gpioPinListeners) {

    }

    public void removeListener(List<? extends GpioPinListener> list) {

    }

    public void removeAllListeners() {

    }

    public GpioPinShutdown getShutdownOptions() {
        return null;
    }

    public void setShutdownOptions(GpioPinShutdown gpioPinShutdown) {

    }

    public void setShutdownOptions(Boolean aBoolean) {

    }

    public void setShutdownOptions(Boolean aBoolean, PinState pinState) {

    }

    public void setShutdownOptions(Boolean aBoolean, PinState pinState, PinPullResistance pinPullResistance) {

    }

    public void setShutdownOptions(Boolean aBoolean, PinState pinState, PinPullResistance pinPullResistance, PinMode pinMode) {

    }
}
