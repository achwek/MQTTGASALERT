package com.example.mqttgasalert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private Switch switchAlarm, switchVibration, switchLight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialiser les vues
        TextView titre = findViewById(R.id.titre);
        switchAlarm = findViewById(R.id.switchAlarm);
        switchVibration = findViewById(R.id.switchVibration);
        switchLight = findViewById(R.id.switchLight);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Titre de la page de paramètres
        titre.setText("Settings");

        // Initialiser les commutateurs avec les valeurs stockées dans SharedPreferences
        switchAlarm.setChecked(sharedPreferences.getBoolean("alarm_state", false));
        switchVibration.setChecked(sharedPreferences.getBoolean("vibration_state", false));
        switchLight.setChecked(sharedPreferences.getBoolean("light_state", false));

        // Ajouter des écouteurs pour enregistrer les changements dans SharedPreferences
        switchAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("alarm_state", isChecked);
            editor.apply();
        });

        switchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("vibration_state", isChecked);
            editor.apply();
        });

        switchLight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("light_state", isChecked);
            editor.apply();
        });



        //navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_settings);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.bottom_listSubscribers) {
                startActivity(new Intent(getApplicationContext(), ListSubscriberActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.bottom_settings) {
                // Vous êtes déjà dans l'activité SettingsActivity, pas besoin d'actions supplémentaires.
                return true;
            }
            return false;
        });
    }
}
