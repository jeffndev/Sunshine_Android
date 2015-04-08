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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.jnewel200.sunshine.app.data.WeatherContract;


public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    //ArrayAdapter<String> mForecastAdapter;
    ForecastAdapter mForecastAdapter;
    int mLastListPos = 0;
    final String POSITION_STATE_KEY = "LAST_LIST_POSITION";

    private final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private static final int LOADER_ID = 0;
    private static final String[] FORECAST_COLUMNS = {
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
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;


    public interface Callback{
        void onItemSelected(Uri dataDetailUri);
    }

    public ForecastFragment() {
    }

    void onLocationChanged() {
        fetchWeatherData();
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    private void fetchWeatherData(){
        Log.v(LOG_TAG,"FETCHING WEATHER FROM API!!");
        String location = Utility.getPreferredLocation(getActivity());
        FetchWeatherTask task = new FetchWeatherTask(getActivity());
        task.execute(location);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_refresh:
                fetchWeatherData();
                return true;
            case R.id.action_db_view:
                Intent intent = new Intent(getActivity(),DBActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mLastListPos != ListView.INVALID_POSITION) {
            outState.putInt(POSITION_STATE_KEY, mLastListPos);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        if(savedInstanceState!= null && savedInstanceState.containsKey(POSITION_STATE_KEY)) {
            mLastListPos = savedInstanceState.getInt(POSITION_STATE_KEY);
        }

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        final Uri weatherForLocation = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting,System.currentTimeMillis()
        );
        ListView lv = (ListView)rootView.findViewById(R.id.listview_forecast);
        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        lv.setAdapter(mForecastAdapter);
        lv.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                                       @Override
              public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //  pass it a uri to fetch the data..Detail Activity implements LoaderCallbacks to get it
                    Cursor cursor = (Cursor)parent.getItemAtPosition(position);
                    if(cursor != null){
                        mLastListPos = position;
                        Long itemDate = cursor.getLong(COL_WEATHER_DATE);
                        String prefLoc = Utility.getPreferredLocation(getActivity());
                        Uri weatherItemUri =
                           WeatherContract.WeatherEntry.buildWeatherByLocationAndDate(prefLoc,itemDate);
                        ((Callback)getActivity()).onItemSelected(weatherItemUri);
                    }
              }
            }
        );
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_ID,null,this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
        if(mLastListPos != ListView.INVALID_POSITION) {
            ListView lv = (ListView) getView().findViewById(R.id.listview_forecast);
            lv.smoothScrollToPosition(mLastListPos);
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG,"FORECAST LOADER CREATING");
        if(id == LOADER_ID){
            String prefLocation = Utility.getPreferredLocation(getActivity());
            String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
            Uri uri =
                    WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(prefLocation, System.currentTimeMillis());
            return new CursorLoader(getActivity(),
                   uri, FORECAST_COLUMNS, null, null,sortOrder);
        }else{
            return null;
        }
    }
}
