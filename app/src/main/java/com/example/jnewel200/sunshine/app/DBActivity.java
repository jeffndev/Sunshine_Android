package com.example.jnewel200.sunshine.app;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.jnewel200.sunshine.app.data.WeatherContract;

import java.util.ArrayList;


public class DBActivity extends ActionBarActivity{

    //private DBViewFragment mDBViewFragment;
    private final String DBVIEWFRAGMENT_TAG = "DBVIEW";
    public enum TABLE_VIEWING {LOCATION, WEATHER};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db);
        //mDBViewFragment = new DBViewFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DBViewFragment(),DBVIEWFRAGMENT_TAG)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_db, menu);
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        DBViewFragment dbf;
        if(id == R.id.action_db_location){
            //  I want to then call loadWeatherForLocation()...
            dbf = (DBViewFragment)getSupportFragmentManager().findFragmentByTag(DBVIEWFRAGMENT_TAG);
            dbf.loadLocations();
            return true;
        }
        if(id == R.id.action_db_weather){
            //  I want to then call loadWeatherForLocation()...
            dbf = (DBViewFragment)getSupportFragmentManager().findFragmentByTag(DBVIEWFRAGMENT_TAG);
            dbf.loadWeatherForLocation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DBViewFragment extends Fragment
            implements LoaderManager.LoaderCallbacks<Cursor>{

        private static final int DBVIEW_LOADER_ID = 301;
        private TABLE_VIEWING currentViewMode = TABLE_VIEWING.LOCATION;

        ArrayAdapter<String> mDBViewAdapter;
        public DBViewFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            getLoaderManager().initLoader(DBVIEW_LOADER_ID, null, this);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Uri dbUri;
            String sortOrder; //by table id
            switch(currentViewMode){
                case LOCATION:
                    sortOrder = WeatherContract.LocationEntry._ID;
                    dbUri = WeatherContract.LocationEntry.CONTENT_URI;
                    break;
                case WEATHER:
                    sortOrder = WeatherContract.WeatherEntry._ID;
                    String curLocationSetting = Utility.getPreferredLocation(getActivity());
                    dbUri = WeatherContract.WeatherEntry.buildWeatherLocation(curLocationSetting);
                    break;
                default:
                    return null;
            }
            return new CursorLoader(getActivity(),
                    dbUri, null, null, null, sortOrder);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) { /*empty*/  }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if(!data.moveToFirst()) return;
            //TODO: figure out the RIGHT way to deal with this,
            //  an interesting problem, in that the Cursor is maintained
            //  between orientation changes, BUT the Mode of this Fragment
            //  is NOT maintained...
            mDBViewAdapter.clear();
            mDBViewAdapter.add(buildHeaderRow());
            do{
                mDBViewAdapter.add(buildCurrentDataRow(data));
            }while(data.moveToNext());
        }

        private String buildCurrentDataRow(Cursor cursor){
            if(cursor == null) return null;
            String formattedRow;
            switch (currentViewMode){
                case LOCATION: {
                    int _idIdx = cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
                    int cityNameIdx = cursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_CITY_NAME);
                    int locQueryIdx = cursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);
                    long _id = cursor.getLong(_idIdx);
                    String cityName = cursor.getString(cityNameIdx);
                    String locationQuery = cursor.getString(locQueryIdx);
                    formattedRow = String.format("%d\t | %s\t| %s",_id, cityName,locationQuery);
                    }
                    break;
                case WEATHER:{
                    //get cursor column indexes, then grab the vals
                    //TODO: investigate this...it's seems like a bug in the cursor ?
                    int _idIdx = 0;//cursor.getColumnIndex(WeatherContract.WeatherEntry._ID);
                    int dateIdx = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
                    int descIdx = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);
                    int maxTempIdx = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
                    int minTempIdx = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP);
                    int idLocIdx = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_LOC_KEY);
                    int humidityIdx = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_HUMIDITY);
                    int weatherConditionIdIdx = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID);
                    //get values and prepare the output string...
                    long _id = cursor.getLong(_idIdx);
                    long date = cursor.getLong(dateIdx);
                    String dateString = Utility.formatDate(date);
                    String desc = cursor.getString(descIdx);
                    double maxTemp = cursor.getDouble(maxTempIdx);
                    double minTemp = cursor.getDouble(minTempIdx);
                    int locationId = cursor.getInt(idLocIdx);
                    double humidity = cursor.getDouble(humidityIdx);
                    int weatherConditionId = cursor.getInt(weatherConditionIdIdx);
                    formattedRow =
                       String.format("%d\t | %d\t| %s\t| %s\t | %f\t | %f\t | %f\t | %d\t | %d",
                        _id, date, dateString, desc, maxTemp, minTemp, humidity, weatherConditionId, locationId);
                    }
                    break;
                default:
                    formattedRow = null;
            }
            return formattedRow;
        }
        private String buildHeaderRow(){
            switch(currentViewMode){
                case LOCATION:
                    return WeatherContract.LocationEntry._ID + "\t| " +
                            WeatherContract.LocationEntry.COLUMN_CITY_NAME + "\t| " +
                            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING;
                case WEATHER:
                    return WeatherContract.WeatherEntry._ID + "\t| " +
                            WeatherContract.WeatherEntry.COLUMN_DATE + "\t| " +
                            " formatted-date\t| " +
                            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC + "\t|" +
                            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP + "\t|" +
                            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP + "\t|" +
                            WeatherContract.WeatherEntry.COLUMN_HUMIDITY +  "\t|" +
                            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID +  "\t|" +
                            WeatherContract.WeatherEntry.COLUMN_LOC_KEY;
                default:
                    return null;
            }

        }
        void loadLocations(){
            currentViewMode = TABLE_VIEWING.LOCATION;
            getLoaderManager().restartLoader(DBVIEW_LOADER_ID,null,this);
        }

        void loadWeatherForLocation(){
            currentViewMode = TABLE_VIEWING.WEATHER;
            getLoaderManager().restartLoader(DBVIEW_LOADER_ID,null,this);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_db, container, false);
            mDBViewAdapter = new ArrayAdapter<String>(
                    getActivity(),
                    R.layout.list_item_dbview_row,
                    R.id.list_item_dbviewrow_textview,
                    new ArrayList<String>()
            );
            ListView lv = (ListView)rootView.findViewById(R.id.list_view_dbview_row);
            lv.setAdapter(mDBViewAdapter);
            return rootView;
        }
    }
}
