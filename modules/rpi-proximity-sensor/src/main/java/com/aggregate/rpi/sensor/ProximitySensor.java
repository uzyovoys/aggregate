package com.aggregate.rpi.sensor;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

/**
 * Created by morfeusys on 18.02.16.
 */
public class ProximitySensor extends AbstractVerticle implements GpioPinListenerDigital {
    private GpioController controller;

    @Override
    public void start(Future<Void> f) throws Exception {
        String pinName = config().getString("input");
        if (pinName == null || pinName.isEmpty()) {
            f.fail(new IllegalArgumentException("No input pin name is defined"));
            return;
        }
        Pin pin = RaspiPin.getPinByName(pinName);
        if (pin == null) {
            f.fail(new IllegalArgumentException("Pin with name '" + pinName + "' is not found"));
            return;
        }
        Pin vcc = null;
        String vccName = config().getString("vcc");
        if (vccName != null && !vccName.isEmpty()) {
            vcc = RaspiPin.getPinByName(vccName);
            if (vcc == null) {
                f.fail(new IllegalArgumentException("Pin with name '" + vccName + "' is not found"));
                return;
            }
        }
        controller = GpioFactory.getInstance();
        if (vcc != null) controller.provisionDigitalOutputPin(vcc, PinState.HIGH);
        GpioPinDigitalInput input = controller.provisionDigitalInputPin(pin, PinPullResistance.PULL_DOWN);
        input.addListener(this);
        f.complete();
    }

    @Override
    public void stop() throws Exception {
        controller.shutdown();
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        PinState state = event.getState();
        if (state == PinState.HIGH) {
            vertx.eventBus().publish("asr.stop", null);
        } else {
            vertx.eventBus().publish("asr.start", null);
        }
    }
}
