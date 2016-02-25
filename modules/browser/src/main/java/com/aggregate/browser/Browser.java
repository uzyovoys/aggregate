package com.aggregate.browser;

import com.aggregate.api.Markup;
import com.aggregate.api.Request;
import io.vertx.core.AbstractVerticle;

import java.awt.Desktop;
import java.net.URI;

/**
 * Created by morfeusys on 24.02.16.
 */
public class Browser extends AbstractVerticle {
    private static final String PATTERN_SITE = "BrowserSite";

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer("cmd.browser.open", m -> onOpen(Request.fromMessage(m)));
    }

    private void onOpen(Request request) {
        Markup markup = request.markup.get(PATTERN_SITE);
        String url = markup != null ? markup.source : request.markup.source;
        open(url.replace(" ", ""));
    }

    private void open(String url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI("http://" + url));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
