package com.info.weather;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;

public class WeatherInfoActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;

    private Button mStartWeatherButton;
    private EditText mCityEditText;
    private TextView mCityNameTextView;
    private TextView mCityTemperatureTextView;
    private ImageView mWeatherIcon;
    private boolean weatherFormat = false;
    private Weather mWeather;

    private final String imageUrl = "http://openweathermap.org/img/w/";


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
                showTheWeather(getFormatUrl());
            }
        });

        mCityNameTextView = (TextView) findViewById(R.id.city_name);
        mCityTemperatureTextView = (TextView) findViewById(R.id.temperature);
        mCityTemperatureTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weatherFormat = !weatherFormat;
                showTheWeather(getFormatUrl());
            }
        });

        buildGoogleApiClient();
    }

    private void showTheWeather(String url) {
        new JSONParserAsync(this, mWeather).execute(url);
    }

    public void updateView() {
        mCityNameTextView.setText(mWeather.getCity());
        mCityTemperatureTextView.setText(String.valueOf(mWeather.getTemperature()) + "°");
        Picasso.with(this).
                load(imageUrl + mWeather.getIcon() + ".png").
                into(mWeatherIcon);
    }

    public String getFormatUrl() {
        String format;
        if (!weatherFormat) {
            format = "&units=metric";
        } else {
            format = "";
        }
        return "http://api.openweathermap.org/data/2.5/weather?q=" +
                mCityEditText.getText().toString().replaceAll(" ", "") +
                format + "&appid=2de143494c0b295cca9337e1e96b00e0";
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
        String firstLocation = "";

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            firstLocation = String.valueOf(mLastLocation.getLatitude() + "&lon=" + mLastLocation.getLongitude()).replaceAll(",", ".");
        }
        String firstUrl = "http://api.openweathermap.org/data/2.5/weather?lat=" +
                firstLocation +
                "&units=metric&appid=2de143494c0b295cca9337e1e96b00e0";
        showTheWeather(firstUrl);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "please, turn on internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }
}
