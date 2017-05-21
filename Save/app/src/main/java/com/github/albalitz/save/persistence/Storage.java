package com.github.albalitz.save.persistence;

import android.content.SharedPreferences;
import android.util.Log;

import com.github.albalitz.save.SaveApplication;
import com.github.albalitz.save.activities.ApiActivity;
import com.github.albalitz.save.persistence.api.Api;
import com.github.albalitz.save.persistence.database.Database;

/**
 * Access the configured storage setting choice.
 */
public class Storage {

    private static SharedPreferences prefs = SaveApplication.getSharedPreferences();

    public static SavePersistenceOption getStorageSettingChoice(ApiActivity context) {
        SavePersistenceOption storage;
        if (prefs.getBoolean("pref_key_use_api_or_local", false)) {
            Log.d("Storage", "Storage method: API.");
            storage = new Api(context);
        } else {
            Log.d("Storage", "Storage method: database.");
            storage = new Database(context);
        }
        return storage;
    }
}
