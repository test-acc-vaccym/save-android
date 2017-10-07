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
import com.github.albalitz.save.utils.Utils;
import com.github.albalitz.save.utils.temporary_sharedpreference.TemporaryPreference;
import com.github.albalitz.save.utils.temporary_sharedpreference.TemporarySharedPreferenceHandler;

import java.util.Arrays;
import java.util.List;

/**
 * Created by albalitz on 3/24/17.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = SaveApplication.getAppContext();

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        /*
         * add fancy settings "buttons" that do stuff when clicking them
         */
        // export
        PreferenceCategory exportPreferenceCategory = (PreferenceCategory) getPreferenceManager().findPreference("pref_cat_export");
        Preference preference = new Preference(context);
        preference.setTitle(R.string.pref_export);
        preference.setSummary(R.string.pref_export_summary);
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                TemporarySharedPreferenceHandler.putTemporarySharedPreferenceValue(TemporaryPreference.EXPORT, true);
                Utils.showToast(context, "Your links will be exported when you view the saved list.");
                return true;
            }
        });
        exportPreferenceCategory.addPreference(preference);

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
}
