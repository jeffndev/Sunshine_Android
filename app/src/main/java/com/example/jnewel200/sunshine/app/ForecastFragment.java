package com.example.jnewel200.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.text.format.Time;
import android.widget.Toast;

import com.example.jnewel200.sunshine.app.data.WeatherContract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ForecastFragment extends Fragment {
    ArrayAdapter<String> mForecastAdapter;
    //ForecastAdapter mForecastAdapter;
    private final String LOG_TAG = ForecastFragment.class.getSimpleName();

    public ForecastFragment() {
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
        String location = Utility.getPreferredLocation(getActivity());

        Log.v(LOG_TAG,"in fetchWeatherData, pref received: " + location);
        FetchWeatherTask task = new FetchWeatherTask(getActivity(), mForecastAdapter);
        task.execute(location);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_refresh:
                fetchWeatherData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onStart(){
        Log.v(LOG_TAG,"Starting onStart(), should see pre-FetchWeather log next");
        super.onStart();
        Log.v(LOG_TAG,"About to FetchWeatherData..");
        fetchWeatherData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocation = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting,System.currentTimeMillis()
        );
//        Cursor cursor = getActivity().getContentResolver().query(
//                weatherForLocation,null,null,null, sortOrder);
//        mForecastAdapter = new ForecastAdapter(getActivity(),cursor,0);

        mForecastAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forcecast,
                R.id.list_item_forecast_textview,
                new ArrayList<String>());
        ListView lv = (ListView)rootView.findViewById(R.id.listview_forecast);
        lv.setAdapter(mForecastAdapter);
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                String forecast = mForecastAdapter.getItem(pos);
                Intent detailIntent = new Intent(getActivity(),DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT,forecast);

                startActivity(detailIntent);
            }
        });
        return rootView;
    }
}
