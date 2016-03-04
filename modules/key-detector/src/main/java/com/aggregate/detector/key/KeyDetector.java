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
        if (pressedKeys.contains(code)) return;
        vertx.eventBus().publish("key.pressed", code);
        pressedKeys.add(code);
        log.info("Keys: " + pressedKeys);
        if (!keySet.isEmpty() && pressedKeys.containsAll(keySet)) {
            log.info("Keyset detected");
            vertx.eventBus().publish("asr.start", null);
        }
    }

    private void keyReleased(int code) {
        if (pressedKeys.contains(code)) {
            vertx.eventBus().publish("key.released", code);
            if (keySet.contains(code) && pressedKeys.containsAll(keySet)) {
                vertx.eventBus().publish("asr.stop", null);
            }
        }
        pressedKeys.remove(code);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent event) {
        keyPressed(event.getRawCode());
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent event) {
        keyReleased(event.getRawCode());
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent event) {
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent event) {
        //log.info("Mouse Clicked: " + e.getClickCount());
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent event) {
        keyPressed(event.getButton());
        //log.info("Mouse Pressed: " + event.getButton());
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent event) {
        keyReleased(event.getButton());
        //log.info("Mouse Released: " + event.getButton());
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent event) {
        //log.info("Mouse Moved: " + e.getX() + ", " + e.getY());
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent event) {
        //log.info("Mouse Dragged: " + e.getX() + ", " + e.getY());
    }

}
