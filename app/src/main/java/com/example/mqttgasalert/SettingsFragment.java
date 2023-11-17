package com.example.mqttgasalert;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import android.content.SharedPreferences;

public class SettingsFragment extends PreferenceFragmentCompat {

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        // Initialiser les commutateurs avec les valeurs stockées dans SharedPreferences
        SwitchPreferenceCompat switchAlarm = findPreference("alarm_state");
        SwitchPreferenceCompat switchVibration = findPreference("vibration_state");
        SwitchPreferenceCompat switchLight = findPreference("light_state");

        switchAlarm.setChecked(sharedPreferences.getBoolean("alarm_state", false));
        switchVibration.setChecked(sharedPreferences.getBoolean("vibration_state", false));
        switchLight.setChecked(sharedPreferences.getBoolean("light_state", false));

        // Ajouter des écouteurs pour enregistrer les changements dans SharedPreferences
        switchAlarm.setOnPreferenceChangeListener((preference, newValue) -> {
            sharedPreferences.edit().putBoolean("alarm_state", (Boolean) newValue).apply();
            return true;
        });

        switchVibration.setOnPreferenceChangeListener((preference, newValue) -> {
            sharedPreferences.edit().putBoolean("vibration_state", (Boolean) newValue).apply();
            return true;
        });

        switchLight.setOnPreferenceChangeListener((preference, newValue) -> {
            sharedPreferences.edit().putBoolean("light_state", (Boolean) newValue).apply();
            return true;
        });
    }
}
