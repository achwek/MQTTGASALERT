package com.example.mqttgasalert;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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
                // You are already in the SettingsActivity, no need for additional actions.
                return true;
            }
            return false;
        });

    }
}
