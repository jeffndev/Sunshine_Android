package com.example.jnewel200.sunshine.app;

/**
 * Created by jnewel200 on 3/17/2015.
 */
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
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
    private boolean mUseTodayLayout = true;
    /**
     * Prepare the weather high/lows for presentation.
     */
    public void setUseTodayLayout(boolean useTodayLayout){
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position) {
        //but if this is twopane, switch it back to R.layout.list_item_forecast layout...
        //how to know if the main activity is in two pane?
        return (position == 0 && mUseTodayLayout ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
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


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if(cursor!= null) {
            ViewHolder viewHolder = (ViewHolder)view.getTag();

            boolean isMetric = Utility.isMetric(mContext);
            int weatherIconId = cursor.getInt(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
            int assetIconId = (getItemViewType(cursor.getPosition()) == VIEW_TYPE_TODAY ?
                              Utility.getArtResourceForWeatherCondition(weatherIconId) :
                              Utility.getIconResourceForWeatherCondition(weatherIconId));
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
            viewHolder.iconView.setImageResource(assetIconId);
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