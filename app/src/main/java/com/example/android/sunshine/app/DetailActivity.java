package com.example.android.sunshine.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

//Activity for details of a particular list item.
public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //Get the intent's extras.
        Intent intent = getIntent();
        Bundle bundle = new Bundle();


        //If the intent exists and has extras, put them into a bundle.
        if (intent != null && intent.hasExtra(ForecastFragment.DETAILS_EXTRAS)) {
            String message = intent.getStringExtra(ForecastFragment.DETAILS_EXTRAS);
            bundle.putString(ForecastFragment.DETAILS_EXTRAS, message);
        }

        if (savedInstanceState == null) {
            //Set the bundle as the Detail Fragment's arguments
            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }
}
