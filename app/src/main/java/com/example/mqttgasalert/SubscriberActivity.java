package com.example.mqttgasalert;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import org.eclipse.paho.client.mqttv3.*;
import android.content.Intent;
// Add these imports if they are not already present
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.components.YAxis;
import android.graphics.Color;
import java.util.ArrayList;



public class SubscriberActivity extends AppCompatActivity {
    //chart declaration
    private LineChart mChart;
    private LineDataSet dataSet;
    //sos declaration
    private boolean isSOSActive = false;
    private CameraManager cameraManager;
    private String cameraId;
    private Handler sosHandler = new Handler(Looper.getMainLooper());
    private int sosIndex = 0;
    private static final long SOS_DELAY = 100; // Durée entre les clignotements en millisecondes

    //declaration de parametre de broker port topic et seuil
    private String brokerAddress, brokerPort, gasTopic, threshold;
    private TextView gasValueTextView;

    private MqttClient mqttClient;
    private double thresholdValue = 0.0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscriber);
// Récupérer l'intention qui a démarré cette activité
        Intent intent = getIntent();

        brokerAddress = intent.getStringExtra("adressB");
        brokerPort = intent.getStringExtra("port");
        gasTopic= intent.getStringExtra("topic");
        threshold = intent.getStringExtra("seuil");
        gasValueTextView = findViewById(R.id.gasValueTextView);

        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Set the threshold value
                String thresholdStr = threshold.toString();
                if (!thresholdStr.isEmpty()) {
                    thresholdValue = Double.parseDouble(thresholdStr);
                }

                // Connect to MQTT broker
                startButtonClick(view);
            }
        });
        //*******chart oncreate
        mChart = findViewById(R.id.mChart);

        // Set up the line chart
        dataSet = new LineDataSet(new ArrayList<>(), "Gas Values");
        LineData lineData = new LineData(dataSet);
        mChart.setData(lineData);


        // Vérifiez si le dispositif a une fonction de lampe de poche
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Toast.makeText(this, "La lampe de poche n'est pas disponible sur ce dispositif", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialisez le gestionnaire de caméra
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return;
        }


    }

    private void connectToMQTTBroker() {
        String brokerAd = brokerAddress.toString();
        String brokerP = brokerPort.toString();
        final String broker = "tcp://" + brokerAd + ":" + brokerP;
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
                    // Handle incoming messages
                    if (topic.equals(gasTopic.toString())) {
                        String gasValueStr = new String(message.getPayload());
                        displayGasValue(gasValueStr);

                        // Check if the gas value exceeds the threshold
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

    /*private void displayGasValue(final String value) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                gasValueTextView.setText(value);
                Toast.makeText(getApplicationContext(), value, Toast.LENGTH_LONG).show();
            }
        });
    }*/

    private void handleGasExceedingThreshold(final double gasValue) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (gasValue > thresholdValue) {
                    // Vibrate the device
                    vibrateDevice();
                    toggleSOS();

                }
            }
        });
    }



    //begin code chart
    private void displayGasValue(final String value) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                // Update the TextView
                gasValueTextView.setText(value);

                // Update the LineChart
                updateChart(Double.parseDouble(value));
            }
        });
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

            // Notify chart data has changed
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



    //*******fincode chart
    private void vibrateDevice() {
        // Get the Vibrator service
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Check if the device supports vibration
        if (vibrator != null && vibrator.hasVibrator()) {
            // Vibrate for 5000 milliseconds (5 seconds)
            vibrator.vibrate(5000);
        }
    }

    private void showGasExceedingThresholdNotification(double gasValue) {// Create a notification channel if the device is running Android Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "GasNotificationChannel";
            CharSequence channelName = "Gas Notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "GasNotificationChannel")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Gas Alert!")
                .setContentText("Gas value exceeds threshold: " + gasValue)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

// Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }

    public void startButtonClick(View view) {
        // Set the threshold value
        String thresholdStr = threshold.toString();
        if (!thresholdStr.isEmpty()) {
            thresholdValue = Double.parseDouble(thresholdStr);
        }

        // Connect to MQTT broker
        connectToMQTTBroker();

        // Optionally, you can add additional logic here if needed
        // For example, you might want to update the UI or perform other actions.
    }


//alert sos

    private void toggleSOS() {
        isSOSActive = true;
        startSOS();
    }

 /*  private void toggleSOS() {

        isSOSActive = !isSOSActive;

        if (isSOSActive) {
            startSOS();
        } else {
            stopSOS();
        }
    }*/

    private void startSOS() {
        sosIndex = 0;
        sosHandler.post(sosRunnable);
    }


    private void stopSOS() {
        sosHandler.removeCallbacks(sosRunnable);
        turnOffFlashlight();
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

            if (sosIndex < 50) { // Répétez pour former le motif SOS
                if (sosIndex % 2 == 0) {
                    // Tournez la torche
                    turnOnFlashlight();
                } else {
                    // Éteignez la torche
                    turnOffFlashlight();
                }

                sosIndex++;
                sosHandler.postDelayed(this, SOS_DELAY);
            } else {
                isSOSActive = false;
            }

        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSOS();
    }

}
