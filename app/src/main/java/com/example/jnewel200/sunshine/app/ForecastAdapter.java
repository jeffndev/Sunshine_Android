package com.example.jnewel200.sunshine.app;

/**
 * Created by jnewel200 on 3/17/2015.
 */
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jnewel200.sunshine.app.data.WeatherContract;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }
    private final String LOG_TAG = ForecastAdapter.class.getSimpleName();

    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY = 1;
    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(mContext, high, isMetric) + "/" + Utility.formatTemperature(mContext, low, isMetric);
        return highLowStr;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    /*
            This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
            string.
         */
    private String convertCursorRowToUXFormat(Cursor cursor) {
        // get row indices for our cursor
        int idx_max_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
        int idx_min_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
        int idx_date = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
        int idx_short_desc = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);

        String highAndLow = formatHighLows(
                cursor.getDouble(idx_max_temp),
                cursor.getDouble(idx_min_temp));

        return Utility.formatDate(cursor.getLong(idx_date)) +
                " - " + cursor.getString(idx_short_desc) +
                " - " + highAndLow;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = (viewType==VIEW_TYPE_TODAY ?
                            R.layout.list_item_forecast_today:
                            R.layout.list_item_forecast
        );
        View view =  LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.
        if(cursor!= null) {
            ViewHolder viewHolder = (ViewHolder)view.getTag();

            boolean isMetric = Utility.isMetric(mContext);
            int weatherIconId = cursor.getInt(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
            String hi = Utility.formatTemperature(mContext, cursor.getDouble(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP)), isMetric);
            String lo = Utility.formatTemperature(mContext, cursor.getDouble(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP)), isMetric);
            String forecast = cursor.getString(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC));
            String date = Utility.getFriendlyDayString(mContext,cursor.getLong(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE)));

            if(viewHolder==null) {
                viewHolder = new ViewHolder(view);
            }

            viewHolder.dateView.setText(date);
            viewHolder.hiView.setText(hi);
            viewHolder.loView.setText(lo);
            viewHolder.descriptionView.setText(forecast);
            viewHolder.iconView.setImageResource(R.drawable.ic_launcher);
        }
    }
    public static class ViewHolder{
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView hiView;
        public final TextView loView;

        public ViewHolder(View view){
            iconView = (ImageView)view.findViewById(R.id.list_item_icon);
            dateView = (TextView)view.findViewById(R.id.list_item_date_textview);
            hiView = (TextView)view.findViewById(R.id.list_item_high_textview);
            loView = (TextView)view.findViewById(R.id.list_item_low_textview);
            descriptionView = (TextView)view.findViewById(R.id.list_item_forecast_textview);
        }
    }
}