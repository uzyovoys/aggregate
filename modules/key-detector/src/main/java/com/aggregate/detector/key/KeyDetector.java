package com.aggregate.detector.key;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * Created by morfeusys on 18.02.16.
 */
public class KeyDetector extends AbstractVerticle implements NativeKeyListener {
    private static Logger log = LoggerFactory.getLogger(KeyDetector.class);

    private Set<Integer> keySet = new HashSet<>();
    private Set<Integer> pressedKeys = new HashSet<>();

    @Override
    public void start(Future<Void> f) throws Exception {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);
        JsonArray keys = config().getJsonArray("key-codes");
        if (keys == null || keys.isEmpty()) {
            log.warn("No keys are defined for hook");
        } else {
            keySet.addAll(keys.getList());
        }
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
            f.complete();
        } catch (NativeHookException e) {
            f.fail(e);
        }
    }

    @Override
    public void stop() throws Exception {
        GlobalScreen.unregisterNativeHook();
    }

    public void nativeKeyPressed(NativeKeyEvent event) {
        int code = event.getRawCode();
        vertx.eventBus().publish("key.pressed", code);
        if (pressedKeys.contains(code)) return;
        pressedKeys.add(code);
        log.info("Keys: " + pressedKeys);
        if (pressedKeys.containsAll(keySet)) {
            log.info("Keyset detected");
            vertx.eventBus().publish("asr.start", null);
        }
    }

    public void nativeKeyReleased(NativeKeyEvent event) {
        int code = event.getRawCode();
        vertx.eventBus().publish("key.released", code);
        if (pressedKeys.contains(code)
                && keySet.contains(code)
                && pressedKeys.containsAll(keySet)) {
            vertx.eventBus().publish("asr.stop", null);
        }
        pressedKeys.remove(code);
    }

    public void nativeKeyTyped(NativeKeyEvent event) {
    }
}
