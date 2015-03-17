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
    //ArrayAdapter<String> mForecastAdapter;
    ForecastAdapter mForecastAdapter;
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
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = prefs.getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default)
        );
        Log.v(LOG_TAG,"in fetchWeatherData, pref received: " + location);
        FetchWeatherTask task = new FetchWeatherTask();
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
        String loccationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocation = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                loccationSetting,System.currentTimeMillis()
        );
        Cursor cursor = getActivity().getContentResolver().query(weatherForLocation,null,null,null, sortOrder);
        mForecastAdapter = new ForecastAdapter(getActivity(),cursor,0);

//        mForecastAdapter = new ArrayAdapter<String>(
//                getActivity(),
//                R.layout.list_item_forcecast,
//                R.id.list_item_forecast_textview,
//                new ArrayList<String>());
        ListView lv = (ListView)rootView.findViewById(R.id.listview_forecast);
        lv.setAdapter(mForecastAdapter);
//        lv.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
//                String forecast = mForecastAdapter.getItem(pos);
//                Intent detailIntent = new Intent(getActivity(),DetailActivity.class)
//                        .putExtra(Intent.EXTRA_TEXT,forecast);
//
//                startActivity(detailIntent);
//            }
//        });
        return rootView;
    }
/*----------------------FETCH WEATHER TASK (AsyncTask)----------------------*/
    public class FetchWeatherTask extends AsyncTask<String, Void, String []> {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
/*
        private String [] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException{
            JSONObject mainObj = new JSONObject(forecastJsonStr);
            String weatherStrings [] = new String[numDays];
            JSONArray daysArray = mainObj.getJSONArray("list");
            for(int i = 0;i < numDays; i++){
                JSONObject dayObj = daysArray.getJSONObject(i);
                double maxTemp = dayObj.getJSONObject("temp").getDouble("max");
                double minTemp = dayObj.getJSONObject("temp").getDouble("min");
                String weatherSummary = dayObj.getJSONArray("weather").getJSONObject(0).getString("main");
                weatherStrings[i] = weatherSummary + " " + maxTemp + "/" + minTemp;
            }
            return weatherStrings;
        }
*/
//        @Override
//        protected void onPostExecute(String [] forecasts){
//            if(forecasts == null)
//                Log.v(LOG_TAG,"hit onPostExecute, null forecasts..");
//            else
//                Log.v(LOG_TAG,"hit onPostExecute: numForecasts: " + forecasts.length);
//            if(forecasts != null){
//                Log.v(LOG_TAG,"hit onPostExecute: first forecast: " + forecasts[0]);
//                mForecastAdapter.clear();
//                for(String s: forecasts)
//                    mForecastAdapter.add(s);
//            }
//        }

        @Override
        protected String [] doInBackground(String... parms){
            if(parms == null)
                Log.v(LOG_TAG,"null parms to doInBackground");
            else
                Log.v(LOG_TAG,"hit doInBackgroud with parms count: " + parms.length);
            if(parms.length == 0){
                return null;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;
            String format = "json";
            String units = "metric";
            int numDays = 7;
            try{
                final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                                .appendQueryParameter(QUERY_PARAM, parms[0])
                                .appendQueryParameter(FORMAT_PARAM, format)
                                .appendQueryParameter(UNITS_PARAM, units)
                                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                                .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if(inputStream == null){
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String ln;
                while((ln = reader.readLine()) != null){
                    buffer.append(ln + "\n");
                }
                if(buffer.length() == 0){
                    return null;
                }
                forecastJsonStr = buffer.toString();
            }catch(IOException e){
                Log.e(LOG_TAG,"Error ",e );
                return null;
            }finally{
                if(urlConnection != null)
                    urlConnection.disconnect();
                if(reader != null){
                    try{
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream",e);
                    }
                }
            }
            String forecastStrings [] = null;
            try{
                forecastStrings = getWeatherDataFromJson(forecastJsonStr, numDays);
            }catch(JSONException e){
                forecastStrings = null;
            }

            return forecastStrings;
        }

        /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */

        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }
        /**
     * Prepare the weather high/lows for presentation.
     */

        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            String units = getString(R.string.pref_units_default);
            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                units = prefs.getString(
                        getString(R.string.pref_units_key),
                        getString(R.string.pref_units_default)
                );
            }catch(RuntimeException e){
                Log.e(LOG_TAG,"formatHighLows had problem getting preferences", e);
            }
            boolean bImperial = false;
            if(units.equals(getString(R.string.pref_units_imperial))){
                bImperial = true;
                high = (1.8*high)+32;
                low = (1.8*low)+32;
            }
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);
            String highLowStr = roundedHigh + (bImperial?"°/":"/")
                        + roundedLow
                        + (bImperial?"°":"");
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {
            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.
            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.
            Time dayTime = new Time();
            dayTime.setToNow();
            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
            // now we work exclusively in UTC
            dayTime = new Time();
            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;
                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);
                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);
                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }
            return resultStrs;
        }
    }
}