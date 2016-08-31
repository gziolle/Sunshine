package com.example.android.sunshine.app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Guilherme on 30/08/2016.
 */
public class ForecastFragment extends Fragment {
    public static final String TAG = "ForecastFragment";

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

        ArrayList<String> forecastData = new ArrayList<>();
        forecastData.add("String 1");
        forecastData.add("String 2");
        forecastData.add("String 3");
        forecastData.add("String 4");
        forecastData.add("String 5");
        forecastData.add("String 6");
        forecastData.add("String 7");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textView, forecastData);
        ListView listView = (ListView) rootView.findViewById(R.id.listView_forecast);
        listView.setAdapter(adapter);

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
                new FetchWeatherTask().execute();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    class FetchWeatherTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection conn = null;
            String forecastJsonStr = null;
            InputStream is = null;
            BufferedReader reader = null;

            try {
                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043,us&units=metric&appid=b6fb1780ef33770bd276bea15e7ea414&mode=json&cnt=7");
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

            } catch (Exception e) {
                Log.e(TAG, "Error: ", e);

                return null;

            } finally {
                if (conn != null) {
                    conn.disconnect();
                }

                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing the stream: ", e);
                    }
                }
            }
            return null;
        }
    }
}
