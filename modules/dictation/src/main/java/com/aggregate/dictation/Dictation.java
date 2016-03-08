package com.aggregate.dictation;

import com.aggregate.api.Pattern;
import com.aggregate.api.Request;
import com.aggregate.api.Response;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;

/**
 * Created by morfeusys on 24.02.16.
 */
public class Dictation extends AbstractVerticle {
    private static Logger log = LoggerFactory.getLogger(Dictation.class);

    private String osName;

    @Override
    public void start() throws Exception {
        osName = System.getProperty("os.name").toLowerCase();
        JsonObject config = config();
        config.put("signal", "dictation");
        config.put("key-codes", config.getJsonArray("key-codes", new JsonArray()));
        vertx.eventBus().consumer("dictation.start", m -> startAction());
        vertx.eventBus().consumer("dictation.stop", m -> stopAction());
        vertx.eventBus().consumer("cmd.dictation.type", m -> type(Request.fromMessage(m)));
        vertx.eventBus().publish("key.addAction", config);
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
    private void startAction(){
        vertx.eventBus().publish("asr.start", null);
        vertx.eventBus().send("response", new Response("dictation.text", true));
    }
    private void stopAction(){
        vertx.eventBus().publish("asr.stop", null);
    }
}
