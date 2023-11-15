package com.example.mqttgasalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(2000);
                    startActivity(new Intent(SplashScreen.this, MainActivity.class));
                    finish();
                } catch (Exception e) {
                    // Handle exceptions here
                    Toast.makeText(SplashScreen.this, "Erreur", Toast.LENGTH_SHORT).show();
                }
            }
        };
        thread.start();
    }
}
