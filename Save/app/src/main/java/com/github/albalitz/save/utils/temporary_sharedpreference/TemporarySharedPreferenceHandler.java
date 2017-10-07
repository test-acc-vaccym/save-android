package com.github.albalitz.save.utils.temporary_sharedpreference;

import android.content.SharedPreferences;
import android.util.Log;

import com.github.albalitz.save.SaveApplication;

/**
 * Handles putting and popping temporary values to the shared preferences.
 * This allows setting things like flags to run a certain action from places that don't have access
 * to certain things, because I currently don't know a better way to do that.
 */
public class TemporarySharedPreferenceHandler {

    public static boolean putTemporarySharedPreferenceValue(TemporaryPreference key, boolean value) {
        SharedPreferences preferences = SaveApplication.getSharedPreferences();
        if (preferences.contains(key.pref)) {
            return false;
        }

        Log.d("TempSharedPrefHandler", "Setting temporary preference: " + key + " to '" + value + "'");
        preferences.edit().putBoolean(key.pref, value).apply();
        return true;
    }

    public static boolean popTemporarySharedPreferenceValue(TemporaryPreference key) {
        SharedPreferences preferences = SaveApplication.getSharedPreferences();
        boolean value = preferences.getBoolean(key.pref, false);
        preferences.edit().remove(key.pref).apply();
        Log.d("TempSharedPrefHandler", "Removed temporary preference: " + key + " with value '" + value + "'");
        return value;
    }

}
