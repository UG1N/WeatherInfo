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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
    public static final String FILENAME = "weather";


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
        mGoogleApiClient.connect();
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
        loadSavedWeather();
        Toast.makeText(this, "please, turn on internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveTheWeather();
        mGoogleApiClient.disconnect();
    }

    private void saveTheWeather() {
        FileOutputStream fos = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            fos = openFileOutput(FILENAME, MODE_PRIVATE);
            objectOutputStream = new ObjectOutputStream(fos);
            objectOutputStream.writeObject(mWeather);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (objectOutputStream != null) {
                    objectOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadSavedWeather() {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = openFileInput(FILENAME);
            ois = new ObjectInputStream(fis);
            mWeather = (Weather) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            updateView();
        }
    }
}
