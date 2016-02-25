package com.aggregate.api;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Created by morfeusys on 22.02.16.
 */
public class Markup {
    public final String source;
    public final String name;
    public final String value;
    public final Map<String, Object> data = new HashMap<>();

    public final List<Markup> children = new ArrayList<>();

    public Markup(String name, Markup markup) {
        this(markup.source, name, null);
        children.add(markup);
    }

    public Markup(String source, String name, String value) {
        this.source = source;
        this.name = name;
        this.value = value;
    }

    public List<Markup> getAll(String name) {
        ArrayList<Markup> list = new ArrayList<>();
        if (name.equals(this.name)) {
            list.add(this);
        }
        for (Markup markup : children) {
            list.addAll(markup.getAll(name));
        }
        return list;
    }

    public List<Markup> getChildren() {
        if (source == null) return children;
        String[] arr = source.split(" ");
        Set<Markup> set = new HashSet<>(children);
        List<Markup> list = new ArrayList<>(children.size());
        for (int i = 0; i < arr.length; i++) {
            String s = arr[i];
            for (Markup m : set) {
                if (s.equals(m.source)) {
                    list.add(m);
                    set.remove(m);
                    break;
                }
            }
        }
        return list;
    }

    public Markup get(String name) {
        if(name.equals(this.name)) return this;
        for(Markup markup : children)
            if(name.equals(markup.name)) return markup;
        return null;
    }

    public boolean has(String name) {
        if(name.equals(this.name)) return true;
        for(Markup markup : children)
            if(name.equals(markup.name)) return true;
        return false;
    }

    public boolean isBlank() {
        return source.isEmpty();
    }

    public Markup get(int index) {
        return isEmpty() ? null : children.get(index);
    }

    public boolean isEmpty() {
        return children.isEmpty();
    }

    public int getIntValue() {
        return Integer.parseInt(value);
    }

    @Override
    public String toString() {
        return source;
    }


    public JsonObject toJson() {
        JsonObject dataObject = new JsonObject();
        data.forEach(dataObject::put);
        JsonObject json = new JsonObject()
                .put("source", source)
                .put("name", name)
                .put("value", value)
                .put("data", dataObject);
        if (!children.isEmpty()) {
            JsonArray array = new JsonArray();
            json.put("children", array);
            children.forEach(c -> array.add(c.toJson()));
        }
        return json;
    }


    public static Markup fromJson(JsonObject json) {
        if (json == null) return null;
        Markup markup = new Markup(json.getString("source"), json.getString("name"), json.getString("value"));
        JsonArray array = json.getJsonArray("children");
        if (array != null) {
            for (int i = 0; i < array.size(); i++) {
                markup.children.add(Markup.fromJson(array.getJsonObject(i)));
            }
        }
        JsonObject data = json.getJsonObject("data", new JsonObject());
        data.fieldNames().forEach(f -> markup.data.put(f, data.getValue(f)));
        return markup;
    }
}
