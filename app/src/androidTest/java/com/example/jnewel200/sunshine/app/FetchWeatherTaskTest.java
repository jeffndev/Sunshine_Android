package com.example.jnewel200.sunshine.app;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.example.jnewel200.sunshine.app.data.TestUtilities;
import com.example.jnewel200.sunshine.app.data.WeatherContract;
import com.example.jnewel200.sunshine.app.data.WeatherContract.WeatherEntry;
import com.example.jnewel200.sunshine.app.data.WeatherContract.LocationEntry;

/**
 * Created by jnewel200 on 3/18/2015.
 */
public class FetchWeatherTaskTest extends AndroidTestCase {
    static class TestSouthPoleLocation {
        final static String TEST_LOCATION_SETTING = "XXXXXXX,SP";
        final static String TEST_CITY_NAME = "South Pole";
        final static double TEST_COORD_LONG = 99.555;
        final static double TEST_COORD_LAT = 99.555;
        final static long TEST_START_DATE = 1416916800000L; //UTC nov 25, 2014 at noon, millis
        final static long MILLIS_IN_A_DAY = 86400000L;
        final static int NUM_WEATHER_ENTRIES = 5;

        static ContentValues getLocationForInsert(){
            ContentValues newValues = new ContentValues();
            newValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME,TEST_CITY_NAME);
            newValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, TEST_LOCATION_SETTING);
            newValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT,TEST_COORD_LAT);
            newValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, TEST_COORD_LONG);
            return newValues;
        }
        static ContentValues [] getTestWeatherEntries(long testLocationKey){
            ContentValues weatherRows [] = new ContentValues[NUM_WEATHER_ENTRIES];
            for(int i = 0; i != NUM_WEATHER_ENTRIES; i++){
                ContentValues cv = new ContentValues();
                cv.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY,testLocationKey);
                cv.put(WeatherContract.WeatherEntry.COLUMN_DATE, TEST_START_DATE + MILLIS_IN_A_DAY *i);
                cv.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, 5 + (i%2)*7);
                cv.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, 45 + (i%3)*10);
                cv.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, 4 + (i-1)*4 );
                cv.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, -1 + (i-1)*4);
                cv.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, 120 + (i%2)*13);
                cv.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, "cold, cold yah:" + i );
                cv.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, 2 + i);
                cv.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, 10 + i*4);
                weatherRows[i] = cv;
            }
            return weatherRows;
        }
    }

    public void testAddLocationHelper_Returns_Existing_ID_For_Existing_Location(){
        long locId = 0L;
        Cursor cursor = mContext.getContentResolver().query(LocationEntry.CONTENT_URI,
                new String [] {LocationEntry._ID},
                LocationEntry.COLUMN_LOCATION_SETTING + "=?",
                new String [] {TestSouthPoleLocation.TEST_LOCATION_SETTING},
                null);
        if(!cursor.moveToFirst()){
            //add it
            Uri insertedUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI,
                    TestSouthPoleLocation.getLocationForInsert());
            locId = ContentUris.parseId(insertedUri);
        }else{
            locId = cursor.getLong(cursor.getColumnIndex(LocationEntry._ID));
        }

        assertNotSame(0, locId);
        FetchWeatherTask task = new FetchWeatherTask(mContext);
        long locIdFromTask = task.addLocation(TestSouthPoleLocation.TEST_LOCATION_SETTING,
                                TestSouthPoleLocation.TEST_CITY_NAME,
                                TestSouthPoleLocation.TEST_COORD_LAT,
                                TestSouthPoleLocation.TEST_COORD_LONG
                );
        assertEquals(locId, locIdFromTask);
        int numDeleted = mContext.getContentResolver().delete(LocationEntry.CONTENT_URI,
                        LocationEntry._ID + "=?",
                        new String [] {Long.toString(locId)});
        assertEquals(1,numDeleted);
    }

    public void testAddLocationHelper_Adds_New_Location_when_Location_Not_There(){
        int numDeleted = mContext.getContentResolver().delete(LocationEntry.CONTENT_URI,
                LocationEntry.COLUMN_LOCATION_SETTING + "=?",
                new String [] {TestSouthPoleLocation.TEST_LOCATION_SETTING});


        FetchWeatherTask task = new FetchWeatherTask(mContext);
        long locIdFromTask = task.addLocation(TestSouthPoleLocation.TEST_LOCATION_SETTING,
                TestSouthPoleLocation.TEST_CITY_NAME,
                TestSouthPoleLocation.TEST_COORD_LAT,
                TestSouthPoleLocation.TEST_COORD_LONG
        );
        assertTrue(locIdFromTask > 0L);

        Cursor cursor = mContext.getContentResolver().query(LocationEntry.buildLocationUri(locIdFromTask),
                null,null,null, null);
        assertTrue(cursor.moveToFirst());
        TestUtilities.validateCurrentRecord("",cursor, TestSouthPoleLocation.getLocationForInsert());

        numDeleted = mContext.getContentResolver().delete(LocationEntry.CONTENT_URI,
                LocationEntry._ID + "=?",
                new String [] {Long.toString(locIdFromTask)});
        assertEquals(1,numDeleted);
    }
}
