package com.example.mqttgasalert;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelperGas extends SQLiteOpenHelper {
    // Database information
    private static final String DATABASE_NAME = "MQTTDBgas";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "gas";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_GAS_VALUE = "gas_value";
    private static final String COLUMN_GAS_TIMESTAMP = "gas_timestamp";

    public DatabaseHelperGas(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_GAS_VALUE + " REAL, " +
                COLUMN_GAS_TIMESTAMP + " TEXT)"; // Add gas_timestamp column
        db.execSQL(createTableQuery);
    }



    // Handle database upgrades if needed
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // You can implement upgrade logic here if necessary
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    // Insert a new gas value into the database
    public long insertGasValue(Gas gas) {
        long result = -1; // Initialize result to indicate failure
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_GAS_VALUE, gas.getValue());
            values.put(COLUMN_GAS_TIMESTAMP, gas.getDateTime());
            result = db.insert(TABLE_NAME, null, values);
            Log.d("DatabaseHelper", "Inserted values gas success " + result);
            db.close();

        } catch (Exception e) {
            Log.e("DatabaseHelperGas", "Error inserting gas value: " + e.getMessage());
        }
        return result;
    }


    // Supprimer la valeur la plus ancienne de la base de données
    public void deleteOldestGasValue(Gas gas) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            // Supprimer la ligne la plus ancienne basée sur la date et l'heure
            db.delete(TABLE_NAME, COLUMN_GAS_TIMESTAMP + " = ?", new String[]{gas.getDateTime()});
        } catch (Exception e) {
            Log.e("DatabaseHelperGas", "Erreur lors de la suppression de la valeur la plus ancienne : " + e.getMessage());
        }
    }
    // Update an existing gas value in the database
    public int updateGasValue(Gas gas) {
        int result = -1; // Initialize result to indicate failure

        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_GAS_VALUE, gas.getValue());
            values.put(COLUMN_GAS_TIMESTAMP, gas.getDateTime());

            // Update the row based on the gas timestamp
            result = db.update(TABLE_NAME, values, COLUMN_GAS_TIMESTAMP + " = ?", new String[]{gas.getDateTime()});
        } catch (Exception e) {
            Log.e("DatabaseHelperGas", "Error updating gas value: " + e.getMessage());
        }

        return result;
    }

    // Method to retrieve the recent 10 gas values from the database
    // Method to retrieve the recent 10 gas values from the database
    public List<Gas> getRecentGasValues() {
        List<Gas> gasValues = new ArrayList<>();

        try (SQLiteDatabase db = this.getReadableDatabase()) {

            // Query to retrieve the last 10 gas values, ordered by timestamp in descending order
            String query = "SELECT * FROM " + TABLE_NAME +
                    " ORDER BY " + COLUMN_GAS_TIMESTAMP + " DESC LIMIT 10";

            try (Cursor cursor = db.rawQuery(query, null)) {
                if (cursor.moveToFirst()) {
                    do {
                        Gas gas = new Gas();
                        gas.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                        gas.setValue(String.valueOf(cursor.getDouble(cursor.getColumnIndex(COLUMN_GAS_VALUE))));
                        gas.setDateTime(cursor.getString(cursor.getColumnIndex(COLUMN_GAS_TIMESTAMP)));
                        gasValues.add(gas);
                    } while (cursor.moveToNext());
                }
            }
        }

        return gasValues;
    }

}