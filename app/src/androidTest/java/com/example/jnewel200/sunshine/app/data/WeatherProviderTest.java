package com.example.jnewel200.sunshine.app.data;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
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

    public void testDirectProviderQuery(){
        Time time = new Time();
        time.setToNow();
        int testDate = Time.getJulianDay(time.toMillis(false),time.gmtoff);
        WeatherProvider provider = new WeatherProvider();
        Cursor cursor = provider.query(
            WeatherEntry.CONTENT_URI.buildUpon().
                    appendQueryParameter(WeatherEntry.COLUMN_DATE, Integer.toString(testDate)).build(),
                    null,
                    null,
                    null,
                    null);
        assertTrue("Cursor has no data, but is should",cursor.moveToFirst());


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
