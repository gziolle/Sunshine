package com.example.android.sunshine.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by gziolle on 9/8/2016.
 */
public class WeatherDBHelper extends SQLiteOpenHelper {

    //Represents the database version. If new changes are applied to the database or its tables
    //This number must be increased to trigger the onUpgrade method.
    public static final int DATABASE_VERSION = 2;

    //Database name that will be used in the file system.
    public static final String DATABASE_NAME = "weather.db";

    //Query to create the location table.
    private static final String SQL_CREATE_LOCATION_TABLE =
            "CREATE TABLE " + WeatherContract.LocationEntry.TABLE_NAME + " (" +
                    WeatherContract.LocationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " TEXT UNIQUE NOT NULL, " +
                    WeatherContract.LocationEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, " +
                    WeatherContract.LocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                    WeatherContract.LocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL" +
                    ");";


    //Query to create the weather table.
    private static final String SQL_CREATE_WEATHER_TABLE =
            "CREATE TABLE " + WeatherContract.WeatherEntry.TABLE_NAME + " (" +
                    WeatherContract.WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                    WeatherContract.WeatherEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, " +
                    WeatherContract.WeatherEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                    WeatherContract.WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL, " +
                    WeatherContract.WeatherEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL, " +

                    WeatherContract.WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL, " +
                    WeatherContract.WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL, " +

                    WeatherContract.WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL, " +
                    WeatherContract.WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL, " +
                    WeatherContract.WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, " +
                    WeatherContract.WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL, " +
                    //Added the Location ID column as a foreign key
                    "FOREIGN KEY (" + WeatherContract.WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES " + WeatherContract.LocationEntry.TABLE_NAME + "(" + WeatherContract.LocationEntry._ID + ")," +
                    //Added a constraint to have only one weather entry per date per location. In case a new entry for the same date and
                    // location is inserted into the table, it will replace the old entry.
                    " UNIQUE (" + WeatherContract.WeatherEntry.COLUMN_DATE + ", " + WeatherContract.WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";

    public WeatherDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Execute the SQL commands in order to create the Weather and Location tables.
        db.execSQL(SQL_CREATE_WEATHER_TABLE);
        db.execSQL(SQL_CREATE_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Since the tables are used as a cache from a web service instead of
        //user-generated data, we'll drop the tables and create them again.
        //If the tables are filled by user data, you should use the ALTER TABLE command instead.
        db.execSQL("DROP TABLE IF EXISTS " + WeatherContract.WeatherEntry.TABLE_NAME + ";");
        db.execSQL("DROP TABLE IF EXISTS " + WeatherContract.LocationEntry.TABLE_NAME + ";");
        onCreate(db);
    }
}
