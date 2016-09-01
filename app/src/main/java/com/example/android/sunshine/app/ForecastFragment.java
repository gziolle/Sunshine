package com.example.android.sunshine.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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

    private static final int MY_PERMISSIONS_REQUEST_INTERNET = 1;
    public ArrayAdapter<String> mForecastAdapter;
    public ArrayList<String> mForecastData;
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

        mForecastData = new ArrayList<>();
        mForecastData.add("String 1");
        mForecastData.add("String 2");
        mForecastData.add("String 3");
        mForecastData.add("String 4");
        mForecastData.add("String 5");
        mForecastData.add("String 6");
        mForecastData.add("String 7");

        mForecastAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textView, mForecastData);
        mListView = (ListView) rootView.findViewById(R.id.listView_forecast);
        mListView.setAdapter(mForecastAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String forecast = (String) parent.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("weather", forecast);
                startActivity(intent);
            }
        });

        return rootView;
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
                //TODO
                //Make a check for internet connection before starting the task.
                ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = manager.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected()) {
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.INTERNET)
                            == PackageManager.PERMISSION_GRANTED) {
                        new FetchWeatherTask().execute("94043,us");
                    } else {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.READ_CONTACTS},
                                MY_PERMISSIONS_REQUEST_INTERNET);
                    }
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
                    //Start the AsyncTask related to the weather fetching.
                    new FetchWeatherTask().execute("94043,us");
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

    class FetchWeatherTask extends AsyncTask<String, Void, String[]> {


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
                builder.authority("api.openweathermap.org");
                builder.appendPath("data").appendPath("2.5").appendPath("forecast").appendPath("daily");
                builder.appendQueryParameter("q", postalCode);
                builder.appendQueryParameter("units", "metric");
                builder.appendQueryParameter("appid", "b6fb1780ef33770bd276bea15e7ea414");
                builder.appendQueryParameter("mode", "json");
                builder.appendQueryParameter("cnt", "7");

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
                    forecastArray = parser.getWeatherDataFromJsonString(forecastJsonStr);
                } catch (JSONException e) {
                    Log.e(TAG, "Error on JSON parsing:", e);
                }

//                Log.v(TAG, forecastJsonStr);

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
            mForecastAdapter.clear();
            mForecastData.addAll(Arrays.asList(strings));
        }
    }
}
