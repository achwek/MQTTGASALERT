package com.example.mqttgasalert;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private ListView subscribersListView;

    // Add your EditText fields
    private EditText nameConnectionEditText;
    private EditText adresseBrokerEditText;
    private EditText portEditText;
    private EditText topicEditText;
    private EditText seuilEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize your EditText fields
        nameConnectionEditText = findViewById(R.id.nameConnection);
        adresseBrokerEditText = findViewById(R.id.adresseBroker);
        portEditText = findViewById(R.id.port);
        topicEditText = findViewById(R.id.topic);
        seuilEditText = findViewById(R.id.seuil);
        saveButton = findViewById(R.id.saveButton);

        // Initialize the dbHelper
        dbHelper = new DatabaseHelper(this);

        // Ajouter subscriber à SQLite
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Vérification si tous les champs sont remplis
                if (validateInputs()) {
                    // Récupérer les valeurs des champs
                    String connectionName = nameConnectionEditText.getText().toString().trim();
                    String adresseB = adresseBrokerEditText.getText().toString().trim();
                    String portStr = portEditText.getText().toString().trim();
                    String topic = topicEditText.getText().toString().trim();
                    String seuil = seuilEditText.getText().toString().trim();

                    // Convertir la valeur de port en entier
                    int portValue = 0; // Valeur par défaut ou une valeur appropriée selon le contexte
                    if (!portStr.isEmpty()) {
                        try {
                            portValue = Integer.parseInt(portStr);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            // Gérer l'erreur de conversion ici si nécessaire
                        }
                    }

                    // Ajouter le subscriber à la base de données
                    long id = dbHelper.insertSubscriber(connectionName, adresseB, portValue, topic, seuil);

                    if (id != -1) {
                        // Succès : Afficher un message ou effectuer d'autres actions si nécessaire
                        Toast.makeText(MainActivity.this, "Subscriber ajouté avec succès", Toast.LENGTH_SHORT).show();

                        // Réinitialiser les champs après l'ajout
                        nameConnectionEditText.setText("");
                        adresseBrokerEditText.setText("");
                        portEditText.setText("");
                        topicEditText.setText("");
                        seuilEditText.setText("");
                    } else {
                        // Échec : Afficher un message d'erreur si l'ajout a échoué
                        Toast.makeText(MainActivity.this, "Erreur lors de l'ajout du subscriber", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Afficher un message d'erreur si les champs ne sont pas remplis
                    Toast.makeText(MainActivity.this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Fin ajouter à la base de données

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home) {
                // Handle bottom_home case
                return true;
            } else if (itemId == R.id.bottom_listSubscribers) {
                startActivity(new Intent(getApplicationContext(), ListSubscriberActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_settings) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });
    }

    // Fonction pour valider les champs
    private boolean validateInputs() {
        String adresseB = adresseBrokerEditText.getText().toString().trim();
        String port = portEditText.getText().toString().trim();
        String topic = topicEditText.getText().toString().trim();
        String seuil = seuilEditText.getText().toString().trim();

        // Vérifier si tous les champs sont remplis
        return !adresseB.isEmpty() && !port.isEmpty() && !topic.isEmpty() && !seuil.isEmpty();
    }
}
