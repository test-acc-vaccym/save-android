package com.github.albalitz.save.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.albalitz.save.SaveApplication;
import com.github.albalitz.save.persistence.api.Request;
import com.github.albalitz.save.utils.Utils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by albalitz on 10/17/17.
 */
public class DeauthAllTokensActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.prefs = SaveApplication.getSharedPreferences();

        deauthAllTokens();
        finish();
    }

    private void deauthAllTokens() {
        String url = this.prefs.getString("pref_key_api_url", null);
        if (url == null) {
            Log.e(this.toString(), "No URL set in the preferences!");
            return;
        } else {
            url += "/token/invalidate/all";
        }

        JsonHttpResponseHandler jsonHttpResponseHandler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("api", "Got response from the API: " + response);
                try {
                    String statusmessage = response.getString("success");
                    Utils.showToast(getApplicationContext(), statusmessage);
                } catch (JSONException e) {
                    Log.d(this.toString(), e.toString());
                    Utils.showToast(getApplicationContext(), "Error trying to to decode JSON response. Please notify your API admin.");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(this.toString(), "Can't delete auth tokens.");
                Utils.showToast(getApplicationContext(), "Can't reach Save API.");
            }
        };

        Log.i(this.toString(), "Deleting all auth tokens ...");
        Request.delete(url,
                prefs.getString("pref_key_api_username", null),
                prefs.getString("pref_key_api_password", null),
                null,  // request params
                jsonHttpResponseHandler);
    }
}
