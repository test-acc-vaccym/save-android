package com.github.albalitz.save.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.ListView;

import com.github.albalitz.save.R;
import com.github.albalitz.save.SaveApplication;
import com.github.albalitz.save.activities.MainActivity;
import com.github.albalitz.save.activities.RegisterActivity;
import com.github.albalitz.save.utils.Utils;
import com.github.albalitz.save.utils.temporary_sharedpreference.TemporaryPreference;
import com.github.albalitz.save.utils.temporary_sharedpreference.TemporarySharedPreferenceHandler;

import java.util.Arrays;
import java.util.List;

/**
 * Created by albalitz on 3/24/17.
 */
public class SettingsFragment extends PreferenceFragment {

    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = SaveApplication.getAppContext();

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        /*
         * add fancy settings "buttons" that do stuff when clicking them
         * TODO: move to activities and start a corresponding intent (see e.g. DeauthAllTokensActivity)
         */
        PreferenceCategory importExportPreferenceCategory = (PreferenceCategory) getPreferenceManager().findPreference("pref_cat_importexport");
        // export
        Preference exportPref = new Preference(context);
        exportPref.setTitle(R.string.pref_export);
        exportPref.setSummary(R.string.pref_export_summary);
        exportPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                TemporarySharedPreferenceHandler.putTemporarySharedPreferenceValue(TemporaryPreference.EXPORT, true);
                switchToMainActivity();
                return true;
            }
        });
        importExportPreferenceCategory.addPreference(exportPref);
        // import
        Preference importPref = new Preference(context);
        importPref.setTitle(R.string.pref_import);
        importPref.setSummary(R.string.pref_import_summary);
        importPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                TemporarySharedPreferenceHandler.putTemporarySharedPreferenceValue(TemporaryPreference.IMPORT, true);
                switchToMainActivity();
                return true;
            }
        });
        importExportPreferenceCategory.addPreference(importPref);


        // dangerous stuff
        PreferenceScreen dangerZone = (PreferenceScreen) getPreferenceManager().findPreference("pref_cat_danger_zone");

        // delete all links
        Preference deleteAllPref = new Preference(context);
        deleteAllPref.setTitle(R.string.pref_delete_all);
        deleteAllPref.setSummary(R.string.pref_delete_all_summary);
        deleteAllPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                TemporarySharedPreferenceHandler.putTemporarySharedPreferenceValue(TemporaryPreference.DELETE_ALL, true);
                switchToMainActivity();
                return true;
            }
        });
        dangerZone.addPreference(deleteAllPref);

        setValuesAsSummary();
    }

    /**
     * Get the values of specific preferences and set those as the summary,
     * so the values are visible on the preferences screen.
     */
    private void setValuesAsSummary() {
        List<EditTextPreference> editTextPreferences = Arrays.asList(
                (EditTextPreference) findPreference("pref_key_api_url"),
                (EditTextPreference) findPreference("pref_key_api_username")
        );

        for (EditTextPreference pref : editTextPreferences) {
            String setValue = pref.getText();
            if (setValue != null && !setValue.isEmpty()) {
                pref.setSummary(setValue);
            }
        }
    }

    private void switchToMainActivity() {
        Intent intent = new Intent(context, MainActivity.class);
        startActivity(intent);
    }
}
