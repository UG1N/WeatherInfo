package com.info.weather;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.info.weather.WeatherApi.LocationBuilder.Units.IMPERIAL;
import static com.info.weather.WeatherApi.LocationBuilder.Units.METRIC;

public class WeatherInfoActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = WeatherInfoActivity.class.getSimpleName();

    protected GoogleApiClient mGoogleApiClient;

    private Button mStartWeatherButton;
    private EditText mCityEditText;
    private TextView mCityNameTextView;
    private TextView mCityTemperatureTextView;
    private ImageView mWeatherIcon;
    private boolean weatherFormat = false;
    private Weather mWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_info);
        mWeather = new Weather();

        mCityEditText = (EditText) findViewById(R.id.search_name);
        mWeatherIcon = (ImageView) findViewById(R.id.weather_icon);
        mStartWeatherButton = (Button) findViewById(R.id.show_me_the_weather);
        mStartWeatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTheWeather(WeatherApi
                        .locationBuilder()
                        .location(mCityEditText.getText())
                        .untis(weatherFormat ? IMPERIAL : METRIC)
                        .build());
            }
        });

        mCityNameTextView = (TextView) findViewById(R.id.city_name);
        mCityTemperatureTextView = (TextView) findViewById(R.id.temperature);
        mCityTemperatureTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weatherFormat = !weatherFormat;
                showTheWeather(WeatherApi
                        .locationBuilder()
                        .location(mCityEditText.getText())
                        .untis(weatherFormat ? IMPERIAL : METRIC)
                        .build());
            }
        });

        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    private void showTheWeather(final String url) {
        new JSONParserAsync() {
            @Override
            protected void onPostExecute(Weather weather) {
                if (weather != null) {
                    mWeather = weather;
                    updateView();
                } else {
                    Log.e(TAG, "Can't get weather data from url: " + url, mException);
                }
            }
        }.execute(url);
    }

    public void updateView() {
        mCityNameTextView.setText(mWeather.getCity());
        mCityTemperatureTextView.setText(getString(
                R.string.temperature_format, mWeather.getTemperature()));
        mWeatherIcon.setImageResource(WeatherApi.iconResourceBuilder(this).weather(mWeather).build());
    }

    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this, "onConnected", Toast.LENGTH_SHORT).show();

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            return;
        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            return;
        }

        String requestUrl = WeatherApi.coordinatesBuilder()
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .build();
        showTheWeather(requestUrl);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mWeather = WeatherStorage.loadSavedWeather(this);
        updateView();
        Toast.makeText(this, "please, turn on internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        WeatherStorage.saveTheWeather(this, mWeather);
        mGoogleApiClient.disconnect();
    }
}
