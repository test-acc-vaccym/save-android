package com.github.albalitz.save;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by albalitz on 3/24/17.
 */
public class SaveApplication extends Application {
    private static Context context;

    public static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 42;

    @Override
    public void onCreate() {
        super.onCreate();
        SaveApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        // todo: may need some handling of context being null and stuff
        return SaveApplication.context;
    }

    public static void setAppContext(Context context) {
        SaveApplication.context = context;
    }

    public static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(SaveApplication.getAppContext());
    }
}
