package com.example.jnewel200.sunshine.app.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.test.AndroidTestCase;

/**
 * Created by jnewel200 on 3/16/2015.
 */
public class WeatherDBHelperTest extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testThat_DB_Exists(){
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        assertNotNull(db);

    }

}
