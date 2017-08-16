package com.github.albalitz.save.persistence.api;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.github.albalitz.save.SaveApplication;
import com.github.albalitz.save.persistence.Link;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by albalitz on 8/15/17.
 */
public class OfflineQueue {

    private static final String QUEUE_KEY = "offline_queue";

    private static SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SaveApplication.getAppContext());

    public static void addLink(Link link) throws JSONException {
        Set<String> queuedLinks = preferences.getStringSet(QUEUE_KEY, new HashSet<String>());
        queuedLinks.add(link.json().toString());
        Editor preferenceEditor = preferences.edit();
        preferenceEditor.putStringSet(QUEUE_KEY, queuedLinks);
        preferenceEditor.apply();
        Log.d("OfflineQueue", "Queued link. Now in queue: " + queuedLinks.size());
    }

    public static void dropLink(Link link) throws JSONException {
        Set<String> queuedLinks = preferences.getStringSet(QUEUE_KEY, new HashSet<String>());
        queuedLinks.remove(link.json().toString());
        Editor preferenceEditor = preferences.edit();
        preferenceEditor.putStringSet(QUEUE_KEY, queuedLinks);
        preferenceEditor.apply();
        Log.d("OfflineQueue", "Dropped link: " + link + ". Now in queue: " + queuedLinks.size());
    }

    public static List<Link> getLinks() throws JSONException {
        Set<String> queuedLinks = preferences.getStringSet(QUEUE_KEY, new HashSet<String>());
        ArrayList<Link> links = new ArrayList<>();
        for (String linkJsonString : queuedLinks) {
            JSONObject linkJson = new JSONObject(linkJsonString);
            Link link = new Link(linkJson);
            links.add(link);
        }
        Log.d("OfflineQueue", "Found " + links.size() + " queued links.");
        return links;
    }

}
