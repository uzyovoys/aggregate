package com.aggregate.rpi;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by morfeusys on 18.02.16.
 */
public class RpiGpioDetector extends AbstractVerticle implements GpioPinListenerDigital {
    private GpioController controller;
    private Set<PinData> output;
    private Map<String, Integer> states = Collections.synchronizedMap(new HashMap<>());
    private long listenTimeout;
    private long detectionTimestamp;
    private boolean listening;

    @Override
    public void start(Future<Void> f) throws Exception {
        Set<PinData> input = getPins("input");
        output = getPins("output");
        listenTimeout = config().getLong("listenTimeout", 0l);

        if (output.isEmpty()) {
            f.fail(new IllegalArgumentException("Define output pins in the config file"));
            return;
        }

        controller = GpioFactory.getInstance();

        input.forEach(pd -> {
            Pin pin = pd.getPin();
            PinState state = pd.getState();
            if (pin == null) {
                f.fail(new IllegalArgumentException("Pin with name '" + pd.name + "' is not found"));
                return;
            }
            if (state == null) {
                f.fail(new IllegalArgumentException("No such pin state '" + pd.state + ". Use 0 and 1 only."));
                return;
            }
            controller.provisionDigitalOutputPin(pin, state);
        });

        output.forEach(pd -> {
            Pin pin = pd.getPin();
            if (pin == null) {
                f.fail(new IllegalArgumentException("Pin with name '" + pd.name + "' is not found"));
                return;
            }
            controller.provisionDigitalInputPin(pin).addListener(this);
        });

        vertx.eventBus().consumer("asr.stop", m -> listening = false);

        f.complete();
    }

    @Override
    public void stop() throws Exception {
        controller.shutdown();
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        int state = event.getState().getValue();
        String name = event.getPin().getName();
        states.put(name, state);
        for (PinData pd : output) {
            Integer s = states.get(pd.name);
            if (s == null) return;
            if (s != pd.state) {
                finish();
                return;
            }
        }
        listen();
    }

    private synchronized void listen() {
        if (!listening) {
            listening = true;
            detectionTimestamp = System.currentTimeMillis();
            vertx.eventBus().publish("asr.start", null);
        }
    }

    private synchronized void finish() {
        if (System.currentTimeMillis() - detectionTimestamp < listenTimeout) {
            vertx.eventBus().publish("asr.listen", null);
        } else if (listening) {
            listening = false;
            vertx.eventBus().publish("asr.stop", null);
        }
    }

    private Set<PinData> getPins(String config) {
        JsonObject obj = config().getJsonObject(config);
        if (obj == null || obj.isEmpty()) return Collections.emptySet();
        return obj.fieldNames().stream().map(name -> new PinData(name, obj.getInteger(name)))
                .collect(Collectors.toSet());
    }
}
