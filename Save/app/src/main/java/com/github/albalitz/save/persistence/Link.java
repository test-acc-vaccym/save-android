package com.github.albalitz.save.persistence;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Representation of what is saved and returned by the api
 */
public class Link {
    private int id;
    private String url;
    private String annotation;
    private String createdOn;
    private String updatedOn;

    /**
     * Create a Link instance from a JSONObject,
     * e.g. an API response.
     */
    public Link(JSONObject json) throws JSONException {
        try {
            this.id = json.getInt("id");
            this.createdOn = json.getString("created_on");
            this.updatedOn = json.getString("updated_on");
        } catch (JSONException e) {
            Log.w("Link", "JSON doesn't contain ID and/or dates.");
        }
        this.url = json.getString("url");
        this.annotation = json.getString("annotation");
    }

    /**
     * Create a Link instance from attributes,
     * including the id, e.g. from the local database.
     * @param id
     * @param url
     * @param annotation
     */
    public Link(int id, String url, String annotation) {
        this.id = id;
        this.url = url;
        this.annotation = annotation;
    }

    /**
     * Create a Link instance from its attributes,
     * e.g. when creating one using a button.
     */
    public Link(String url, String annotation) {
        this.url = url;
        this.annotation = annotation;
    }

    public String toString() {
        return this.annotation + " (" + this.url + ")";
    }

    public JSONObject json() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("url", this.url);
        json.put("annotation", this.annotation);

        return json;
    }

    public int id() {
        return id;
    }

    public String url() {
        return url;
    }

    public String annotation() {
        return annotation;
    }

    public String createdOn() {
        return createdOn;
    }

    public String updatedOn() {
        return updatedOn;
    }
}
