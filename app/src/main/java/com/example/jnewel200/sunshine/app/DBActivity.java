package com.example.jnewel200.sunshine.app;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.jnewel200.sunshine.app.data.WeatherContract;

import java.util.ArrayList;


public class DBActivity extends ActionBarActivity {
    private DBViewFragment mDBViewFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db);
        mDBViewFragment = new DBViewFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mDBViewFragment)
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
        if(id == R.id.action_db_location){
            //  I want to then call loadWeatherForLocation()...
            mDBViewFragment.loadLocations();
            return true;
        }
        if(id == R.id.action_db_weather){
            //  I want to then call loadWeatherForLocation()...
            mDBViewFragment.loadWeatherForLocation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DBViewFragment extends Fragment {
        ArrayAdapter<String> mDBViewAdapter;
        public DBViewFragment() {
            setHasOptionsMenu(true);
        }

        void loadLocations(){
            if(mDBViewAdapter == null) return;

            Cursor cursor = getActivity().getContentResolver().query(
                    WeatherContract.LocationEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            );
            try {
                if (cursor.moveToFirst()) {
                    int _idIdx = cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
                    int cityNameIdx = cursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_CITY_NAME);
                    int locQueryIdx = cursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);
                    mDBViewAdapter.clear();
                    //List<String> dbRowsList = new ArrayList<String>();
                    //header row
                    mDBViewAdapter.add(WeatherContract.LocationEntry._ID + "\t| " +
                            WeatherContract.LocationEntry.COLUMN_CITY_NAME + "\t| " +
                            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);
                    do{
                        long _id = cursor.getLong(_idIdx);
                        String cityName = cursor.getString(cityNameIdx);
                        String locationQuery = cursor.getString(locQueryIdx);
                        mDBViewAdapter.add(String.format("%d\t | %s\t| %s",_id, cityName,locationQuery));
                    }while(cursor.moveToNext());
                }
            }finally{
                if(cursor != null)
                    cursor.close();
            }
        }

        void loadWeatherForLocation(){
            if(mDBViewAdapter == null) return;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String curLocationSetting = prefs.getString(
                    getString(R.string.pref_location_key),
                    getString(R.string.pref_location_default)
            );

            Cursor cursor = getActivity().getContentResolver().query(
                    WeatherContract.WeatherEntry.buildWeatherLocation(curLocationSetting),
                    null,
                    null,
                    null,
                    WeatherContract.WeatherEntry.COLUMN_DATE + " ASC"
            );
            try {
                if (cursor.moveToFirst()) {
                    int _idIdx = cursor.getColumnIndex(WeatherContract.WeatherEntry._ID);
                    int dateIdx = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
                    int descIdx = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);
                    int maxTempIdx = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
                    int minTempIdx = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP);

                    mDBViewAdapter.clear();
                    //List<String> dbRowsList = new ArrayList<String>();
                    //header row
                    mDBViewAdapter.add(WeatherContract.WeatherEntry._ID + "\t| " +
                                    WeatherContract.WeatherEntry.COLUMN_DATE + "\t| " +
                                    WeatherContract.WeatherEntry.COLUMN_SHORT_DESC + "\t|" +
                                    WeatherContract.WeatherEntry.COLUMN_MAX_TEMP + "\t|" +
                                    WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
                    );
                    do{
                        long _id = cursor.getLong(_idIdx);
                        long date = cursor.getLong(dateIdx);
                        String desc = cursor.getString(descIdx);
                        double maxTemp = cursor.getDouble(maxTempIdx);
                        double minTemp = cursor.getDouble(minTempIdx);
                        mDBViewAdapter.add(String.format("%d\t | %d\t| %s\t | %f\t | %f",
                                _id, date,desc, maxTemp, minTemp));
                    }while(cursor.moveToNext());
                }
            }finally{
                if(cursor != null)
                    cursor.close();
            }
        }

        @Override
        public void onStart(){
            super.onStart();
            loadLocations();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_db, container, false);
            //String [] dbRowsInit = new String [] {"id  |  date  |  descr", " 1   |  12/3/2015 | first item"};
            //List<String> dbRowsInitList = Arrays.asList(dbRowsInit);
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
