package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.net.URI;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
}
