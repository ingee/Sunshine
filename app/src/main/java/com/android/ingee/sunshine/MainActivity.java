package com.android.ingee.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.android.ingee.sunshine.data.WeatherContract;

public class MainActivity
        extends AppCompatActivity
        implements android.app.LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int FORECAST_LOADER = 0;
    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LOG = 8;

    ForecastAdapter mForecastAdapter;

    private void updateWeather() {
        FetchWeatherTask weatherTask = new FetchWeatherTask(this);
        String location = Utility.getPreferredLocation(this);
        weatherTask.execute(location);
    }

    private void openPerferredLocationInMap() {
        String location = Utility.getPreferredLocation(this);

        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
        Uri mapLocation = Uri.parse("geo:0.0?").buildUpon()
                .appendQueryParameter("q", location)
                .build();
        mapIntent.setData(mapLocation);

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Log.d(LOG_TAG, "Couldn't call " + location + ", no receiving apps installed!");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mForecastAdapter = new ForecastAdapter(this, null, 0);
        ListView vw = (ListView) findViewById(R.id.listview_forecast);
        vw.setAdapter(mForecastAdapter);

        getLoaderManager().initLoader(FORECAST_LOADER, null, this);

        Log.v(getClass().getSimpleName(), "MainActivity created~~~");
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_map) {
            openPerferredLocationInMap();
            return true;
        }
        if (id == R.id.action_test) {
            Log.v("MainActivity", "Test Menu Selected~~~");

            //jus test, don't mind {{{

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(
                    getApplicationContext());
            String value = pref.getString(getString(R.string.pref_units_key), "none");
            Toast toast = Toast.makeText(getApplicationContext(), value, Toast.LENGTH_SHORT);
            toast.show();

            //}}}
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(this);
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());
        return new CursorLoader(this,
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }
}
