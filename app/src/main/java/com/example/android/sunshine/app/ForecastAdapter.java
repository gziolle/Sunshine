package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by gziolle on 9/22/2016.
 */
public class ForecastAdapter extends CursorAdapter {

    private static Context mContext;
    private static final int TODAY_VIEW_ITEM = 0;
    private static final int FUTURE_VIEW_LIST_ITEM = 1;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;

        switch (viewType) {
            case TODAY_VIEW_ITEM:
                layoutId = R.layout.list_item_forecast_today;
                break;
            case FUTURE_VIEW_LIST_ITEM:
                layoutId = R.layout.list_item_forecast;
                break;
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder holder = (ViewHolder) view.getTag();

        holder.icon.setImageResource(R.drawable.ic_launcher);

        long dateInMilli = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);

        holder.dateTextView.setText(Utility.getFriendlyDayString(mContext, dateInMilli));

        holder.forecastTextView.setText(cursor.getString(ForecastFragment.COL_WEATHER_DESC));

        boolean isMetric = Utility.isMetric(mContext);

        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        holder.highTextView.setText(Utility.formatTemperature(mContext, high, isMetric));

        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        holder.lowTextView.setText(Utility.formatTemperature(mContext, low, isMetric));
    }

    @Override
    public int getViewTypeCount() {
        return 2; // Today or Future list items.
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? TODAY_VIEW_ITEM : FUTURE_VIEW_LIST_ITEM;
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
//        boolean isMetric = Utility.isMetric(mContext);
//        String highLowStr = Utility.formatTemperature(contehigh, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
//        return highLowStr;
        return ";";
    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {

        String highAndLow = formatHighLows(
                cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE)) +
                " - " + cursor.getString(ForecastFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

    public static class ViewHolder {
        ImageView icon;
        TextView dateTextView;
        TextView forecastTextView;
        TextView highTextView;
        TextView lowTextView;

        public ViewHolder(View view) {
            icon = (ImageView) view.findViewById(R.id.list_item_icon);
            dateTextView = (TextView) view.findViewById(R.id.list_item_date_textview);
            forecastTextView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTextView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTextView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }


    }
}
