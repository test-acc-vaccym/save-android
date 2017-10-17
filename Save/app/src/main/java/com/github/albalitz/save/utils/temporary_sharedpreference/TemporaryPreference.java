package com.github.albalitz.save.utils.temporary_sharedpreference;

/**
 * Only allow specific keys as temporary preferences.
 * This avoids accidentally deleting something important by setting a custom key.
 */
public enum TemporaryPreference {
    DELETE_ALL("delete_all"),
    EXPORT("export"),
    IMPORT("import");

    public final String pref;

    private static final String KEY_PREFIX = "tmp_";

    TemporaryPreference(String temporaryPreference) {
        this.pref = KEY_PREFIX + temporaryPreference;
    }
}
