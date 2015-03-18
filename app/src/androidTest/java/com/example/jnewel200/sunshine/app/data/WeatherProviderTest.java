package com.example.jnewel200.sunshine.app.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.text.format.Time;

import com.example.jnewel200.sunshine.app.data.WeatherContract.*;

/**
 * Created by jnewel200 on 3/13/2015.
 */
public class WeatherProviderTest extends AndroidTestCase {


    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    static class TestLocationMinsk{
        final static String TEST_LOCATION_SETTING = "999999,USSR";
        final static String TEST_CITY_NAME = "Minsk";
        final static double TEST_COORD_LONG = 44.555;
        final static double TEST_COORD_LAT = 66.555;
        final static long TEST_START_DATE = 1423742400000L; //UTC feb 12, 2015 at noon, millis
        final static long MILLIS_IN_A_DAY = 86400000L;
        final static int NUM_WEATHER_ENTRIES = 5;

        static ContentValues getLocationForInsert(){
            ContentValues newValues = new ContentValues();
            newValues.put(LocationEntry.COLUMN_CITY_NAME,TEST_CITY_NAME);
            newValues.put(LocationEntry.COLUMN_LOCATION_SETTING, TEST_LOCATION_SETTING);
            newValues.put(LocationEntry.COLUMN_COORD_LAT,TEST_COORD_LAT);
            newValues.put(LocationEntry.COLUMN_COORD_LONG, TEST_COORD_LONG);
            return newValues;
        }
        static ContentValues [] getTestWeatherEntries(long testLocationKey){
            ContentValues weatherRows [] = new ContentValues[NUM_WEATHER_ENTRIES];
            for(int i = 0; i != NUM_WEATHER_ENTRIES; i++){
                ContentValues cv = new ContentValues();
                cv.put(WeatherEntry.COLUMN_LOC_KEY,testLocationKey);
                cv.put(WeatherEntry.COLUMN_DATE, TEST_START_DATE + MILLIS_IN_A_DAY *i);
                cv.put(WeatherEntry.COLUMN_DEGREES, 5 + (i%2)*7);
                cv.put(WeatherEntry.COLUMN_HUMIDITY, 45 + (i%3)*10);
                cv.put(WeatherEntry.COLUMN_MAX_TEMP, 4 + (i-1)*4 );
                cv.put(WeatherEntry.COLUMN_MIN_TEMP, -1 + (i-1)*4);
                cv.put(WeatherEntry.COLUMN_PRESSURE, 120 + (i%2)*13);
                cv.put(WeatherEntry.COLUMN_SHORT_DESC, "weather yah:" + i );
                cv.put(WeatherEntry.COLUMN_WEATHER_ID, 2 + i);
                cv.put(WeatherEntry.COLUMN_WIND_SPEED, 10 + i*4);
                weatherRows[i] = cv;
            }
            return weatherRows;
        }
    }
    public void testInsert_A_Location_And_Delete_It(){

        int numDeleted = mContext.getContentResolver().delete(LocationEntry.CONTENT_URI,
               LocationEntry.COLUMN_LOCATION_SETTING + "=?",
               new String []{TestLocationMinsk.TEST_LOCATION_SETTING});
        assert(numDeleted < 2);

        ContentValues newValues = TestLocationMinsk.getLocationForInsert();

        Uri newLocUri = mContext.getContentResolver().insert(
                LocationEntry.CONTENT_URI, newValues);
        long insertedId = ContentUris.parseId(newLocUri);
        assert(insertedId != 0L);
        //query it back
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(insertedId),null,null,null,null);
        assertTrue(cursor.moveToFirst());
        TestUtilities.validateCurrentRecord("Not getting location data back", cursor, newValues);

       numDeleted = mContext.getContentResolver().delete(
                LocationEntry.buildLocationUri(insertedId),null,null
        );
        assertEquals(1,numDeleted);
    }

    public void testInsert_Weather_Values_at_Test_Location_Can_Retrieve_Back_and_Delete(){
        //first delete all weather values at the test location (just in case any left-overs)
        mContext.getContentResolver().delete(
               WeatherEntry.buildWeatherLocation(TestLocationMinsk.TEST_LOCATION_SETTING),null,null);
        //then delete the test location (just in case any left-overs)
        mContext.getContentResolver().delete(LocationEntry.CONTENT_URI,
                LocationEntry.COLUMN_LOCATION_SETTING + "=?",
                new String [] {TestLocationMinsk.TEST_LOCATION_SETTING});

        //insert the test location
        ContentValues locationValues = TestLocationMinsk.getLocationForInsert();
        Uri insertedUri = mContext.getContentResolver().insert(
                LocationEntry.CONTENT_URI, locationValues);
        long locKey = ContentUris.parseId(insertedUri);
        assert(locKey > 0);
        //now insert weather entries for this location
        ContentValues weatherEntries [] = TestLocationMinsk.getTestWeatherEntries(locKey);

        long insertedWeatherEntryIds[] = new long[TestLocationMinsk.NUM_WEATHER_ENTRIES];
        for(int i = 0; i != TestLocationMinsk.NUM_WEATHER_ENTRIES; i++) {
            insertedUri = mContext.getContentResolver().insert(WeatherEntry.CONTENT_URI, weatherEntries[i]);
            insertedWeatherEntryIds[i] = ContentUris.parseId(insertedUri);
            assert(insertedWeatherEntryIds[i] != 0);
        }
        //Now retreive them back...
        Cursor cursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocation(TestLocationMinsk.TEST_LOCATION_SETTING),
                null,null,null,null
        );
        assertTrue(cursor.moveToFirst());
        assertEquals(TestLocationMinsk.NUM_WEATHER_ENTRIES,cursor.getCount());
        for(int i = 0; i != TestLocationMinsk.NUM_WEATHER_ENTRIES;i ++) {
            TestUtilities.validateCurrentRecord("", cursor, weatherEntries[i]);
            cursor.moveToNext();
        }
        //clean up
        mContext.getContentResolver().delete(LocationEntry.CONTENT_URI,
                LocationEntry.COLUMN_LOCATION_SETTING + "=?",
                new String [] {TestLocationMinsk.TEST_LOCATION_SETTING});
        mContext.getContentResolver().delete(
                WeatherEntry.buildWeatherLocation(TestLocationMinsk.TEST_LOCATION_SETTING),null,null);

    }

    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                WeatherProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: WeatherProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + WeatherContract.CONTENT_AUTHORITY,
                    providerInfo.authority, WeatherContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: WeatherProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }
}
