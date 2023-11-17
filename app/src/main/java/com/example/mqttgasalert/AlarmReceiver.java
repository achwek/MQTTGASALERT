package com.example.mqttgasalert;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Action à effectuer lorsque l'alarme est déclenchée
        Toast.makeText(context, "Alarme déclenchée!", Toast.LENGTH_SHORT).show();

        // Exemple : faire vibrer l'appareil
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(2000);
        }

        // Exemple : jouer un son
        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound);
        mediaPlayer.start();
    }
}
