package com.example.mqttgasalert;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database information
    private static final String DATABASE_NAME = "MQTTDB";
    private static final int DATABASE_VERSION = 1;

    // Table information
    private static final String TABLE_NAME = "subscribers";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_Connection_Name = "connectionName";
    private static final String COLUMN_ADRESS_B = "adressB";
    private static final String COLUMN_PORT = "port";
    private static final String COLUMN_TOPIC = "topic";
    private static final String COLUMN_SEUIL = "seuil";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create the database table
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_Connection_Name + " TEXT, " +
                COLUMN_ADRESS_B + " TEXT, " +
                COLUMN_PORT + " INTEGER, " +
                COLUMN_TOPIC + " TEXT, " +
                COLUMN_SEUIL + " INTEGER)";
        db.execSQL(createTableQuery);
    }

    // Handle database upgrades if needed
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // You can implement upgrade logic here if necessary
    }

    // Insert a new configuration into the database
// Insert a new configuration into the database
    // Insert a new subscriber into the database
    // Insert a new subscriber into the database
    public long insertSubscriber(String connectionName, String adressB, int port, String topic, String seuil) {
        long result = -1; // Initialize result to indicate failure
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            // Remove the following line since COLUMN_ID is auto-incremented
            // values.put(COLUMN_ID, subscriberId);
            values.put(COLUMN_Connection_Name, connectionName);
            values.put(COLUMN_ADRESS_B, adressB);
            values.put(COLUMN_PORT, port);
            values.put(COLUMN_TOPIC, topic);
            values.put(COLUMN_SEUIL, seuil);

            result = db.insert(TABLE_NAME, null, values);

            Log.d("DatabaseHelper", "Inserted subscriber with ID: " + result);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error inserting subscriber: " + e.getMessage(), e);
        } finally {
            db.close();
        }

        return result;
    }


    // Retrieve all configurations saved in the database
    public ArrayList<Subscriber> getAllSubscribers() {
        ArrayList<Subscriber> subscriberList = new ArrayList<>();
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null)) {

            if (cursor.moveToFirst()) {
                do {
                    int id= cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                    String connectionName = cursor.getString(cursor.getColumnIndex(COLUMN_Connection_Name));
                    String adresseBroker = cursor.getString(cursor.getColumnIndex(COLUMN_ADRESS_B));
                    int port = cursor.getInt(cursor.getColumnIndex(COLUMN_PORT));
                    String topic = cursor.getString(cursor.getColumnIndex(COLUMN_TOPIC));
                    int seuil = cursor.getInt(cursor.getColumnIndex(COLUMN_SEUIL));

                    // Create a Subscriber object and add it to the list
                    Subscriber subscriber = new Subscriber(id,connectionName, adresseBroker, port, topic, seuil);
                    subscriberList.add(subscriber);
                } while (cursor.moveToNext());
            }
        }

        return subscriberList;
    }

    // Delete a configuration based on the connectionName
   /* public void deleteSubscriber(int subscriberId) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(subscriberId)});
            Log.e("ListSubscriberActivity", " : " + COLUMN_ID);

        }
    }*/
    public void deleteSubscriber(int subscriberId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int deletedRows = db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(subscriberId)});
            Log.d("DatabaseHelper", "Number of rows deleted: " + deletedRows);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting subscriber: " + e.getMessage());
        } finally {
            db.close();
        }
    }




}
