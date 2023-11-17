package com.example.mqttgasalert;


import android.Manifest;
import androidx.core.app.ActivityCompat;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.Vibrator;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraAccessException;  // Add this line for CameraAccessException
import androidx.core.content.ContextCompat;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.graphics.Color;
import android.app.PendingIntent;
import android.app.AlarmManager;
import android.util.Log;


public class SubscriberActivity extends AppCompatActivity {
    private LineChart mChart;
    private LineDataSet dataSet;
    private boolean isSOSActive = false;
    private CameraManager cameraManager;
    private String cameraId;
    private Handler sosHandler = new Handler(Looper.getMainLooper());
    private Handler alarmHandler = new Handler(Looper.getMainLooper()); // Ajoutez cette ligne pour déclarer la variable alarmHandler

    private int sosIndex = 0;
    private static final long ALARM_DURATION = 1000; // 5 minutes
    private static final long SOS_DELAY = 100;

    private String brokerAddress, brokerPort, gasTopic, threshold;
    private ListView gasValuesListView;
    private List<String> gasValuesList;
    private ArrayAdapter<String> gasValuesAdapter;

    private MqttClient mqttClient;
    private double thresholdValue = 0.0;
    private static final int MAX_LIST_SIZE = 15;
    // Déclarez MY_PERMISSIONS_REQUEST_VIBRATE en tant que variable de classe
    private static final int MY_PERMISSIONS_REQUEST_VIBRATE = 123;  // Vous pouvez choisir n'importe quel nombre entier
    private AlarmManager alarmManager; // Ajoutez cette ligne pour déclarer la variable alarmManager
    private PendingIntent alarmPendingIntent; // Ajoutez cette ligne pour déclarer la variable alarmPendingIntent
    private PendingIntent pendingIntent; // Déclarez la variable ici


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscriber);

        Intent intent = getIntent();

        brokerAddress = intent.getStringExtra("adressB");
        brokerPort = intent.getStringExtra("port");
        gasTopic = intent.getStringExtra("topic");
        threshold = intent.getStringExtra("seuil");
        gasValuesListView = findViewById(R.id.listViewGasValues);
        gasValuesList = new ArrayList<>();
        gasValuesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, gasValuesList);
        gasValuesListView.setAdapter(gasValuesAdapter);

        mChart = findViewById(R.id.mChart);
        dataSet = new LineDataSet(new ArrayList<>(), "Gas Values");
        LineData lineData = new LineData(dataSet);
        mChart.setData(lineData);
// Créer une intention pour l'alarme
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Toast.makeText(this, "La lampe de poche n'est pas disponible sur ce dispositif", Toast.LENGTH_SHORT).show();
            return;
        }

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return;
        }
// Demander la permission de vibration si elle n'est pas accordée
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.VIBRATE}, MY_PERMISSIONS_REQUEST_VIBRATE);
        }
        String thresholdStr = threshold.toString();
        if (!thresholdStr.isEmpty()) {
            thresholdValue = Double.parseDouble(thresholdStr);
        }

        connectToMQTTBroker();
        // Call the method to update UI with recent gas values
        updateUIWithRecentGasValues();
    }

    private void connectToMQTTBroker() {
        String brokerAd = brokerAddress.toString();
        String brokerP = brokerPort.toString();
        final String broker = "tcp://" + brokerAd.trim() + ":" + brokerP.trim();

        final String clientId = "AndroidCl" + System.currentTimeMillis();

        try {
            mqttClient = new MqttClient(broker, clientId, null);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            mqttClient.connect(options);

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    // Handle connection lost
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    if (topic.equals(gasTopic.toString())) {
                        String gasValueStr = new String(message.getPayload());
                        displayGasValue(gasValueStr);

                        double gasValue = Double.parseDouble(gasValueStr);
                        if (gasValue > thresholdValue) {
                            handleGasExceedingThreshold(gasValue);
                        }
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Not used in this example
                }
            });

            mqttClient.subscribe(gasTopic.toString(), 0);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void displayGasValue(final String value) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                updateChart(Double.parseDouble(value));
                updateGasValuesList(value);
            }
        });
    }
    private void updateGasValuesList(String value) {
        // Format the date and time
        String formattedDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Add the gas value and date to the list
        String gasValueWithDate = value + " (" + formattedDateTime + ")";
        // Add the gas value at the beginning of the list
        gasValuesList.add(0, gasValueWithDate);
        // Create a Gas object and insert it into the database
        Gas gas = new Gas(value, formattedDateTime);
        DatabaseHelperGas dbHelper = new DatabaseHelperGas(this);
        dbHelper.insertGasValue(gas);


        // Limit the list size to MAX_LIST_SIZE
        if (gasValuesList.size() > MAX_LIST_SIZE) {
            gasValuesList.remove(gasValuesList.size() - 1); // Remove the oldest element

        }
        // Notify the adapter that the data set has changed
        gasValuesAdapter.notifyDataSetChanged();
    }
    private void updateChart(double gasValue) {
        LineData data = mChart.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), (float) gasValue), 0);

            mChart.notifyDataSetChanged();
            mChart.invalidate();
        }
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Gas Values");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.RED);
        set.setCircleColor(Color.RED);
        set.setLineWidth(2f);
        set.setCircleRadius(3f);
        set.setValueTextSize(10f);
        set.setDrawValues(false);
        return set;
    }



    private void vibrateDevice() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(5000);
        }
    }

    private void toggleSOS() {
        isSOSActive = true;
        startSOS();
    }

    private void startSOS() {
        sosIndex = 0;
        sosHandler.post(sosRunnable);
    }



    private void turnOnFlashlight() {
        try {
            cameraManager.setTorchMode(cameraId, true);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void turnOffFlashlight() {
        try {
            cameraManager.setTorchMode(cameraId, false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Runnable sosRunnable = new Runnable() {
        @Override
        public void run() {
            if (sosIndex < 50) {
                if (sosIndex % 2 == 0) {
                    turnOnFlashlight();
                } else {
                    turnOffFlashlight();
                }

                sosIndex++;
                sosHandler.postDelayed(this, SOS_DELAY);
            } else {
                isSOSActive = false;
            }
        }
    };



    private void updateUIWithRecentGasValues() {
        DatabaseHelperGas dbHelper = new DatabaseHelperGas(this);
        List<Gas> recentGasValues = dbHelper.getRecentGasValues();

        // Update ListView
        for (Gas gas : recentGasValues) {
            String gasValueWithDate = gas.getValue() + " (" + gas.getDateTime() + ")";
            gasValuesList.add(0, gasValueWithDate);
        }
        gasValuesAdapter.notifyDataSetChanged();

        // Update Chart
        for (Gas gas : recentGasValues) {
            updateChart(Double.parseDouble(gas.getValue()));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSOS();
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_VIBRATE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission accordée, vous pouvez maintenant utiliser la vibration
                } else {
                    // Permission refusée, gestion en conséquence
                    Toast.makeText(this, "Permission de vibration refusée", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            // autres cas de permission ici si nécessaire
        }
    }

    private void handleGasExceedingThreshold(final double gasValue) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (gasValue > thresholdValue) {
                    // Lire les préférences partagées
                    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

                    // Vérifier si la vibration est activée
                    boolean vibrationEnabled = sharedPreferences.getBoolean("vibration_enabled", true);
                    if (vibrationEnabled) {
                        vibrateDevice();
                    }

                    // Vérifier si le SOS est activé
                    boolean sosEnabled = sharedPreferences.getBoolean("sos_enabled", true);
                    if (sosEnabled) {
                        toggleSOS();

                        // Récupérer la durée du SOS depuis les préférences partagées
                        long sosDuration = sharedPreferences.getLong("sos_duration", 300);
                        scheduleAlarm(); // Démarrez l'alarme
                        scheduleAlarmCancellation(sosDuration * 1000); // Convertissez la durée en millisecondes
                    }
                }
            }
        });
    }


    private void scheduleAlarm() {
        // Vérifiez si la permission VIBRATE est accordée
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {

            // Obtenez l'instance de AlarmManager
            alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            // Définissez l'heure à laquelle déclencher l'alarme (par exemple, immédiatement)
            long triggerTime = SystemClock.elapsedRealtime();

            // Programmez l'alarme
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent);
                }
            }
        } else {
            // La permission de vibration n'est pas accordée, gérer en conséquence
            Toast.makeText(this, "Permission de vibration manquante pour l'alarme", Toast.LENGTH_SHORT).show();
        }
    }
    private void stopSOS() {
        Log.d("SubscriberActivity", "Stopping SOS");
        sosHandler.removeCallbacks(sosRunnable);
        turnOffFlashlight();

        if (alarmPendingIntent != null) {
            Log.d("SubscriberActivity", "Canceling alarm");
            cancelAlarm(); // Arrêter l'alarme lorsque vous arrêtez le SOS
        }
    }


    private void scheduleAlarmCancellation(long delayMillis) {
        Log.d("SubscriberActivity", "Scheduling alarm cancellation");
        // Planifiez l'annulation de l'alarme après la durée spécifiée
        alarmHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cancelAlarm();
            }
        }, delayMillis);
    }
    private void cancelAlarm() {
        Log.d("SubscriberActivity", "Canceling the alarm");
        if (alarmManager != null && alarmPendingIntent != null) {
            alarmManager.cancel(alarmPendingIntent);
        }
    }


}