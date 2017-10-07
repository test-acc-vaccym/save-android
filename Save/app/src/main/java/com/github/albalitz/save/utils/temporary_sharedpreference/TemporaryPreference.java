package com.github.albalitz.save.utils.temporary_sharedpreference;

/**
 * Only allow specific keys as temporary preferences.
 * This avoids accidentally deleting something important by setting a custom key.
 */
public enum TemporaryPreference {
    EXPORT("export");

    public final String pref;

    private static final String KEY_PREFIX = "tmp_";

    TemporaryPreference(String temporaryPreference) {
        this.pref = KEY_PREFIX + temporaryPreference;
    }
}
