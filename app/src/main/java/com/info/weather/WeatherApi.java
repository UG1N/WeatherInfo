package com.info.weather;

import android.content.Context;

import java.net.URLEncoder;

public class WeatherApi {

    public static CoordinatesBuilder coordinatesBuilder() {
        return new CoordinatesBuilder();
    }

    public static LocationBuilder locationBuilder() {
        return new LocationBuilder();
    }

    public static IconBuilder iconResourceBuilder(Context context) {
        return new IconBuilder(context);
    }

    public static class CoordinatesBuilder {

        private double mLatitude;
        private double mLongitude;

        public CoordinatesBuilder latitude(double latitude) {
            mLatitude = latitude;
            return this;
        }

        public CoordinatesBuilder longitude(double longitude) {
            mLongitude = longitude;
            return this;
        }

        public String build() {
            return new StringBuilder()
                    .append("http://api.openweathermap.org/data/2.5/weather?lat=")
                    .append(mLatitude)
                    .append("&lon=")
                    .append(mLongitude)
                    .append("&units=metric&appid=c7b3b5a86a23d0e8d4b862787f675958")
                    .toString();
        }
    }

    public static class LocationBuilder {
        private String mLocationName;
        private Units mUnits;

        public enum Units {

            IMPERIAL("imperial"),
            METRIC("metric"),
            STANDART("standart");

            String mParamValue;

            Units(String paramValue) {
                mParamValue = paramValue;
            }

            public String getParamValue() {
                return mParamValue;
            }
        }

        public LocationBuilder location(CharSequence name) {
            mLocationName = URLEncoder.encode(name.toString());
            return this;
        }

        public LocationBuilder untis(Units units) {
            mUnits = units;
            return this;
        }

        public String build() {
            return new StringBuilder()
                    .append("http://api.openweathermap.org/data/2.5/weather?q=")
                    .append(mLocationName)
                    .append("&units=")
                    .append(mUnits.getParamValue())
                    .append("&appid=c7b3b5a86a23d0e8d4b862787f675958")
                    .toString();
        }

    }

    public static class IconBuilder {
        private Context mContext;
        private Weather mWeather;

        public IconBuilder(Context context) {
            mContext = context;
        }

        public IconBuilder weather(Weather weather) {
            mWeather = weather;
            return this;
        }

        public int build() {
            return mContext.getResources()
                    .getIdentifier(mWeather.getIcon(), "drawable", mContext.getPackageName());
        }
    }
}
