package com.aggregate.api;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.*;

/**
 * Represents response from speech processing module
 *
 * @author morfeusys
 */
public class Response extends JsonObject {
    /**
     * List of output speech strings (could be empty)
     */
    public final List<String> speeches;

    /**
     * Dialog's context (optional)
     */
    public final String context;

    /**
     * If switching context is modal or not
     */
    public final boolean modal;

    public Response(String speech) {
        this(singletonList(speech));
    }

    public Response(List<String> speeches) {
        this(speeches, null, false);
    }

    public Response(String speech, String context, boolean modal) {
        this(singletonList(speech), context, modal);
    }

    public Response(String context, boolean modal) {
        this(emptyList(), context, modal);
    }

    public Response(List<String> speeches, String context, boolean modal) {
        this.speeches = unmodifiableList(speeches);
        this.context = context;
        this.modal = modal;
        JsonArray array = new JsonArray();
        speeches.forEach(s -> {
            if (s != null && !s.isEmpty()) array.add(s);
        });
        put("context", context);
        put("modal", modal);
        put("speeches", array);
    }


    /**
     * Converts Json object to Response instance
     * @param json JsonObject
     * @return Response instance
     */
    public static Response fromJson(JsonObject json) {
        JsonArray array = json.getJsonArray("speeches", new JsonArray());
        List<String> speeches = new ArrayList<>(array.size());
        array.forEach(s -> speeches.add(s.toString()));
        if (speeches.isEmpty()) {
            String speech = json.getString("speech");
            if (speech != null) speeches.add(speech);
        }
        String context = json.getString("context", json.getString("module"));
        Boolean modal = json.getBoolean("modal", false);
        return new Response(speeches, context, modal);
    }
}
