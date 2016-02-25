package com.aggregate.typer;

import com.aggregate.api.Pattern;
import com.aggregate.api.Request;
import com.aggregate.api.Response;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by morfeusys on 24.02.16.
 */
public class Typer extends AbstractVerticle {
    private static Logger log = LoggerFactory.getLogger(Typer.class);

    private String osName;
    private Set<Integer> keySet = new HashSet<>();
    private Set<Integer> pressedKeys = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void start() throws Exception {
        osName = System.getProperty("os.name").toLowerCase();
        JsonArray keys = config().getJsonArray("key-codes", new JsonArray());
        keySet.addAll(keys.getList());
        if (!keySet.isEmpty()) {
            vertx.eventBus().consumer("key.pressed", m -> keyPressed((Integer) m.body()));
            vertx.eventBus().consumer("key.released", m -> keyReleased((Integer) m.body()));
        }
        vertx.eventBus().consumer("cmd.typer.type", m -> type(Request.fromMessage(m)));
    }

    private void type(Request request) {
        String text = request.markup.get(Pattern.TEXT).source;
        if (!text.isEmpty()) {
            type(text);
        }
    }

    private void type(String text) {
        try {
            Robot robot = new Robot();
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection stringSelection = new StringSelection(text);
            clipboard.setContents(stringSelection, stringSelection);

            int ctrl = osName.contains("mac") ? KeyEvent.VK_META : KeyEvent.VK_CONTROL;
            robot.keyPress(ctrl);
            robot.keyPress(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_V);
            robot.keyRelease(ctrl);
        } catch (Exception e) {
            log.error("Cannot type", e);
        }
    }

    private void keyPressed(int code) {
        if (!keySet.contains(code) || pressedKeys.contains(code)) return;
        pressedKeys.add(code);
        if (pressedKeys.containsAll(keySet)) {
            vertx.eventBus().publish("asr.start", null);
            vertx.eventBus().send("response", new Response("typer.text", true));
        }
    }

    private void keyReleased(int code) {
        if (pressedKeys.contains(code)) {
            vertx.eventBus().publish("asr.stop", null);
            pressedKeys.remove(code);
        }
    }
}
