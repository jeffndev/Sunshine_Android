package com.example.jnewel200.sunshine.app.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.CancellationSignal;
import org.apache.http.client.utils.URIUtils;
import com.example.jnewel200.sunshine.app.data.WeatherContract.WeatherEntry;
import com.example.jnewel200.sunshine.app.data.WeatherContract.LocationEntry;


/**
 * Created by jnewel200 on 3/12/2015.
 */
public class WeatherProvider extends ContentProvider {


    static final int WEATHER = 100;
    static final int LOCATION = 300;
    static final int WEATHER_WITH_ID = 105;
    static final int WEATHER_WITH_LOCATION = 101;
    static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    static final int LOCATION_WITH_ID = 302;

    private WeatherDbHelper mDBHelper;

    private static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        //content://auth/weather/
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY ,
                WeatherContract.PATH_WEATHER,
                WEATHER);
        //content://auth/weather/
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY ,
                WeatherContract.PATH_WEATHER + "/#",
                WEATHER_WITH_ID);
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

        //content://auth/location/#
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY,
                WeatherContract.PATH_LOCATION +"/#",
                LOCATION_WITH_ID);
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
        Cursor rtnCursor = null;
        switch(match){
            case WEATHER:
                rtnCursor =  mDBHelper.getReadableDatabase().query(
                        WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            //weather/#
            case WEATHER_WITH_ID:{
                long _id = ContentUris.parseId(uri);
                rtnCursor = mDBHelper.getReadableDatabase().query(
                        WeatherEntry.TABLE_NAME,
                        projection,
                        WeatherEntry._ID + "=?",
                        new String [] {Long.toString(_id)},
                        null,null,null);
            }
                break;
            //weather/*
            case WEATHER_WITH_LOCATION: {
                //OPTIONAL startDate parameter...
                String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
                long startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);
                //assume we HAVE a location setting...
                SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
                qBuilder.setTables(WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        LocationEntry.TABLE_NAME + " ON " +
                        WeatherEntry.TABLE_NAME + "." + WeatherEntry.COLUMN_LOC_KEY + " = " +
                        LocationEntry.TABLE_NAME + "." + LocationEntry._ID);
                if (startDate == 0) {
                    //start date not entered...
                    selection = LocationEntry.TABLE_NAME + "." + LocationEntry.COLUMN_LOCATION_SETTING + "=?";
                    selectionArgs = new String[]{locationSetting};
                } else {
                    //both start date and location
                    selection = WeatherEntry.TABLE_NAME + "." + WeatherEntry.COLUMN_DATE + " >= ? AND " +
                            LocationEntry.TABLE_NAME + "." + LocationEntry.COLUMN_LOCATION_SETTING + "=?";
                    selectionArgs = new String[]{Long.toString(startDate), locationSetting};
                }
                rtnCursor = qBuilder.query(mDBHelper.getReadableDatabase(),
                        projection, selection, selectionArgs, null, null, sortOrder);
            }
                break;
            //weather/*/#
            case WEATHER_WITH_LOCATION_AND_DATE: {
                String locationSetting = WeatherEntry.getLocationSettingFromUri(uri);
                long date = WeatherEntry.getDateFromUri(uri);
                date = WeatherContract.normalizeDate(date);
                SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
                qBuilder.setTables(WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        LocationEntry.TABLE_NAME + " ON " +
                        WeatherEntry.TABLE_NAME + "." + WeatherEntry.COLUMN_LOC_KEY + " = " +
                        LocationEntry.TABLE_NAME + "." + LocationEntry._ID);
                selection = WeatherEntry.TABLE_NAME + "." + WeatherEntry.COLUMN_DATE + " = ? AND " +
                        LocationEntry.TABLE_NAME + "." + LocationEntry.COLUMN_LOCATION_SETTING + "=?";
                selectionArgs = new String[]{Long.toString(date), locationSetting};
                rtnCursor = qBuilder.query(mDBHelper.getReadableDatabase(),
                        projection, selection, selectionArgs, null, null, sortOrder);
            }
                break;
            case LOCATION:
                rtnCursor =  mDBHelper.getReadableDatabase().query(
                        LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            //location/#
            case LOCATION_WITH_ID:{
                long _id = ContentUris.parseId(uri);
                rtnCursor = mDBHelper.getReadableDatabase().query(
                        LocationEntry.TABLE_NAME,
                        projection,
                        LocationEntry._ID + "=?",
                        new String [] {Long.toString(_id)},
                        null,null,null
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        rtnCursor.setNotificationUri(getContext().getContentResolver(),uri);
        return rtnCursor;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int match = buildUriMatcher().match(uri);
        int deletedRows = 0;
        if(selection == null) selection = "1"; //little SQLite hack to get correct numDeleted back
        switch (match){
            //auth/weather/#
            case WEATHER_WITH_LOCATION:
                String locationSetting = WeatherEntry.getLocationSettingFromUri(uri);
                Cursor c = mDBHelper.getReadableDatabase().query(LocationEntry.TABLE_NAME,
                        new String [] {LocationEntry._ID},
                        LocationEntry.COLUMN_LOCATION_SETTING + "=?",
                        new String [] {locationSetting},null,null,null);
                long locKey = 0L;
                if(c.moveToFirst()) { //throw new android.database.SQLException("Failed to find Location row " + uri);
                    locKey = c.getLong(c.getColumnIndex(LocationEntry._ID));
                }
                deletedRows = mDBHelper.getWritableDatabase().delete(WeatherEntry.TABLE_NAME,
                        WeatherEntry.COLUMN_LOC_KEY + "=?",
                        new String [] {Long.toString(locKey)});
                break;
            case WEATHER:
                deletedRows = mDBHelper.getWritableDatabase().delete(
                        WeatherEntry.TABLE_NAME,selection,selectionArgs);
                break;
            case LOCATION:
                deletedRows = mDBHelper.getWritableDatabase().delete(
                        LocationEntry.TABLE_NAME,selection,selectionArgs);
                break;
            case LOCATION_WITH_ID:
                long _id = ContentUris.parseId(uri);
                deletedRows = mDBHelper.getReadableDatabase().delete(
                        LocationEntry.TABLE_NAME,
                        LocationEntry._ID + "=?",
                        new String [] {Long.toString(_id)});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(deletedRows != 0)
            getContext().getContentResolver().notifyChange(uri,null);
        return deletedRows;
    }
    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(WeatherContract.WeatherEntry.COLUMN_DATE)) {
            long dateValue = values.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);
            values.put(WeatherContract.WeatherEntry.COLUMN_DATE, WeatherContract.normalizeDate(dateValue));
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int match = buildUriMatcher().match(uri);
        Uri insertedUri = null;
        switch(match){
            case WEATHER: {
                normalizeDate(values);
                long _id = mDBHelper.getWritableDatabase().insert(WeatherEntry.TABLE_NAME, null, values);
                if(_id > 0){
                    insertedUri = WeatherEntry.buildWeatherUri(_id);
                }else{
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case LOCATION: {
                long _id =  mDBHelper.getWritableDatabase().insert(LocationEntry.TABLE_NAME,null,values);
                if(_id > 0){
                    insertedUri = LocationEntry.buildLocationUri(_id);
                }else{
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return insertedUri;
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //TODO: implement update in the provider
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        int match = buildUriMatcher().match(uri);
        switch(match){
            case WEATHER:
                return WeatherEntry.CONTENT_TYPE;
            case LOCATION:
                return LocationEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION:
                return WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int match = buildUriMatcher().match(uri);
        switch(match){
            case WEATHER:{

            }
            break;
            case LOCATION:{

            }
            break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return 0;
        //TODO: bulk insert code, with db transaction
    }
 /*-----------------END ContentProvider protocols-----------------------------------*/

}
