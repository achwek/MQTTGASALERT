package com.example.mqttgasalert;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.os.Bundle;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class ListSubscriberActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ListAdapterSubscribers adapter;

    // Déclarez l'interface en dehors des méthodes
    private ListAdapterSubscribers.OnDeleteClickListener onDeleteClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_subscriber);

        // Étape 1 : Instancier DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Étape 2 : Récupérer les données des abonnés depuis la base de données
        ArrayList<Subscriber> subscribers = dbHelper.getAllSubscribers();

        // Étape 3 : Créer une instance de ListAdapterSubscribers et la définir comme adaptateur pour RecyclerView
        onDeleteClickListener = new ListAdapterSubscribers.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(int position) {
                // Assurez-vous que la position est valide avant d'accéder à la liste
                if (position >= 0 && position < subscribers.size()) {
                    // Supprimez l'élément de la base de données
                    String subscriberId = subscribers.get(position).getSubscriberId();
                    dbHelper.deleteSubscriber(subscriberId);

                    // Supprimez l'élément de la liste
                    subscribers.remove(position);
                    adapter.notifyItemRemoved(position);
                } else {
                    // Journalisez une erreur ou affichez un message indiquant la position invalide
                    Log.e("ListSubscriberActivity", "Position invalide : " + position);
                }
            }
        };

        adapter = new ListAdapterSubscribers(this, subscribers, onDeleteClickListener);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewSubscribers);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Barre de navigation inférieure
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.bottom_listSubscribers);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home) {
                if (!getClass().equals(MainActivity.class)) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    return true;
                }
            } else if (itemId == R.id.bottom_listSubscribers) {
                // Déjà sur l'écran des abonnés
                return true;
            } else if (itemId == R.id.bottom_settings) {
                // Vérifiez si vous êtes déjà sur SettingsActivity avant de démarrer une nouvelle activité
                if (!getClass().equals(SettingsActivity.class)) {
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                }
                return true;
            }

            return false;
        });
    }
}
