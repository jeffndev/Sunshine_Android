package com.example.jnewel200.sunshine.app;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jnewel200.sunshine.app.data.WeatherContract;

/**
 * Created by jnewel200 on 3/31/2015.
 */
public class DetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    public static final String WEATHER_DETAIL_DATA_TAG = "WeatherDetailUri";
    private String mForecast;
    private ViewHolder mViewHolder;
    private ShareActionProvider mShareActionProvider;
    private Uri mUri;

    public static final int DETAIL_LOADER_ID = 1;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    // these constants correspond to the projection defined above, and must change if the
    // projection changes
    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;
    private static final int COL_WEATHER_HUMIDITY = 5;
    private static final int COL_WEATHER_PRESSURE = 6;
    private static final int COL_WEATHER_WIND_SPEED = 7;
    private static final int COL_WEATHER_WIND_DEGREES = 8;
    private static final int COL_WEATHER_CONDITION_ID = 9;


    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    public void onLocationChanged(String newLocation){
        Uri uri = mUri;
        if(uri != null){
            Long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherByLocationAndDate(newLocation,date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER_ID,null,this);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(!data.moveToFirst()) return;

        View view = getView();
        //ViewHolder viewHolder = (ViewHolder)view.getTag();
        if(mViewHolder == null){
            mViewHolder = new ViewHolder(view);
            //view.setTag(viewHolder);
        }
        //Get the date
        long rawDate = data.getLong(COL_WEATHER_DATE);
        String dateString = Utility.formatDate(data.getLong(COL_WEATHER_DATE));
        String weatherDescription = data.getString(COL_WEATHER_DESC);
        boolean isMetric = Utility.isMetric(getActivity());
        double humidity = data.getDouble(COL_WEATHER_HUMIDITY);
        double wind = data.getDouble(COL_WEATHER_WIND_SPEED);
        double windDegrees = data.getDouble(COL_WEATHER_WIND_DEGREES);
        double pressure = data.getDouble(COL_WEATHER_PRESSURE);
        String high = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        String low = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
        int weatherIconId = data.getInt(COL_WEATHER_CONDITION_ID);

        mForecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);
        //set the data in the detail view items...
        mViewHolder.forecastView.setText(weatherDescription);
        mViewHolder.dayView.setText(Utility.getDayName(getActivity(),rawDate));
        mViewHolder.dateView.setText(Utility.getFormattedMonthDay(getActivity(),rawDate));
        mViewHolder.hiView.setText(high);
        mViewHolder.loView.setText(low);
        mViewHolder.humidityView.setText(Utility.getFormattedHumidityDisplay(getActivity(),humidity));
        mViewHolder.windView.setText( Utility.getFormattedWindSpeedDisplay(getActivity(), wind, windDegrees, isMetric));
        mViewHolder.pressureView.setText( Utility.getFormattedPressureDisplay(getActivity(), pressure));
        mViewHolder.iconView.setImageResource( Utility.getArtResourceForWeatherCondition(weatherIconId) );
        mViewHolder.iconView.setContentDescription( weatherDescription );

        if(mShareActionProvider!=null && mForecast != null){
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }


    public static class ViewHolder{
        public final ImageView iconView;
        public final TextView dayView;
        public final TextView dateView;
        public final TextView forecastView;
        public final TextView hiView;
        public final TextView loView;
        public final TextView humidityView;
        public final TextView windView;
        public final TextView pressureView;


        public ViewHolder(View view){
            iconView = (ImageView)view.findViewById(R.id.detail_icon);
            dateView = (TextView)view.findViewById(R.id.detail_date_textview);
            dayView = (TextView)view.findViewById(R.id.detail_day_textview);
            hiView = (TextView)view.findViewById(R.id.detail_high_textview);
            loView = (TextView)view.findViewById(R.id.detail_low_textview);
            forecastView = (TextView)view.findViewById(R.id.detail_forecast_textview);
            humidityView = (TextView)view.findViewById(R.id.detail_humidity_textview);
            windView = (TextView)view.findViewById(R.id.detail_wind_textview);
            pressureView = (TextView)view.findViewById(R.id.detail_pressure_textview);
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) { /*empty*/ }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if( mUri != null){
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    FORECAST_COLUMNS,
                    null,
                    null,
                    null
            );
        }else{
            return null;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        if(args != null){
            mUri = args.getParcelable(WEATHER_DETAIL_DATA_TAG);
        }

        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        mViewHolder = new ViewHolder(view);
        //view.setTag(viewHolder);
        return view;
    }
    private Intent createShareForecastIntent(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG );
        return shareIntent;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail_fragment, menu);

        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if(mForecast != null){
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }
}

