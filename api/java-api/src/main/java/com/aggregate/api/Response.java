package com.aggregate.api;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
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
     * Module's id to switch dialog's context (optional)
     */
    public final String moduleId;

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

    public Response(String speech, String moduleId, boolean modal) {
        this(singletonList(speech), moduleId, modal);
    }

    public Response(String moduleId, boolean modal) {
        this(emptyList(), moduleId, modal);
    }

    public Response(List<String> speeches, String moduleId, boolean modal) {
        this.speeches = unmodifiableList(speeches);
        this.moduleId = moduleId;
        this.modal = modal;
        JsonArray array = new JsonArray();
        speeches.forEach(s -> {
            if (s != null && !s.isEmpty()) array.add(s);
        });
        put("moduleId", moduleId);
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
        String moduleId = json.getString("moduleId");
        boolean modal = json.getBoolean("modal");
        return new Response(speeches, moduleId, modal);
    }
}
