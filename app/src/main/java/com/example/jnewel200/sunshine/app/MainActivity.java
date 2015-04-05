package com.example.jnewel200.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    //private final String FORECASTFRAGMENT_TAG = "FFTAG";
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private String mLocation;
    private boolean mTwoPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLocation = Utility.getPreferredLocation(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(findViewById(R.id.weather_detail_container) != null){
            //meaning, the wide-screen layout main is being used, so the detail frag IS in there
            mTwoPane = true;
            if(savedInstanceState == null){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment())
                        .commit();
            }
        }else{
            mTwoPane = false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }
        if(id == R.id.action_map_location){
            startMapLocationIntent();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void startMapLocationIntent(){
        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String locQuery = prefs.getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default)
        );
        Uri locUri = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q",locQuery)
                .build();
        mapIntent.setData(locUri);
        if( mapIntent.resolveActivity(getPackageManager()) != null ){
            startActivity(mapIntent);
        } else {
            Log.d(LOG_TAG, "Couldn't call " + locQuery + ", no receiving apps installed!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation( this );
        // update the location in our second pane using the fragment manager
        if (location != null && !location.equals(mLocation)) {
            ForecastFragment ff =
                    (ForecastFragment)getSupportFragmentManager()
                            .findFragmentById(R.id.fragment_forecast);
            if ( null != ff ) {
                ff.onLocationChanged();
            }
            mLocation = location;
        }
    }
/**
     * A placeholder fragment containing a simple view.
     */

}
