package com.android.ingee.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ArrayAdapter<String> mForecastAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] data = {
                "Today - Sunny - 88/63",
                "Tomorrow - Foggy - 70/40",
                "Weds - Cloudy - 72/63",
                "Thurs - Asteroids - 75/65",
                "Fri - Heavy Rain - 65/56",
                "Sat - HELP TRAPPED IN WEATHERSTATION - 60/51",
                "Sun - Sunny - 80/68"
        };
        List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));

        mForecastAdapter = new ArrayAdapter<String>(
                this,
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast);
        ListView vw = (ListView) findViewById(R.id.listview_forecast);
        vw.setAdapter(mForecastAdapter);
        Log.e("MainActivity", "ingee,ingee,ingee~~~");
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
            FetchWeatherTask weatherTask = new FetchWeatherTask();
            weatherTask.execute("Seongnam,KR");
            return true;
        }
        if (id == R.id.action_test) {
            Log.v("MainActivity", "Test Menu Selected~~~");

            //test {{{

            try {
                JSONObject jsonObj = new JSONObject("{\"city\":{\"id\":5375480,\"name\":\"Mountain View\",\"coord\":{\"lon\":-122.083847,\"lat\":37.386051},\"country\":\"US\",\"population\":0},\"cod\":\"200\",\"message\":0.0319,\"cnt\":7,\"list\":[{\"dt\":1468094400,\"temp\":{\"day\":24.65,\"min\":9.39,\"max\":24.65,\"night\":9.39,\"eve\":20.05,\"morn\":24.65},\"pressure\":993.53,\"humidity\":70,\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"clear sky\",\"icon\":\"01n\"}],\"speed\":2.41,\"deg\":294,\"clouds\":0},{\"dt\":1468180800,\"temp\":{\"day\":21.42,\"min\":8.14,\"max\":21.42,\"night\":8.14,\"eve\":16.7,\"morn\":12.08},\"pressure\":992.72,\"humidity\":65,\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"clear sky\",\"icon\":\"01d\"}],\"speed\":2.15,\"deg\":297,\"clouds\":0},{\"dt\":1468267200,\"temp\":{\"day\":23.92,\"min\":8.99,\"max\":24.22,\"night\":8.99,\"eve\":19.82,\"morn\":11.63},\"pressure\":989.02,\"humidity\":67,\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"clear sky\",\"icon\":\"01d\"}],\"speed\":1.36,\"deg\":264,\"clouds\":0},{\"dt\":1468353600,\"temp\":{\"day\":18.69,\"min\":11.3,\"max\":21.58,\"night\":14.32,\"eve\":21.58,\"morn\":11.3},\"pressure\":1008.8,\"humidity\":0,\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"clear sky\",\"icon\":\"01d\"}],\"speed\":2.52,\"deg\":296,\"clouds\":0},{\"dt\":1468440000,\"temp\":{\"day\":17.65,\"min\":10.59,\"max\":20.72,\"night\":13.4,\"eve\":20.72,\"morn\":10.59},\"pressure\":1010.96,\"humidity\":0,\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"clear sky\",\"icon\":\"01d\"}],\"speed\":1.95,\"deg\":283,\"clouds\":11},{\"dt\":1468526400,\"temp\":{\"day\":18.05,\"min\":10.7,\"max\":20.86,\"night\":13.84,\"eve\":20.86,\"morn\":10.7},\"pressure\":1008.86,\"humidity\":0,\"weather\":[{\"id\":500,\"main\":\"Rain\",\"description\":\"light rain\",\"icon\":\"10d\"}],\"speed\":1.95,\"deg\":287,\"clouds\":11},{\"dt\":1468612800,\"temp\":{\"day\":17.53,\"min\":11.46,\"max\":20.29,\"night\":13.95,\"eve\":20.29,\"morn\":11.46},\"pressure\":1007.54,\"humidity\":0,\"weather\":[{\"id\":500,\"main\":\"Rain\",\"description\":\"light rain\",\"icon\":\"10d\"}],\"speed\":1.69,\"deg\":257,\"clouds\":15}]}");
                JSONArray listArr = jsonObj.getJSONArray("list");
                JSONObject firstObj = listArr.getJSONObject(0);
                JSONObject tempObj = firstObj.getJSONObject("temp");
                double maxTemp = tempObj.getDouble("max");
                Log.v("Test Code", "max temp = " + maxTemp);
            }
            catch (Exception e) {
                Log.v("Test Code", "something wrong~~~");
            }

            //}}}
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, Void> {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected Void doInBackground(String... params) {
            //params should exist, it should be city-name
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            String format = "json";
            String units = "metric";
            int numDays = 7;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                //
                // API ex)
                // http://api.openweathermap.org/data/2.5/forecast/daily?id=1897000&mode=json&units=metric&cnt=7
                // http://api.openweathermap.org/data/2.5/forecast/daily?q=seongnam,KR&mode=json&units=metric&cnt=7&appid=01fd0d2baf46be09e48b6e50691f8fb0
                //
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "appid";
                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                                .appendQueryParameter(QUERY_PARAM, params[0])
                                .appendQueryParameter(FORMAT_PARAM, format)
                                .appendQueryParameter(UNITS_PARAM, units)
                                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                                .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                                .build();

                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "Built URI = " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                forecastJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
                Log.v(LOG_TAG, forecastJsonStr);
            }
            return null;
        }
    }
}
