package com.example.jnewel200.sunshine.app.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;

import org.apache.http.client.utils.URIUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jnewel200 on 3/12/2015.
 */
public class WeatherProvider extends ContentProvider {


    static final int WEATHER = 100;
    static final int LOCATION = 300;
    static final int WEATHER_WITH_LOCATION = 101;
    static final int WEATHER_WITH_LOCATION_AND_DATE = 102;

    private WeatherDbHelper mDBHelper;

    private static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        //content://auth/weather/
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY ,
                WeatherContract.PATH_WEATHER,
                WEATHER);
        //content://auth/weather/*    by location query string
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_WEATHER + "/*",
                WEATHER_WITH_LOCATION);
        //content://auth/weather/*/#
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_WEATHER + "/*/#",
                WEATHER_WITH_LOCATION_AND_DATE);
        //content://auth/location
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY ,
                        WeatherContract.PATH_LOCATION,
                        LOCATION);

        return matcher;
    }

/*-----------------ContentProvider protocols-----------------------------------*/
    @Override
    public boolean onCreate() {
        mDBHelper = new WeatherDbHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int match = buildUriMatcher().match(uri);

        switch(match){
            case WEATHER:
                String whereString = "";
                String whereParms[] = new String[]{};
                List<String> whereParmsList = new ArrayList<String>();
                Long recordId = ContentUris.parseId(uri);
                if(recordId > 0){
                    whereString = WeatherContract.WeatherEntry._ID + "=? ";
                    whereParmsList.add( Long.toString(recordId));
                }
                String dateString =
                        uri.getQueryParameter(WeatherContract.WeatherEntry.COLUMN_DATE);
                Long date = -1L;
                if (dateString != null && dateString.length() > 0) {
                    date = Long.parseLong(dateString);
                    date = WeatherContract.normalizeData(date);
                    if(whereString.length() > 0) {
                        whereString = whereString + " AND ";
                    }
                    whereString = whereString + WeatherContract.WeatherEntry.COLUMN_DATE + "=? ";
                    whereParmsList.add(Long.toString(date));
                }
                whereParms = whereParmsList.toArray(new String [whereParmsList.size()]);
                return mDBHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        null,
                        whereString,
                        whereParms,
                        null,
                        null,
                        null);

            case WEATHER_WITH_LOCATION:

                break;
            case WEATHER_WITH_LOCATION_AND_DATE:

                break;
            case LOCATION:

                break;
            default:
                //oops, bad
                break;
        }


        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        return super.bulkInsert(uri, values);

    }
 /*-----------------END ContentProvider protocols-----------------------------------*/

}
