package com.aggregate.tts.mac;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by morfeusys on 18.02.16.
 */
public class MacTextToSpeech extends AbstractVerticle {
    private static Logger log = LoggerFactory.getLogger(MacTextToSpeech.class);

    private final Queue<String> queue = new LinkedList<>();
    private volatile Process process;

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer("tts.say", e -> {
            String text = e.body().toString();
            say(text);
        });
        vertx.eventBus().consumer("tts.stop", e -> {
           stopSpeaking();
        });
    }

    private void say(String text) {
        synchronized (queue) {
            queue.add(text);
        }
        sayNext();
    }

    private void sayNext() {
        if (process != null && process.isAlive()) return;
        final String text;
        synchronized (queue) {
            text = queue.poll();
        }
        if (text != null) {
            vertx.executeBlocking(h -> {
                log.info("Saying " + text);
                try {
                    process = Runtime.getRuntime().exec("say");
                    OutputStream os = process.getOutputStream();
                    os.write((text + "\n").getBytes());
                    os.flush();
                    os.close();
                    process.waitFor();
                    h.complete();
                    log.info("Complete saying " + text);
                } catch (IOException e1) {
                    log.error("Cannot say " + text, e1);
                    h.fail(e1);
                } catch (InterruptedException e2) {
                    stopSpeaking();
                    h.fail(e2);
                }
            }, r -> sayNext());
        }
    }

    private void stopSpeaking() {
        synchronized (queue) {
            queue.clear();
        }
        if (process != null && process.isAlive()) {
            process.destroy();
        }
    }
}
