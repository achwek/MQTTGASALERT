package com.example.mqttgasalert;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseHelperGas extends SQLiteOpenHelper {
    // Database information
    private static final String DATABASE_NAME = "myappdatabase";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "subscribers";
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
                COLUMN_GAS_TIMESTAMP + " INTEGER)";
        db.execSQL(createTableQuery);
    }


    // Handle database upgrades if needed
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // You can implement upgrade logic here if necessary
    }

    // Insert a new gas value into the database
    public long insertGasValue(double gasValue) {
        long result = -1; // Initialize result to indicate failure
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_GAS_VALUE, gasValue);
            result = db.insert(TABLE_NAME, null, values); // Utilisez TABLE_NAME ici
        } catch (Exception e) {
            Log.e("DatabaseHelperGas", "Error inserting gas value: " + e.getMessage());
        }
        return result;
    }


    // Retrieve the recent gas values from the database
    public ArrayList<Double> getRecentGasValues(int limit) {
        ArrayList<Double> gasValues = new ArrayList<>();
        String query = "SELECT " + COLUMN_GAS_VALUE + " FROM " + TABLE_NAME +
                " ORDER BY " + COLUMN_GAS_TIMESTAMP + " DESC LIMIT " + limit;

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    double gasValue = cursor.getDouble(cursor.getColumnIndex(COLUMN_GAS_VALUE));
                    gasValues.add(gasValue);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelperGas", "Error querying gas values: " + e.getMessage());
        }

        return gasValues;
    }


}
