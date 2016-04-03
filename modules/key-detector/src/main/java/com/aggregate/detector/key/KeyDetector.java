package com.aggregate.detector.key;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Created by morfeusys on 18.02.16.
 */
public class KeyDetector extends AbstractVerticle implements NativeKeyListener, NativeMouseInputListener {
    private static final long RELEASE_TIMEOUT = 200;
    private static Logger log = LoggerFactory.getLogger(KeyDetector.class);

    private boolean skipLogs;
    private long pressedTimestamp;
    private Map<String, Set<Integer>> actionMap = new HashMap<>();
    private Set<Integer> pressedKeys = new HashSet<>();

    @Override
    public void start(Future<Void> f) throws Exception {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);
        JsonObject config = config();
        config.put("signal", "key");
        skipLogs = config.getBoolean("skip-logs", false);
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeMouseListener(this);
            GlobalScreen.addNativeKeyListener(this);
            vertx.eventBus().consumer("key.addKeyAction", this::addKeyAction);
            vertx.eventBus().consumer("key.start", this::onKeyStart);
            vertx.eventBus().consumer("key.stop", this::onKeyStop);
        } catch (NativeHookException e) {
            f.fail(e);
            return;
        }
        vertx.eventBus().send("key.addKeyAction", config);
        f.complete();
    }

    @Override
    public void stop() throws Exception {
        GlobalScreen.unregisterNativeHook();
    }

    private void onKeyStart(Message message) {
        pressedTimestamp = System.currentTimeMillis();
        vertx.eventBus().publish("asr.start", null);
    }

    private void onKeyStop(Message message) {
        if (System.currentTimeMillis() - pressedTimestamp < RELEASE_TIMEOUT) {
            vertx.eventBus().publish("asr.listen", null);
        } else {
            vertx.eventBus().publish("asr.stop", null);
        }
    }

    private void addKeyAction(Message message){
        try {
            JsonObject json = (JsonObject) message.body();
            String signal = json.getString("signal");
            JsonArray keys = json.getJsonArray("key-codes");
            Set<Integer> set = new HashSet<>();
            if (keys == null || keys.isEmpty()) {
                log.warn("No keys are defined for hook " + signal);
            } else {
                set.addAll(keys.getList());
                if (signal == null || signal.equals("")) {
                    log.warn("Signal is empty " + message.address());
                } else {
                    actionMap.put(signal, set);
                }
            }
        } catch(Exception e){
            log.warn("Action was not added correctly", e);
        }
    }

    private void keyPressed(int code) {
        if (pressedKeys.contains(code)) return;
        pressedKeys.add(code);
        if (!skipLogs) log.info("Keys: " + pressedKeys);

        actionMap.entrySet().stream().filter(entry -> pressedKeys.equals(entry.getValue())).forEach(entry -> {
            log.info("Keyset detected for " + entry.getKey());
            vertx.eventBus().send(entry.getKey() + ".start", null);
        });
    }

    private void keyReleased(int code) {
        if (pressedKeys.contains(code)) {
            actionMap.entrySet().stream().filter(entry ->
                    entry.getValue().contains(code) && pressedKeys.equals(entry.getValue()))
                    .forEach(e -> vertx.eventBus().send(e.getKey() + ".stop", null));
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
//        log.info(event.isActionKey() + " char"+event.getKeyCode()+": ", event.getKeyChar() + "___" + event.getKeyLocation());
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent event) {
//        log.info("Mouse " + event.getButton() + " Clicked: " + event.getClickCount() + "\n" + pressedKeys);
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
