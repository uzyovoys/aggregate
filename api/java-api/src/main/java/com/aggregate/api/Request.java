package com.aggregate.api;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * Represents user's request
 *
 * @author morfeusys
 */
public class Request {
    /**
     * Markup of input speech
     * @see Markup
     */
    public final Markup markup;

    public Request(Markup markup) {
        this.markup = markup;
    }

    /**
     * Converts this instance to Json object
     * @return JsonObject instance
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        if (markup != null) {
            json.put("markup", markup.toJson());
        }
        return json;
    }


    /**
     * Converts Json object to Request instance
     * @param json JsonObject
     * @return Request instance
     */
    public static Request fromJson(JsonObject json) {
        return new Request(Markup.fromJson(json.getJsonObject("markup")));
    }

    /**
     * Converts Vertx's event message to Request instance
     * @param msg - Vertx's event message
     * @return Request instance
     * @see Message
     */
    public static Request fromMessage(Message msg) {
        Object body = msg.body();
        if (body instanceof JsonObject) {
            return fromJson((JsonObject) body);
        }
        return null;
    }
}
