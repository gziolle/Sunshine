package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.database.WeatherContract;

//Fragment that deals with the "Details" activity.
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DETAIL_LOADER = 0;
    private static final String TAG = DetailFragment.class.getSimpleName();
    private static final String HASHTAG = "#SunshineApp";
    static final String DETAIL_URI = "URI";
    static final String DETAIL_FRAGMENT_TAG = "DFTAG";

    private static String mForecastData;
    private static ViewHolder mViewHolder;
    private Uri mUri;

    private ShareActionProvider mShareActionProvider;

    private static final String[] DETAILS_COLUMNS = {
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
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WEATHER_HUMIDITY = 5;
    static final int COL_WEATHER_WIND_SPEED = 6;
    static final int COL_WEATHER_PRESSURE = 7;
    static final int COL_DEGREES = 8;
    static final int COL_WEATHER_CONDITION_ID = 9;

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

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DETAIL_URI);
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        mViewHolder = new ViewHolder(view);
        view.setTag(mViewHolder);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getActivity().getIntent();
        //In case of tablet UI, intent.getData() may be null.

        if (null != mUri) {
            return new CursorLoader(getActivity(),
                    mUri,
                    DETAILS_COLUMNS,
                    null,
                    null,
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst())
            return;

        boolean isMetric = Utility.isMetric(getActivity());

        int weatherConditionId = data.getInt(COL_WEATHER_CONDITION_ID);

        mViewHolder.icon.setImageResource(Utility.getWeatherConditionImage(weatherConditionId, false, getActivity()));

        long dateInMilli = data.getLong(COL_WEATHER_DATE);

        mViewHolder.dateTextView.setText(Utility.getFriendlyDayString(getActivity(), dateInMilli));

        mViewHolder.forecastTextView.setText(data.getString(COL_WEATHER_DESC));

        double high = data.getDouble(COL_WEATHER_MAX_TEMP);
        mViewHolder.highTextView.setText(Utility.formatTemperature(getActivity(), high, isMetric));

        double low = data.getDouble(COL_WEATHER_MIN_TEMP);
        mViewHolder.lowTextView.setText(Utility.formatTemperature(getActivity(), low, isMetric));

        double humidity = data.getDouble(COL_WEATHER_HUMIDITY);
        mViewHolder.humidityTextView.setText(getActivity().getString(R.string.format_humidity, humidity));

        double pressure = data.getDouble(COL_WEATHER_PRESSURE);
        mViewHolder.pressureTextView.setText(getActivity().getString(R.string.format_pressure, pressure));

        float windSpeed = data.getFloat(COL_WEATHER_WIND_SPEED);
        float degrees = data.getFloat(COL_DEGREES);
        mViewHolder.windTextView.setText(Utility.getFormattedWind(getActivity(), windSpeed, degrees));

        updateShareInfo(data);
    }

    private void updateShareInfo(Cursor data) {
        String dateStr = Utility.formatDate(data.getLong(COL_WEATHER_DATE));

        String description = data.getString(COL_WEATHER_DESC);

        boolean isMetric = Utility.isMetric(getActivity());

        String high = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        String low = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);

        mForecastData = String.format("%s - %s - %s/%s", dateStr, description, high, low);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    void onLocationChanged(String newLocation) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }


    public static class ViewHolder {
        ImageView icon;
        TextView dateTextView;
        TextView forecastTextView;
        TextView highTextView;
        TextView lowTextView;
        TextView humidityTextView;
        TextView windTextView;
        TextView pressureTextView;

        public ViewHolder(View view) {
            icon = (ImageView) view.findViewById(R.id.list_item_icon);
            dateTextView = (TextView) view.findViewById(R.id.list_item_date_textview);
            forecastTextView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTextView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTextView = (TextView) view.findViewById(R.id.list_item_low_textview);
            humidityTextView = (TextView) view.findViewById(R.id.detail_humidity_textview);
            windTextView = (TextView) view.findViewById(R.id.detail_wind_textview);
            pressureTextView = (TextView) view.findViewById(R.id.detail_pressure_textview);
        }
    }
}
