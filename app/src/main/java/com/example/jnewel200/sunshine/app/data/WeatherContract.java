package com.example.jnewel200.sunshine.app.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jnewel200 on 3/12/2015.
 */
public class WeatherContract {
    public static final String CONTENT_AUTHORITY = "com.example.jnewel200.sunshine.app";
    public static final Uri BASE_CONTENT_URI =
            Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_WEATHER = "weather";
    public static final String PATH_LOCATION = "location";

    public static final Long normalizeData(Long startDate){
        Time time = new Time();
        time.setToNow();
        int julianDay = Time.getJulianDay(startDate,time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    public static final class WeatherEntry implements BaseColumns{
        /*----------URI interface configurations for ContentProvider---------------*/
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_WEATHER).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" +
                PATH_WEATHER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" +
                PATH_WEATHER;

        public static Uri buildWeatherUri(Long id){
            //TODO: resolve this ambiguity....is the id for the db record or what?
            // content://auth/weather/id #id for the actual db record??
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }
        public static Uri buildWeatherLocation(String locationSetting){
            return CONTENT_URI.buildUpon().appendPath(locationSetting).build();
        }
        public static Uri buildWeatherLocationWithStartDate(String locationSetting, Long startDate){
            //TODO: make sure this actually works...plus the interface is not clear to me
            Long normalizedDate = normalizeData(startDate);
            return CONTENT_URI.buildUpon().appendPath(locationSetting)
                    .appendQueryParameter(COLUMN_DATE, Long.toString(normalizedDate))
                    .build();
        }

        public static String getLocationSettingFromUri( Uri uri){
            //get the second path segment (after the domain-authority: auth/path/LOC)
            return uri.getPathSegments().get(1);
        }
        public static Long getDateFromUri(Uri uri){
            //get the third path segment (after the domain-auth: auth/path/loc/DATE)
            return Long.parseLong(uri.getPathSegments().get(2));
        }

        public static Long getStartDateFromUri(Uri uri){
            // auth/path/...?date=startDate
            String dateString = uri.getQueryParameter(COLUMN_DATE);
            if(dateString != null && dateString.length() > 0){
                return Long.parseLong(dateString);
            }else{
                return 0L;
            }
        }

        /*----------END---------------------------------------------------------*/

        /*------Internal Database definitions-----------------------------------*/
        public static final String TABLE_NAME = "weather";
        public static final String COLUMN_LOC_KEY = "location_id";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_WEATHER_ID = "weather_id";
        public static final String COLUMN_SHORT_DESC = "short_desc";
        public static final String COLUMN_MIN_TEMP = "min";
        public static final String COLUMN_MAX_TEMP = "max";
        // Humidity is stored as a float representing percentage
        public static final String COLUMN_HUMIDITY = "humidity";
        // Humidity is stored as a float representing percentage
        public static final String COLUMN_PRESSURE = "pressure";
        // Windspeed is stored as a float representing windspeed  mph
        public static final String COLUMN_WIND_SPEED = "wind";
        // Degrees are meteorological degrees (e.g, 0 is north, 180 is south).  Stored as floats.
        public static final String COLUMN_DEGREES = "degrees";

        /*---------END---------------------------------------------------------*/

    }
    public static final class LocationEntry implements BaseColumns{
        /*----------URI interface configurations for ContentProvider---------------*/
        public static final Uri CONTENT_URI =
                 BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                        CONTENT_AUTHORITY + "/" +
                        PATH_LOCATION;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                        CONTENT_AUTHORITY + "/" +
                        PATH_LOCATION;
        public static Uri buildLocationUri(Long id){
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }

        /*----------END---------------------------------------------------------*/

        /*------Internal Database definitions-----------------------------------*/
        public static final String TABLE_NAME = "location";
        public static final String COLUMN_LOCATION_SETTING = "location_setting";
        public static final String COLUMN_CITY_NAME = "city_name";
        public static final String COLUMN_COORD_LAT = "coord_lat";
        public static final String COLUMN_COORD_LONG = "coord_long";
        /*---------END---------------------------------------------------------*/
    }
}
