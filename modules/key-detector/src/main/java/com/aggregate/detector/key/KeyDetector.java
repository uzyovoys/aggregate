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
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * Created by morfeusys on 18.02.16.
 */
public class KeyDetector extends AbstractVerticle implements NativeKeyListener, NativeMouseInputListener {
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
            GlobalScreen.addNativeMouseListener(this);
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

    private void keyPressed(int code) {
        vertx.eventBus().publish("key.pressed", code);
        if (pressedKeys.contains(code)) return;
        pressedKeys.add(code);
        log.info("Keys: " + pressedKeys);
        if (!keySet.isEmpty() && pressedKeys.containsAll(keySet)) {
            log.info("Keyset detected");
            vertx.eventBus().publish("asr.start", null);
        }
    }

    private void keyReleased(int code) {
        vertx.eventBus().publish("key.released", code);
        if (pressedKeys.contains(code)
                && keySet.contains(code)
                && pressedKeys.containsAll(keySet)) {
            vertx.eventBus().publish("asr.stop", null);
        }
        pressedKeys.remove(code);
    }


    public void nativeKeyPressed(NativeKeyEvent event) {
        int code = event.getRawCode();
        keyPressed(code);
    }

    public void nativeKeyReleased(NativeKeyEvent event) {
        int code = event.getRawCode();
        keyReleased(code);
    }

    public void nativeKeyTyped(NativeKeyEvent event) {
    }


    public void nativeMouseClicked(NativeMouseEvent event) {
        //log.info("Mouse Clicked: " + e.getClickCount());
    }

    public void nativeMousePressed(NativeMouseEvent event) {
        int code = event.getButton();
        keyPressed(code);
        //log.info("Mouse Pressed: " + event.getButton());
    }

    public void nativeMouseReleased(NativeMouseEvent event) {
        int code = event.getButton();
        keyReleased(code);
        //log.info("Mouse Released: " + event.getButton());
    }

    public void nativeMouseMoved(NativeMouseEvent event) {
        //log.info("Mouse Moved: " + e.getX() + ", " + e.getY());
    }

    public void nativeMouseDragged(NativeMouseEvent event) {
        //log.info("Mouse Dragged: " + e.getX() + ", " + e.getY());
    }

}
