package com.aggregate.browser;

import com.aggregate.api.Markup;
import com.aggregate.api.Request;
import io.vertx.core.AbstractVerticle;

/**
 * Created by morfeusys on 24.02.16.
 */
public class Browser extends AbstractVerticle {
    private static final String BROWSER_SITE_URL = "BrowserSiteURL";
    private static final String BROWSER_SITE_NAME = "BrowserSiteName";

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer("cmd.browser.open", m -> m.reply(getSite(Request.fromMessage(m))));
    }

    private String getSite(Request request) {
        String url;
        Markup markup = request.markup.get(BROWSER_SITE_URL);
        if (markup != null) {
            url = markup.source.replace(" ", "");
        } else {
            markup = request.markup.get(BROWSER_SITE_NAME);
            url = markup.value.replace("_", ".");
        }
        return url.startsWith("http") ? url : "http://" + url;
    }
}
