package com.example.android.sunshine.app;

/**
 * Created by gziolle on 8/31/2016.
 */

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class WeatherDataParser {

    private static final String TAG = WeatherDataParser.class.getSimpleName();

    private String getReadableDateString(int days) {

        //Create a new Gregorian Calendar set to today
        GregorianCalendar calendar = new GregorianCalendar();
        //Add n days to today
        calendar.add(Calendar.DATE, days);

        Date time = calendar.getTime();
        SimpleDateFormat date = new SimpleDateFormat("EEE MMM dd");
        //Format the date
        return date.format(time);
    }

    private String formatHighLows(int high, int low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    public String[] getWeatherDataFromJsonString(String forecast) throws JSONException {

        String OWM_LIST = "list";
        String OWM_WEATHER = "weather";
        String OWM_TEMP = "temp";
        String OWM_MIN = "min";
        String OWM_MAX = "max";
        String OWM_MAIN = "main";

        String[] dayWeatherArray;

        JSONObject jo = new JSONObject(forecast);
        JSONArray forecastArray = jo.getJSONArray(OWM_LIST);

        dayWeatherArray = new String[forecastArray.length()];

        for (int k = 0; k < dayWeatherArray.length; k++) {
            JSONObject dateObject = forecastArray.getJSONObject(k);

            String day = getReadableDateString(k);

            // description is in a child array called "weather", which is 1 element long.
            String description = dateObject.getJSONArray(OWM_WEATHER).getJSONObject(0).getString(OWM_MAIN);

            //Get the array of temperatures, in order to get high and low temperatures
            JSONObject temperatures = dateObject.getJSONObject(OWM_TEMP);
            int highTemp = temperatures.getInt(OWM_MAX);
            int lowTemp = temperatures.getInt(OWM_MIN);

            //Format the temperatures string
            String highAndLow = formatHighLows(highTemp, lowTemp);
            dayWeatherArray[k] = day + " - " + description + " - " + highAndLow;
        }
        return dayWeatherArray;
    }
}
