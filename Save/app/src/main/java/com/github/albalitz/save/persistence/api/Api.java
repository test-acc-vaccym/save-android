package com.github.albalitz.save.persistence.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.github.albalitz.save.SaveApplication;
import com.github.albalitz.save.activities.ApiActivity;
import com.github.albalitz.save.activities.SnackbarActivity;
import com.github.albalitz.save.persistence.ApiUser;
import com.github.albalitz.save.persistence.Link;
import com.github.albalitz.save.persistence.SavePersistenceOption;
import com.github.albalitz.save.persistence.offline_queue.OfflineQueue;
import com.github.albalitz.save.utils.Utils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import cz.msebera.android.httpclient.Header;

/**
 * Created by albalitz on 3/24/17.
 */
public class Api implements SavePersistenceOption {

    private SharedPreferences prefs;
    private ApiActivity callingActivity;
    private ApiUser caller;

    private OfflineQueue offlineQueue;

    public Api(ApiActivity callingActivity) {
        this.prefs = SaveApplication.getSharedPreferences();
        this.callingActivity = callingActivity;
        this.offlineQueue = new OfflineQueue((Context) callingActivity);
    }

    public Api(ApiUser caller) {
        this.caller = caller;
    }

    // todo: edit a link

    @Override
    public void updateSavedLinks() {
        String url = this.prefs.getString("pref_key_api_url", null);
        if (url == null) {
            Log.e(this.toString(), "No URL set in the preferences!");
            return;
        } else {
            url += "/links";
        }

        JsonHttpResponseHandler jsonHttpResponseHandler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d("api", "Got " + response.length() + " links from the API: " + response);
                ArrayList<Link> savedLinks = new ArrayList<>();

                for (int i = 0; i < response.length(); i++) {
                    JSONObject linkJson = new JSONObject();

                    try {
                        linkJson = response.getJSONObject(i);
                    } catch (JSONException e) {
                        Log.e("api", e.toString());
                    }

                    if (linkJson != null) {
                        Link link = null;
                        try {
                            link = new Link(linkJson);
                        } catch (JSONException e) {
                            Log.e("api", "JSONException while trying to create Link from JSON: " + e.toString());
                        }
                        savedLinks.add(link);
                    }
                }

                if (callingActivity != null) {
                    callingActivity.onSavedLinksUpdate(savedLinks);
                } else {
                    caller.onSavedLinksUpdate(savedLinks);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(this.toString(), "Can't update links from API.");
                if (callingActivity != null) {
                    Utils.showToast((Context) callingActivity, "Can't reach API.");
                }
                callingActivity.onSavedLinksUpdate(new ArrayList<Link>());  // cause the same callbacks as on success but without any links
            }
        };

        Log.i("api", "Getting saved list from API: " + url + " ...");
        Request.get(url,
                prefs.getString("pref_key_api_username", null),
                prefs.getString("pref_key_api_password", null),
                null,  // request params
                jsonHttpResponseHandler);
    }


    @Override
    public void saveLink(final Link link) throws JSONException, UnsupportedEncodingException {
        Log.d("api", "Saving link: " + link.toString() + " ...");

        String url = this.prefs.getString("pref_key_api_url", null);
        if (url == null) {
            Log.e(this.toString(), "No URL set in the preferences!");
            return;
        } else {
            url += "/save";
        }

        JsonHttpResponseHandler jsonHttpResponseHandler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (!response.has("success")) {
                    // todo: show error
                }

                try {
                    Utils.showSnackbar((SnackbarActivity) callingActivity, "Link saved.");
                } catch (ClassCastException e) {
                    // this happens, when sharing to the app and using this from the SaveLinkActivity.
                    // using a toast instead, as they can be displayed on top of another app
                    Utils.showToast((Context) callingActivity, "Link saved.");
                }

                // If this was queued, it is no longer needed
                offlineQueue.dropLink(link);

                // also update the list view
                updateSavedLinks();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e("api.saveLink failure", "No connection?");

                offlineQueue.addLink(link);

                if (callingActivity != null) {
                    Utils.showToast((Context) callingActivity, "Can't save link! Queueing and trying again later.");
                }
                callingActivity.onSavedLinksUpdate(new ArrayList<Link>());  // cause the same callbacks as on success but without any links
            }
        };

        Request.post(url,
                prefs.getString("pref_key_api_username", null),
                prefs.getString("pref_key_api_password", null),
                link.json(),
                jsonHttpResponseHandler);
    }


    @Override
    public void deleteLink(Link link) {
        Log.d("api", "Deleting link: " + link.toString() + " ...");

        String url = this.prefs.getString("pref_key_api_url", null);
        if (url == null) {
            Log.e(this.toString(), "No URL set in the preferences!");
            return;
        } else {
            url += "/links/" + link.id();
        }

        JsonHttpResponseHandler jsonHttpResponseHandler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (!response.has("success")) {
                    // todo: show error
                }

                if (callingActivity != null) {
                    Utils.showSnackbar((SnackbarActivity) callingActivity, "Deleted link.");

                    // also update the list view
                    updateSavedLinks();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Utils.showToast((Context) callingActivity, "Deleting link failed. Try again later.");
                callingActivity.onSavedLinksUpdate(new ArrayList<Link>());  // cause the same callbacks as on success but without any links
            }
        };

        Log.i("api", "Deleting link: " + link.toString() + " ...");
        Request.delete(url,
                prefs.getString("pref_key_api_username", null),
                prefs.getString("pref_key_api_password", null),
                null,  // request params
                jsonHttpResponseHandler);
    }


    public void registerUser(final String username, final String password) throws JSONException, UnsupportedEncodingException {
        Log.d("api", "Registering user: " + username + " ...");

        String url = this.prefs.getString("pref_key_api_url", null);
        if (url == null) {
            Log.e(this.toString(), "No URL set in the preferences!");
            return;
        } else {
            url += "/register";
        }

        JsonHttpResponseHandler jsonHttpResponseHandler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (!response.has("success")) {
                    // todo: show error
                }

                Log.i("api register success", "Persisting newly registered user to preferences.");
                prefs.edit()
                        .putString("pref_key_api_username", username)
                        .putString("pref_key_api_password", password)
                        .apply();

                if (callingActivity != null) {
                    callingActivity.onRegistrationSuccess();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e("api.register failure", errorResponse.toString());
            }
        };

        JSONObject json = new JSONObject();
        json.put("uname", username);
        json.put("pass", password);
        Request.post(url,
                prefs.getString("pref_key_api_username", null),
                prefs.getString("pref_key_api_password", null),
                json,
                jsonHttpResponseHandler);
    }
}
