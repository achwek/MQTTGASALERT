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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import org.eclipse.paho.client.mqttv3.*;
import android.content.Intent;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.components.YAxis;
import android.graphics.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SubscriberActivity extends AppCompatActivity {
    private LineChart mChart;
    private LineDataSet dataSet;
    private boolean isSOSActive = false;
    private CameraManager cameraManager;
    private String cameraId;
    private Handler sosHandler = new Handler(Looper.getMainLooper());
    private int sosIndex = 0;
    private static final long SOS_DELAY = 100;

    private String brokerAddress, brokerPort, gasTopic, threshold;
    private ListView gasValuesListView;
    private List<String> gasValuesList;
    private ArrayAdapter<String> gasValuesAdapter;

    private MqttClient mqttClient;
    private double thresholdValue = 0.0;

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

        String thresholdStr = threshold.toString();
        if (!thresholdStr.isEmpty()) {
            thresholdValue = Double.parseDouble(thresholdStr);
        }
        try {

            connectToMQTTBroker();

        } catch (MqttException e) {
            e.printStackTrace();
            Toast.makeText(SubscriberActivity.this, "Failed to connect to the MQTT broker", Toast.LENGTH_SHORT).show();
        }


            loadRecentGasValues();




    }

    private void connectToMQTTBroker() throws MqttException {
        String brokerAd = brokerAddress.toString();
        String brokerP = brokerPort.toString();
        final String broker = "tcp://" + brokerAd + ":" + brokerP;
        final String clientId = "AndroidCl" + System.currentTimeMillis();

        try {
            mqttClient = new MqttClient(broker, clientId, null);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            mqttClient.connect(options);
            // Chargez les 10 dernières valeurs de la base de données et mettez à jour le graphique
            loadRecentGasValues();

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
            throw e; // throw the exception to be caught by the calling method
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
        gasValuesList.add(0, gasValueWithDate);  // Ajoutez le nouvel élément en haut de la liste

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
        LineDataSet set = new LineDataSet(new ArrayList<>(), "Gas Values");

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.RED);
        set.setCircleColor(Color.RED);
        set.setLineWidth(2f);
        set.setCircleRadius(3f);
        set.setValueTextSize(10f);
        set.setDrawValues(false);

        return set;
    }


    private void handleGasExceedingThreshold(final double gasValue) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (gasValue > thresholdValue) {
                    vibrateDevice();
                    toggleSOS();
                }
            }
        });
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSOS();
    }
    private void loadRecentGasValues() {
        int limit = 10;  // Définissez la limite souhaitée ici
        ArrayList<Double> recentGasValues = new DatabaseHelperGas(this).getRecentGasValues(limit);

        // Ajouter les valeurs récentes au graphique
        for (Double gasValue : recentGasValues) {
            updateChart(gasValue);
        }
        // Utilisez les valeurs récupérées comme nécessaire
        // Par exemple, vous pourriez les afficher dans un TextView ou les ajouter à un graphique.
    }




}
