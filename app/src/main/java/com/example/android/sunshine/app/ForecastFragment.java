package com.example.android.sunshine.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

/**
 * Created by Guilherme on 30/08/2016.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ForecastFragment.class.getSimpleName();

    private static final String LIST_POSITION = "LIST_POSITION";
    private static final int FORECAST_LOADER = 0;
    private static final int MY_PERMISSIONS_REQUEST_INTERNET = 1;
    public ForecastAdapter mForecastAdapter;
    public ListView mListView;
    private int mSelectedPosition = -1;
    public static CursorLoader mCursorLoader;


    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;


    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d("Ziolle", "onCreateView: " + mSelectedPosition);
        savedInstanceState.putInt(LIST_POSITION, mSelectedPosition);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_forecast, container, false);

        if (savedInstanceState != null) {
            mSelectedPosition = savedInstanceState.getInt(LIST_POSITION);
            Log.d("Ziolle", "onCreateView: " + mSelectedPosition);
        }

        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        mListView = (ListView) rootView.findViewById(R.id.listView_forecast);
        mListView.setAdapter(mForecastAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                mSelectedPosition = position;
                if (cursor != null) {
                    String locationString = Utility.getPreferredLocation(getActivity());
                    ((Callback) getActivity()).onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationString, cursor.getLong(COL_WEATHER_DATE)));
                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // Create a Loader to query the data from the database asynchronously.
        // If the loader already exists, it will be used and the onLoadFinished method
        // will be called.
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);

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
            /*//Read the user's preference value.
            String locationPreference = Utility.getPreferredLocation(getActivity());
            //Start the AsyncTask related to the weather fetching.

            Intent intent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
            Intent sendIntent = new Intent(getActivity(), SunshineService.class);
            intent.putExtra(LOCATION_QUERY_EXTRA, locationPreference);
            sendIntent.putExtra(LOCATION_QUERY_EXTRA, locationPreference);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(),0,intent,PendingIntent.FLAG_ONE_SHOT);
            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            //Set the Alarm Manager to wake the system
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pendingIntent);

            //getActivity().startService(sendIntent);
            //new FetchWeatherTask(getActivity()).execute(locationPreference);
            Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationPreference, System.currentTimeMillis());
            mCursorLoader.setUri(weatherForLocationUri);*/
            SunshineSyncAdapter.syncImmediately(getActivity());
        } else {
            Log.e(TAG, "Can't connect to the internet");
        }
    }

    //Method that creates a new CursorLoader in required.
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d("Ziolle", "onCreateLoader");
        String locationString = Utility.getPreferredLocation(getActivity());
        //Sort order: ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationString, System.currentTimeMillis());
        mCursorLoader = new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
        return mCursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d("Ziolle", "onLoadFinished");
        mForecastAdapter.swapCursor(data);
        if (mSelectedPosition != -1) {
            mListView.smoothScrollToPosition(mSelectedPosition);
        } else {
            MainActivity activity = (MainActivity) getActivity();
            if (activity.getTwoPaneMode()) {
                mListView.clearFocus();
                mListView.post(new Runnable() {
                    @Override
                    public void run() {
                        mListView.setSelection(0);
                    }
                });
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d("Ziolle", "onLoaderReset");
        mForecastAdapter.swapCursor(null);
    }

    public void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);

    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Uri dateUri);
    }
}