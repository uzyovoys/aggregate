package com.aggregate.rpi;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

/**
 * Created by morfeusys on 29.02.16.
 */
public class PinData {
    public final String name;
    public final int state;

    public PinData(String name, int state) {
        this.name = name;
        this.state = state;
    }

    public Pin getPin() {
        return RaspiPin.getPinByName(name);
    }

    public PinState getState() {
        return PinState.getState(state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PinData pinData = (PinData) o;

        if (state != pinData.state) return false;
        return name.equals(pinData.name);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + state;
        return result;
    }
}
