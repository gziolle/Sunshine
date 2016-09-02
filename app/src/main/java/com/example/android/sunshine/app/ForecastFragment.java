package com.example.android.sunshine.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Guilherme on 30/08/2016.
 */
public class ForecastFragment extends Fragment {
    private static final String TAG = ForecastFragment.class.getSimpleName();

    private static final String PREF_LOCATION_DEFAULT_VALUE = "location";
    public static final String DETAILS_EXTRAS = "weather";

    private static final int MY_PERMISSIONS_REQUEST_INTERNET = 1;
    public ArrayAdapter<String> mForecastAdapter;
    public ListView mListView;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mForecastAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textView, new ArrayList<String>());
        mListView = (ListView) rootView.findViewById(R.id.listView_forecast);
        mListView.setAdapter(mForecastAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Create an intent to start the Detail Activity for a list item.
                String forecast = (String) parent.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(DETAILS_EXTRAS, forecast);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
            updateWeather();
        } else {
            //Ask the user for internet permission.
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_INTERNET);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.action_refresh:
                //Check whether it has the permission to use the internet connection.
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
                    updateWeather();
                } else {
                    //Ask the user for internet permission.
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.READ_CONTACTS},
                            MY_PERMISSIONS_REQUEST_INTERNET);
                }
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_INTERNET: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateWeather();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    // Temporary code - should be replaced.
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.READ_CONTACTS},
                            MY_PERMISSIONS_REQUEST_INTERNET);
                }
                return;
            }
        }
    }

    public void updateWeather() {
        //Make a check for internet connection before starting the task.
        ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        //Check if the internet permission is active.
        if (networkInfo != null && networkInfo.isConnected()) {
            //Get the user's shared preferences through the SharedPreferences file.
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            //Read the user's preference value.
            String preference = preferences.getString(getString(R.string.pref_location_key), PREF_LOCATION_DEFAULT_VALUE);
            //Start the AsyncTask related to the weather fetching.
            new FetchWeatherTask().execute(preference);
        } else {
            Log.e(TAG, "Can't connect to the internet");
        }
    }

    class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private String OWM_AUTHORITY = "api.openweathermap.org";
        private String OWM_LOCATION = "q";
        private String OWM_UNITS = "units";
        private String OWM_APP_ID = "appid";
        private String OWM_MODE = "mode";
        private String OWM_DAYS_COUNT = "cnt";

        private static final String TAG = "FetchWeatherTask";

        @Override
        protected String[] doInBackground(String... params) {
            HttpURLConnection conn = null;
            InputStream is;
            BufferedReader reader = null;
            String forecastJsonStr;
            String[] forecastArray = null;

            if (params[0] == null) {
                return null;
            }

            String postalCode = params[0];

            try {
                // Build a URL to access the OpenWeatherMap API with the following query parameters:
                //q=94043,us&units=metric&appid=b6fb1780ef33770bd276bea15e7ea414&mode=json&cnt=7
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http");
                builder.authority(OWM_AUTHORITY);
                builder.appendPath("data").appendPath("2.5").appendPath("forecast").appendPath("daily");
                builder.appendQueryParameter(OWM_LOCATION, postalCode);
                builder.appendQueryParameter(OWM_UNITS, getString(R.string.pref_temperature_units_default));
                builder.appendQueryParameter(OWM_APP_ID, "b6fb1780ef33770bd276bea15e7ea414");
                builder.appendQueryParameter(OWM_MODE, "json");
                builder.appendQueryParameter(OWM_DAYS_COUNT, "7");

                URL url = new URL(builder.build().toString());

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                is = conn.getInputStream();

                if (is == null) {
                    //Error handling
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(is));

                if (reader == null) {
                    //Error Handling
                    return null;
                }
                String line;
                StringBuffer buffer = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    //Only append the lines from the reader if they are not null, right to the end of the stream.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    //Error handling
                    return null;
                }
                forecastJsonStr = buffer.toString();
                try {
                    WeatherDataParser parser = new WeatherDataParser();
                    //Get the user's shared preferences
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    //Get the temperature unit
                    String temperatureUnits = preferences.getString(getString(R.string.pref_temperature_units_key), getString(R.string.pref_temperature_units_default));
                    //Parse the data based on the temperature unit
                    forecastArray = parser.getWeatherDataFromJsonString(forecastJsonStr, temperatureUnits);
                } catch (JSONException e) {
                    Log.e(TAG, "Error on JSON parsing:", e);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error: ", e);

                return null;

            } finally {
                if (conn != null) {
                    conn.disconnect();
                }

                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing the stream: ", e);
                    }
                }
            }
            return forecastArray;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            if (strings != null) {
                mForecastAdapter.clear();
                mForecastAdapter.addAll(Arrays.asList(strings));
            }
        }
    }
}