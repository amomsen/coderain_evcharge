package com.example.nfcjavatest2;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "EVCDatabase.db";
    private SQLiteDatabase database;
    // Credentials
    public static final String CACHE_TABLE = "cache";
    public static final String ID = "id";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String ENERGY_BALANCE = "energy_balance";
    public static final String MINIMUM_ENERGY = "minimum_energy";
    public static final String IS_CHARGING = "is_charging";


    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
        database = this.getWritableDatabase();
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table IF NOT EXISTS " + CACHE_TABLE +
                "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USERNAME + " TEXT, " +
                PASSWORD + " TEXT," +
                ENERGY_BALANCE + " NUMERIC," +
                MINIMUM_ENERGY + " NUMERIC," +
                IS_CHARGING + " NUMERIC)"); // 0 = false; 1 = true;
    }

    public void CreateNewCacheTable() {
        onCreate(database);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + CACHE_TABLE);
        onCreate(db);
    }

    public void saveCredentials(String username, String password) {
        try {
            CacheData data = readData();
            if (data == null) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(USERNAME, username);
                contentValues.put(PASSWORD, password);
                database.insert(DatabaseHelper.CACHE_TABLE, null, contentValues);
            } else {
                String where = "rowid=(SELECT MIN(" + ID + ") FROM " + CACHE_TABLE + ")";
                ContentValues values = new ContentValues();
                values.put(USERNAME, username);
                values.put(PASSWORD, password);
                database.update(CACHE_TABLE, values, where, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CacheData readData() {
        String sql = "SELECT " + USERNAME + ", " + PASSWORD + ", " + ENERGY_BALANCE + ", " + MINIMUM_ENERGY + ", " + IS_CHARGING + " FROM " + CACHE_TABLE + " ORDER BY ID;";
        Cursor cursor = database.rawQuery(sql, new String[]{});
        if (cursor.getCount() == 0) {
            return null;
        }
        CacheData data = new CacheData();
        if (cursor.moveToFirst()) {
            data.Username = cursor.getString(0);
            data.Password = cursor.getString(1);
            data.EnergyBalance = cursor.getString(2);
            data.MinimumEnergy = cursor.getString(3);
            if (cursor.getString(4) != null) {
                data.IsCharging = cursor.getString(4).equals("1");
            } else {
                data.IsCharging = false;
            }
        }
        cursor.close();
        return data;
    }

    public void saveEnergy(double energyLeft) {
        String where = "rowid=(SELECT MIN(" + ID + ") FROM " + CACHE_TABLE + ")";
        ContentValues values = new ContentValues();
        values.put(ENERGY_BALANCE, energyLeft);
        database.update(CACHE_TABLE, values, where, null);
    }

    public void ClearDatabase() {
        database.delete(CACHE_TABLE, "1 == 1", null);
    }

    public void SetChargingState(boolean isCharging) {
        String where = "rowid=(SELECT MIN(" + ID + ") FROM " + CACHE_TABLE + ")";
        ContentValues values = new ContentValues();
        if (isCharging) {
            values.put(IS_CHARGING, "1");
        } else {
            values.put(IS_CHARGING, "0");
        }
        database.update(CACHE_TABLE, values, where, null);
        CacheData data = readData();

    }

    public void DeleteCacheTable() {
        database.execSQL("DROP TABLE IF EXISTS " + CACHE_TABLE);
    }

    public void saveMinimumEnergy(Double minimumEnergy) {
        String where = "rowid=(SELECT MIN(" + ID + ") FROM " + CACHE_TABLE + ")";
        ContentValues values = new ContentValues();
        values.put(MINIMUM_ENERGY, minimumEnergy);
        database.update(CACHE_TABLE, values, where, null);
    }
}
