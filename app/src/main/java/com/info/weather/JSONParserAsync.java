package com.info.weather;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class JSONParserAsync extends AsyncTask<String, Void, String> {

    private Weather mWeather;
    private WeatherInfoActivity mActivity;

    public JSONParserAsync(WeatherInfoActivity activity, Weather weather) {
        mWeather = weather;
        mActivity = activity;
    }

    @Override
    protected String doInBackground(String... params) {
        String dataUrl = null;
        String urlString = params[0];

        DataHandler dataHandler = new DataHandler();
        dataUrl = dataHandler.GetHTTPData(urlString);
        return dataUrl;
    }

    @Override
    protected void onPostExecute(String s) {
        if (s != null) {
            try {
                JSONObject reader = new JSONObject(s);
                String city = reader.getString("name");

                JSONObject sys = reader.getJSONObject("sys");
                String country = sys.getString("country");
                mWeather.setCity(city + ", " + country);

                JSONObject temperature = reader.getJSONObject("main");
                float celsius = Float.parseFloat(temperature.getString("temp"));
                mWeather.setTemperature((int) celsius);

                JSONArray weather = reader.getJSONArray("weather");
                JSONObject weather_object = weather.getJSONObject(0);
                String weatherIcon = weather_object.getString("icon");
                mWeather.setIcon(weatherIcon);

                JSONObject coord = reader.getJSONObject("coord");
                mWeather.setLatitude((float) coord.getDouble("lat"));
                mWeather.setLongitude((float) coord.getDouble("lon"));

                mActivity.updateView();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
