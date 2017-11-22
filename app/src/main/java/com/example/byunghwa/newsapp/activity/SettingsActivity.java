package com.example.byunghwa.newsapp.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.byunghwa.newsapp.R;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content,
                        new MainSettingsFragment()).commit();
    }

    public static class MainSettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        public static final int keyPreference = R.string.pref_topic;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            preferences.registerOnSharedPreferenceChangeListener(this);

            // set summary on startup
            Preference foodPref = findPreference(getString(R.string.pref_topic, ""));
            foodPref.setSummary(preferences.getString(getString(R.string.pref_topic, ""), ""));
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            preferences.unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.i("SettingsActi", "sharedprefs changed...");
            if (key.equals(getString(keyPreference)) ) {

                // set summary
                Preference foodPref = findPreference(key);
                foodPref.setSummary(sharedPreferences.getString(key, ""));

                // we've changed the sort order
                // restart the main activity
                getActivity().finish();
                final Intent intent = IntentCompat.makeMainActivity(new ComponentName(
                        getActivity(), MainActivity.class));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }

    }



}
