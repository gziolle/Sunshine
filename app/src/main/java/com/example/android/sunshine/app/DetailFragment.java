package com.example.android.sunshine.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

//Fragment that deals with the "Details" activity.
public class DetailFragment extends Fragment {

    private static final String TAG = DetailFragment.class.getSimpleName();
    private static final String HASHTAG = "#SunshineApp";

    private static String mForecastData;

    private ShareActionProvider mShareActionProvider;


    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Get the message sent by the activity in order to display it inside the fragment's view.
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            TextView weatherTextView = (TextView) getActivity().findViewById(R.id.weather);
            mForecastData = bundle.getString("weather");
            if (mForecastData != null) {
                weatherTextView.setText(mForecastData);
            }

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detail_fragment, menu);
        //Get the share menu item
        MenuItem item = menu.findItem(R.id.action_share);
        //Set the corresponding Share Action Provider
        //mShareActionProvider =  (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        mShareActionProvider = new ShareActionProvider(getActivity());

        if (mShareActionProvider != null) {
            Log.d(TAG, "mShareActionProvider != null");
            mShareActionProvider.setShareIntent(createShareIntent());
        } else {
            Log.d(TAG, "mShareActionProvider == null");
        }
        MenuItemCompat.setActionProvider(item, mShareActionProvider);
    }

    private Intent createShareIntent() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        //This flag clears the called app from the activity stack, so users arrive in the expected place next time this application is restarted.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, mForecastData + " " + HASHTAG);

        return intent;
    }
}
