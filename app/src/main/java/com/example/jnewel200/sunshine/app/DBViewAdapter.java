package com.example.jnewel200.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.jnewel200.sunshine.app.data.WeatherContract;
import com.example.jnewel200.sunshine.app.DBActivity.TABLE_VIEWING;

import org.w3c.dom.Text;

/**
 * Created by jnewel200 on 4/2/2015.
 */
public class DBViewAdapter extends CursorAdapter{

    //public enum TABLE_VEWING {LOCATION, WEATHER};
    public DBViewAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //get view widgets to populate
        TextView tv = (TextView)view.findViewById(R.id.list_item_dbviewrow_textview);
        //TODO: find out what data-view Mode we are in
        TABLE_VIEWING theViewMode = TABLE_VIEWING.LOCATION;
        if(cursor.getPosition() == 0){
            //build the header first
            String hdr = buildHeaderRow(theViewMode);
            //TODO: Don't know what to do with this, how to force a header view...
        }
        tv.setText(buildCurrentDataRow(cursor,theViewMode));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_dbview_row, parent, false);
    }

    private String buildCurrentDataRow(Cursor cursor, TABLE_VIEWING currentViewMode){
        if(cursor == null) return null;
        String formattedRow;
        switch (currentViewMode){
            case LOCATION: {
                int _idIdx = cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
                int cityNameIdx = cursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_CITY_NAME);
                int locQueryIdx = cursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);
                long _id = cursor.getLong(_idIdx);
                String cityName = cursor.getString(cityNameIdx);
                String locationQuery = cursor.getString(locQueryIdx);
                formattedRow = String.format("%d\t | %s\t| %s",_id, cityName,locationQuery);
            }
            break;
            case WEATHER:{
                //get cursor column indexes, then grab the vals
                //TODO: investigate this...it's seems like a bug in the cursor ?
                int _idIdx = 0;//cursor.getColumnIndex(WeatherContract.WeatherEntry._ID);
                int dateIdx = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
                int descIdx = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);
                int maxTempIdx = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
                int minTempIdx = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP);
                int idLocIdx = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_LOC_KEY);
                int humidityIdx = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_HUMIDITY);
                int weatherConditionIdIdx = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID);
                //get values and prepare the output string...
                long _id = cursor.getLong(_idIdx);
                long date = cursor.getLong(dateIdx);
                String dateString = Utility.formatDate(date);
                String desc = cursor.getString(descIdx);
                double maxTemp = cursor.getDouble(maxTempIdx);
                double minTemp = cursor.getDouble(minTempIdx);
                int locationId = cursor.getInt(idLocIdx);
                double humidity = cursor.getDouble(humidityIdx);
                int weatherConditionId = cursor.getInt(weatherConditionIdIdx);
                formattedRow =
                        String.format("%d\t | %d\t| %s\t| %s\t | %f\t | %f\t | %f\t | %d\t | %d",
                                _id, date, dateString, desc, maxTemp, minTemp, humidity, weatherConditionId, locationId);
            }
            break;
            default:
                formattedRow = null;
        }
        return formattedRow;
    }
    private String buildHeaderRow(TABLE_VIEWING currentViewMode){
        switch(currentViewMode){
            case LOCATION:
                return WeatherContract.LocationEntry._ID + "\t| " +
                        WeatherContract.LocationEntry.COLUMN_CITY_NAME + "\t| " +
                        WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING;
            case WEATHER:
                return WeatherContract.WeatherEntry._ID + "\t| " +
                        WeatherContract.WeatherEntry.COLUMN_DATE + "\t| " +
                        " formatted-date\t| " +
                        WeatherContract.WeatherEntry.COLUMN_SHORT_DESC + "\t|" +
                        WeatherContract.WeatherEntry.COLUMN_MAX_TEMP + "\t|" +
                        WeatherContract.WeatherEntry.COLUMN_MIN_TEMP + "\t|" +
                        WeatherContract.WeatherEntry.COLUMN_HUMIDITY +  "\t|" +
                        WeatherContract.WeatherEntry.COLUMN_WEATHER_ID +  "\t|" +
                        WeatherContract.WeatherEntry.COLUMN_LOC_KEY;
            default:
                return null;
        }

    }
}
