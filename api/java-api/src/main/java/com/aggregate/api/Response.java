package com.aggregate.api;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.*;

/**
 * Created by morfeusys on 22.02.16.
 */
public class Response extends JsonObject {
    public final List<String> speeches;
    public final String moduleId;
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
