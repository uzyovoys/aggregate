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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Created by morfeusys on 18.02.16.
 */
public class KeyDetector extends AbstractVerticle implements NativeKeyListener {
    private static final long RELEASE_TIMEOUT = 200;
    private static Logger log = LoggerFactory.getLogger(KeyDetector.class);

    private boolean skipLogs;
    private long pressedTimestamp;
    private Map<String, Set<String>> actionMap = new HashMap<>();
    private Set<String> pressedKeys = new HashSet<>();

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
            JsonArray keys = json.getJsonArray("keys");
            Set<String> set = new HashSet<>();
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

    private void keyPressed(String key) {
        if (pressedKeys.contains(key)) return;
        pressedKeys.add(key);
        if (!skipLogs) log.info("Keys: " + pressedKeys);

        actionMap.entrySet().stream().filter(entry -> pressedKeys.equals(entry.getValue())).forEach(entry -> {
            log.info("Keyset detected for " + entry.getKey());
            vertx.eventBus().send(entry.getKey() + ".start", null);
        });
    }

    private void keyReleased(String key) {
        if (pressedKeys.contains(key)) {
            actionMap.entrySet().stream().filter(entry ->
                    entry.getValue().contains(key) && pressedKeys.equals(entry.getValue()))
                    .forEach(e -> vertx.eventBus().send(e.getKey() + ".stop", null));
        }
        pressedKeys.remove(key);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent event) {
        keyPressed(NativeKeyEvent.getKeyText(event.getKeyCode()));
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent event) {
        keyReleased(NativeKeyEvent.getKeyText(event.getKeyCode()));
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent event) {
//        log.info(event.isActionKey() + " char"+event.getKeyCode()+": ", event.getKeyChar() + "___" + event.getKeyLocation());
    }

}
