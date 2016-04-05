package com.aggregate.dusi;

import com.aggregate.api.Request;
import com.aggregate.api.Response;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by morfeusys on 22.02.16.
 */
public class DusiAssistant extends AbstractVerticle {
    private static Logger log = LoggerFactory.getLogger(DusiAssistant.class);

    private String id;
    private WSClient wsClient;

    @Override
    public void start(Future<Void> f) throws Exception {
        id = config().getString("id");
        if (id == null || id.isEmpty()) {
            f.fail("Please provide your dusi ID");
            return;
        }
        vertx.eventBus().consumer("cmd.dusi", msg -> {
            process(Request.fromMessage(msg));
        });
        f.complete();
    }

    private void process(Request request) {
        String input = request.markup.source;
        if (wsClient == null || !WebSocket.READYSTATE.OPEN.equals(wsClient.getReadyState())) {
            try {
                wsClient = new WSClient(id);
                vertx.executeBlocking(h -> {
                    try {
                        if (wsClient.connectBlocking()) {
                            h.complete();
                        } else {
                            h.fail("Cannot connect");
                        }
                    } catch (InterruptedException e) {
                        h.fail(e);
                    }
                }, true, r -> {
                    if (r.succeeded()) {
                        send(input);
                    } else {
                        log.error("Cannot connect");
                    }
                });
            } catch (Exception e) {
                log.error("Cannot create ws client", e);
            }
        } else {
            send(input);
        }
    }

    private void send(String input) {
        wsClient.send(input);
    }

    private void processReply(JsonObject json) {
        String uri = json.getString("response_uri");
        String text = json.getString("speech", json.getString("text"));
        List<String> speeches = new ArrayList<>();
        if (text != null) {
            speeches.addAll(Arrays.asList(text.split("\\|")));
        }
        boolean modal = json.getBoolean("modal", false);
        vertx.eventBus().publish("response", new Response(speeches, modal ? "com.aggregate.dusi" : null, modal));
        if (uri != null && Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(uri));
            } catch (Exception e) {
                log.error("Cannot open URI", e);
            }
        }
    }


    private class WSClient extends WebSocketClient {

        public WSClient(String id) throws URISyntaxException {
            super(new URI("ws://api.dusi.mobi:8000/remote?id=" + id));
        }

        @Override
        public void onOpen(ServerHandshake serverHandshake) {
            log.info("Connected");
        }

        @Override
        public void onMessage(String s) {
            log.info("Received: " + s);
            JsonObject json = new JsonObject(s);
            processReply(json);
        }

        @Override
        public void onClose(int i, String s, boolean b) {
            log.info("Disconnected");
        }

        @Override
        public void onError(Exception e) {
            log.error("Error", e);
        }
    }
}
