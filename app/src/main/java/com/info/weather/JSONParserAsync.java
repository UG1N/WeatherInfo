package com.info.weather;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class JSONParserAsync extends AsyncTask<String, Void, Weather> {

    protected Exception mException;

    @Override
    protected Weather doInBackground(String... params) {
        String urlString = params[0];
        try {
            String jsonWeatherData = IoUtils.getHttpData(urlString);
            return parseWeatherJson(jsonWeatherData);

        } catch (Exception e) {
            mException = e;
        }
        return null;
    }

    private Weather parseWeatherJson(String jsonData) throws JSONException {
        Weather result = new Weather();
        JSONObject reader = new JSONObject(jsonData);
        String city = reader.getString("name");

        JSONObject sys = reader.getJSONObject("sys");
        String country = sys.getString("country");
        result.setCity(city + ", " + country);

        JSONObject temperature = reader.getJSONObject("main");
        float celsius = Float.parseFloat(temperature.getString("temp"));
        result.setTemperature((int) celsius);

        JSONArray weather = reader.getJSONArray("weather");
        JSONObject weather_object = weather.getJSONObject(0);
        String weatherIcon = weather_object.getString("icon");
        result.setIcon("ic_" + weatherIcon);

        JSONObject coord = reader.getJSONObject("coord");
        result.setLatitude((float) coord.getDouble("lat"));
        result.setLongitude((float) coord.getDouble("lon"));

        return result;
    }
}
