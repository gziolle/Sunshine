package com.example.android.sunshine.app.database;

import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Created by gziolle on 9/8/2016.
 */
public final class WeatherContract {

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }


    /*Inner class that defines the table contents for the location table*/
    public static final class LocationEntry implements BaseColumns {
        /*The location table's name*/
        public static final String TABLE_NAME = "location";
        /*The location settings provided by user settings*/
        public static final String COLUMN_LOCATION_SETTING = "location_setting";
        /*The location's city name*/
        public static final String COLUMN_CITY_NAME = "city_name";
        /*The latitude coordinate in degrees*/
        public static final String COLUMN_COORD_LAT = "coord_lat";
        /*The longitude coordinate in degrees*/
        public static final String COLUMN_COORD_LONG = "coord_long";
    }

    /*Inner class that defines the table contents of the weather table*/
    public static final class WeatherEntry implements BaseColumns {

        public static final String TABLE_NAME = "weather";

        //Column with the foreign key into the location table
        public static final String COLUMN_LOC_KEY = "location_id";
        //Date, stored in milliseconds since the epoch
        public static final String COLUMN_DATE = "date";
        // Weather id as returned by the API, to identify the icon to be used.
        public static final String COLUMN_WEATHER_ID = "weather_id";

        //Short and long description of the weather, as provided by the API.
        //e.g. "clear" vs "sky is clear"
        public static final String COLUMN_SHORT_DESC = "short_desc";

        //Min and max temperatures for the day (stored as floats)
        public static final String COLUMN_MIN_TEMP = "min_temp";
        public static final String COLUMN_MAX_TEMP = "max_temp";

        //Humidity for the day (stored as an float representing percentage)
        public static final String COLUMN_HUMIDITY = "humidity";

        //Pressure stored as a float representing percentage.
        public static final String COLUMN_PRESSURE = "pressure";
        //Wind speed is stored as a float representing wind speed in mph
        public static final String COLUMN_WIND_SPEED = "wind_speed";
        //Degrees are meteorological degrees (e.g. 0 is north and 100 is south). Stored as floats.
        public static final String COLUMN_DEGREES = "degrees";

    }
}
