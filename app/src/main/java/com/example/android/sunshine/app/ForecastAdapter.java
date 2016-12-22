package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * Created by gziolle on 9/22/2016.
 */
public class ForecastAdapter extends CursorAdapter {

    private static final int TODAY_VIEW_ITEM = 0;
    private static final int FUTURE_VIEW_LIST_ITEM = 1;
    private boolean mUseTodayLayout;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
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

        int weatherConditionId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);

        int position = cursor.getPosition();

        int fallbackIconId;

        if (position == TODAY_VIEW_ITEM && mUseTodayLayout) {
            fallbackIconId = Utility.getWeatherConditionImage(weatherConditionId, false, context);
        } else {
            fallbackIconId = Utility.getWeatherConditionImage(weatherConditionId, true, context);
        }

        Glide.with(context)
                .load(Utility.getArtUrlForWeatherCondition(context, weatherConditionId))
                .error(fallbackIconId)
                .crossFade()
                .into(holder.icon);

        long dateInMilli = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);

        holder.dateTextView.setText(Utility.getFriendlyDayString(context, dateInMilli));

        String description = Utility.getWeatherDescription(context, cursor.getString(ForecastFragment.COL_WEATHER_DESC));
        holder.forecastTextView.setText(description);

        boolean isMetric = Utility.isMetric(context);

        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        holder.highTextView.setText(Utility.formatTemperature(context, high, isMetric));

        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        holder.lowTextView.setText(Utility.formatTemperature(context, low, isMetric));
    }

    @Override
    public int getViewTypeCount() {
        return 2; // Today or Future list items.
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? TODAY_VIEW_ITEM : FUTURE_VIEW_LIST_ITEM;
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
