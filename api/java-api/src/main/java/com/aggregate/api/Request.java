package com.aggregate.api;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * Created by morfeusys on 22.02.16.
 */
public class Request {
    public final Markup markup;

    public Request(Markup markup) {
        this.markup = markup;
    }


    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        if (markup != null) {
            json.put("markup", markup.toJson());
        }
        return json;
    }

    public static Request fromJson(JsonObject json) {
        return new Request(Markup.fromJson(json.getJsonObject("markup")));
    }

    public static Request fromMessage(Message msg) {
        Object body = msg.body();
        if (body instanceof JsonObject) {
            return fromJson((JsonObject) body);
        }
        return null;
    }
}
