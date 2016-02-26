package com.aggregate.api;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Represents user's phrase matching results.
 *
 * Each Markup can contain an inner markups representing the semantics tree.
 * You can traverse through this tree by get and getAll methods.
 *
 * @author morfeusys
 */
public class Markup {
    /**
     * Source sub-string of this markup
     */
    public final String source;

    /**
     * Markup's name (could be null)
     */
    public final String name;

    /**
     * Canonical value (could be null)
     */
    public final String value;

    /**
     * Markup converting results (for standard patterns only)
     */
    public final Map<String, Object> data = new HashMap<>();

    /**
     * Inner markups
     */
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

    /**
     * Finds all markups with specified name
     * @param name markup's name
     * @return all markups inside this one and it's children with such name
     */
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

    /**
     * Fetches children markups in the order of appearing in the source text
     * @return list of children markups
     */
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

    /**
     * Finds markup by name.
     * Returns this markup or one of children.
     * @param name markup's name
     * @return markup or null
     */
    public Markup get(String name) {
        if(name.equals(this.name)) return this;
        for(Markup markup : children)
            if(name.equals(markup.name)) return markup;
        return null;
    }

    /**
     * Checks if this markup or one of children has specified name
     * @param name markup's name
     * @return true if this markup or one of children has specified name
     */
    public boolean has(String name) {
        if(name.equals(this.name)) return true;
        for(Markup markup : children)
            if(name.equals(markup.name)) return true;
        return false;
    }

    /**
     * Checks if the source of this markup is empty
     * @return true if source is empty
     */
    public boolean isBlank() {
        return source.isEmpty();
    }

    /**
     * Fetches child by index
     * @param index child index
     * @return child markup or null if no inner markups or index is greater or equal the children size
     */
    public Markup get(int index) {
        return isEmpty() ? null : children.size() > index ? children.get(index) : null;
    }

    /**
     * Checks if this markup doesn't contain inner markups
     * @return true if children is empty
     */
    public boolean isEmpty() {
        return children.isEmpty();
    }

    /**
     * Converts canonical value to Integer
     * @return Integer value
     */
    public int getIntValue() {
        return Integer.parseInt(value);
    }

    @Override
    public String toString() {
        return source;
    }

    /**
     * Converts this markup to Json object
     * @return JsonObject instance
     */
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

    /**
     * Converts JsonObject to Markup instance
     * @param json JsonObject instance
     * @return Markup instance
     */
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
