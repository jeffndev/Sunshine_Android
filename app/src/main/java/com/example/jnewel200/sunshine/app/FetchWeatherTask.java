package com.example.jnewel200.sunshine.app;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.jnewel200.sunshine.app.data.WeatherContract;
import com.example.jnewel200.sunshine.app.data.WeatherContract.LocationEntry;
import com.example.jnewel200.sunshine.app.data.WeatherContract.WeatherEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Vector;

/**
 * Created by jnewel200 on 3/18/2015.
 */
public class FetchWeatherTask extends AsyncTask<String, Void, String [] > {
    public static final int WEATHER_CHUNK_SIZE = 14;
    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
    private Context mContext;
    private ArrayAdapter<String> mForecastAdapter;

    public FetchWeatherTask(Context context, ArrayAdapter<String> forecastAdapter){
        mContext = context;
        mForecastAdapter = forecastAdapter;
    }

    private String [] getWeatherDataFromDb(String locationSetting){
        String [] uiFormattedWeatherEntries = null;
        Cursor cursor = mContext.getContentResolver().query(
                WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis()),
                null,null,null,null);
        if(!cursor.moveToFirst()){ //no data from this, so just return null
            return null;
        }

        if(cursor.getCount() != WEATHER_CHUNK_SIZE){
            long locKey = cursor.getLong(cursor.getColumnIndex(WeatherEntry.COLUMN_LOC_KEY));
            //delete weather for this location, so you can refresh from OWM API
            int numDeleted = mContext.getContentResolver().delete(WeatherEntry.CONTENT_URI,
                    WeatherEntry.COLUMN_LOC_KEY + "=?",
                    new String [] { Long.toString(locKey)}
            );
            if(numDeleted < 1){
                Log.d(LOG_TAG,
                  "FetchWeatherFromDb cleared out the old forecasts didn't delete..WARNING..should be something");
            }
            uiFormattedWeatherEntries = null;
        }else{
            //ContentValues [] valsArray = new ContentValues[WEATHER_CHUNK_SIZE];
            Vector<ContentValues> cvvs = new Vector<ContentValues>();
            for(int i=0;i != WEATHER_CHUNK_SIZE;i++)
            {
                cvvs.add(weatherCursorRowToContentValues(cursor));
                //valsArray[i] = weatherCursorRowToContentValues(cursor);
                cursor.moveToNext();
            }
            uiFormattedWeatherEntries = contentValuesToUXStringArray(cvvs);
        }
        return uiFormattedWeatherEntries;
    }

    public String [] contentValuesToUXStringArray(Vector<ContentValues> cvv){
        String[] resultStrs = new String[cvv.size()];
        for ( int i = 0; i < cvv.size(); i++ ) {
            ContentValues weatherValues = cvv.elementAt(i);
            String highAndLow = formatHighLows(
                    weatherValues.getAsDouble(WeatherEntry.COLUMN_MAX_TEMP),
                    weatherValues.getAsDouble(WeatherEntry.COLUMN_MIN_TEMP));
            resultStrs[i] = getReadableDateString(
                    weatherValues.getAsLong(WeatherEntry.COLUMN_DATE)) +
                    " - " + weatherValues.getAsString(WeatherEntry.COLUMN_SHORT_DESC) +
                    " - " + highAndLow;
        }
        return resultStrs;
    }
    public ContentValues weatherCursorRowToContentValues(Cursor cursorRow){
        long locationId = cursorRow.getLong(cursorRow.getColumnIndex(WeatherEntry.COLUMN_LOC_KEY));
        long dateTime = cursorRow.getLong(cursorRow.getColumnIndex(WeatherEntry.COLUMN_DATE));
        double humidity = cursorRow.getDouble(cursorRow.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY));
        double pressure = cursorRow.getDouble(cursorRow.getColumnIndex(WeatherEntry.COLUMN_PRESSURE));
        double windSpeed = cursorRow.getDouble(cursorRow.getColumnIndex(WeatherEntry.COLUMN_WIND_SPEED));
        double windDirection = cursorRow.getDouble(cursorRow.getColumnIndex(WeatherEntry.COLUMN_DEGREES));
        double high = cursorRow.getDouble(cursorRow.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP));
        double low = cursorRow.getDouble(cursorRow.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP));
        String description = cursorRow.getString(cursorRow.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC));
        int weatherId = cursorRow.getInt(cursorRow.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID));

        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationId);
        weatherValues.put(WeatherEntry.COLUMN_DATE, dateTime);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, windDirection);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, high);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, low);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, description);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherId);

        return weatherValues;
    }

    private String [] getWeatherDataFromOWMApi(String locationSetting){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String forecastJsonStr = null;
        String format = "json";
        String units = "metric";
        int numDays = WEATHER_CHUNK_SIZE;
        try{
            final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, locationSetting)
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
        String uiFormattedWeatherEntries [] = null;
        try{
            uiFormattedWeatherEntries = getWeatherDataFromJson(forecastJsonStr, locationSetting);
        }catch(JSONException e){
            uiFormattedWeatherEntries = null;
        }
        return uiFormattedWeatherEntries;
    }

    @Override
    protected String [] doInBackground(String... parms){
        if(parms == null)
            Log.v(LOG_TAG, "null parms to doInBackground");
        else
            Log.v(LOG_TAG,"hit doInBackgroud with parms count: " + parms.length);
        if(parms.length == 0){
            return null;
        }
        String locationSetting = parms[0];
        String [] uiFormattedWeatherEntries = getWeatherDataFromDb(locationSetting);
        if(uiFormattedWeatherEntries == null){
            Log.v(LOG_TAG, "FETCHING FROM WEB API!!");
            uiFormattedWeatherEntries = getWeatherDataFromOWMApi(locationSetting);
        }else{
            Log.v(LOG_TAG, "SUCCESS!! FETCHING FROM Local DB!!");
        }
        return uiFormattedWeatherEntries;
    }

        /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */

    @Override
    protected void onPostExecute(String[] forecasts) {
       if(mForecastAdapter != null && forecasts != null){
           mForecastAdapter.clear();
           for(String forecastEntry : forecasts){
               mForecastAdapter.add(forecastEntry);
           }
       }
    }

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
        String units = mContext.getString(R.string.pref_units_default);
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            units = prefs.getString(
                    mContext.getString(R.string.pref_units_key),
                    mContext.getString(R.string.pref_units_default)
            );
        }catch(RuntimeException e){
            Log.e(LOG_TAG,"formatHighLows had problem getting preferences", e);
        }
        boolean bImperial = false;
        if(units.equals(mContext.getString(R.string.pref_units_imperial))){
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
    public long addLocation(String locationSetting, String cityName, double lat, double lon) {
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                new String [] {LocationEntry._ID},
                LocationEntry.COLUMN_LOCATION_SETTING + "=?",
                new String [] {locationSetting},
                null);
        long locKey = 0L;
        if(cursor.moveToFirst()){
            locKey = cursor.getLong(cursor.getColumnIndex(LocationEntry._ID));
        }else{
            ContentValues contentValues = new ContentValues();
            contentValues.put(LocationEntry.COLUMN_LOCATION_SETTING,locationSetting);
            contentValues.put(LocationEntry.COLUMN_CITY_NAME, cityName);
            contentValues.put(LocationEntry.COLUMN_COORD_LAT,lat);
            contentValues.put(LocationEntry.COLUMN_COORD_LONG,lon);
            Uri insertUri = mContext.getContentResolver().insert(
                    LocationEntry.CONTENT_URI, contentValues);
            locKey = ContentUris.parseId(insertUri);
        }
        return locKey;
    }
    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] getWeatherDataFromJson(String forecastJsonStr, String locationSetting)
            throws JSONException {
        // These are the names of the JSON objects that need to be extracted.
        // Location information
        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";

        // Location coordinate
        final String OWM_LATITUDE = "lat";
        final String OWM_LONGITUDE = "lon";
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";
        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";
        final String OWM_WEATHER_ID = "id";

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

        JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
        String cityName = cityJson.getString(OWM_CITY_NAME);

        JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
        double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
        double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);
        long locationId = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);

        String[] resultStrs = new String[WEATHER_CHUNK_SIZE];
        // Insert the new weather information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());
        for(int i = 0; i < weatherArray.length(); i++) {

            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;
            int weatherId;
            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);
            pressure = dayForecast.getDouble(OWM_PRESSURE);
            humidity = dayForecast.getInt(OWM_HUMIDITY);
            windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
            windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);
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
            weatherId = weatherObject.getInt(OWM_WEATHER_ID);
            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);



            ContentValues weatherValues = new ContentValues();

            weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationId);
            weatherValues.put(WeatherEntry.COLUMN_DATE, dateTime);
            weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, description);
            weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherId);

            //TODO: remove this and use the bulk insert
            mContext.getContentResolver().insert(WeatherEntry.CONTENT_URI, weatherValues);

            cVVector.add(weatherValues); //TODO: use bulk insert when implemented
            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }
        return resultStrs;
    }
}