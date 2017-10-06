package com.github.albalitz.save.persistence.api;

import com.github.albalitz.save.SaveApplication;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Wrapper class for HTTP requests.
 */
public class Request {
    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, String username, String password, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.setBasicAuth(username, password);
        client.get(url, params, responseHandler);
    }

    public static void post(String url, String username, String password, JSONObject json, AsyncHttpResponseHandler responseHandler) throws UnsupportedEncodingException {
        if (username != null && password != null) {
            client.setBasicAuth(username, password);
        }

        // See http://stackoverflow.com/questions/13052036/posting-json-xml-using-android-async-http-loopj
        // for the example I based this on.
        client.post(SaveApplication.getAppContext(), url, new StringEntity(json.toString()), "application/json", responseHandler);
    }

    public static void delete(String url, String username, String password, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.setBasicAuth(username, password);
        client.delete(url, params, responseHandler);
    }
}
