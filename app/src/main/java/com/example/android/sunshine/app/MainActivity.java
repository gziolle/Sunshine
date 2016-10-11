package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.sunshine.app.sync.SunshineSyncAdapter;


public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback {


    public static final String TAG = "MainActivity";
    private static final String FORECASTFRAGMENT_TAG = "forecastFragment";
    public static String mLocation;
    private static boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocation = Utility.getPreferredLocation(this);

        if (findViewById(R.id.weather_detail_container) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.weather_detail_container, new DetailFragment()).commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        ForecastFragment ff = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
        if (ff != null) {
            ff.mForecastAdapter.setUseTodayLayout(!mTwoPane);
        }

        SunshineSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        String location = Utility.getPreferredLocation(this);
        if (!mLocation.equals(location)) {
            ForecastFragment ff = (ForecastFragment) getSupportFragmentManager().findFragmentByTag(FORECASTFRAGMENT_TAG);
            if (ff != null)
                ff.onLocationChanged();

            DetailFragment df = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DetailFragment.DETAIL_FRAGMENT_TAG);
            if (df != null)
                df.onLocationChanged(location);
            mLocation = location;
        }
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //Create an Intent to start the Settings Activity.
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_map) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            //Get the user's preferred location through Shared Preferences.
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String userLocation = preferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default_value));

            //Parse the user's location as a URI in order to send it to the Maps app.
            //https://developer.android.com/guide/components/intents-common.html#Maps
            Uri uri = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q", userLocation).build();
            intent.setData(uri);

            //Error handling, in case the device does not have a Map app.
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri dateUri) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, dateUri);

            DetailFragment df = new DetailFragment();
            df.setArguments(args);

            getSupportFragmentManager().beginTransaction().replace(R.id.weather_detail_container, df, DetailFragment.DETAIL_FRAGMENT_TAG).commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.setData(dateUri);
            startActivity(intent);
        }
    }

    public boolean getTwoPaneMode() {
        return mTwoPane;
    }
}
