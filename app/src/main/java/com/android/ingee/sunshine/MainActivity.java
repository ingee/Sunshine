package com.android.ingee.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.ingee.sunshine.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
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

        String locationSetting = Utility.getPreferredLocation(this);
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());
        Cursor cur = this.getContentResolver().query(weatherForLocationUri, null, null, null, sortOrder);

        mForecastAdapter = new ForecastAdapter(this, cur, 0);
        ListView vw = (ListView) findViewById(R.id.listview_forecast);
        vw.setAdapter(mForecastAdapter);

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
}
