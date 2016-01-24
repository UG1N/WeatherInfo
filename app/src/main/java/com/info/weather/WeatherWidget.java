package com.info.weather;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.RemoteViews;
import android.widget.Toast;

public class WeatherWidget extends AppWidgetProvider {

    public static final String SEND_ITEM = "LOCATION_CHANGED";
    public static final String EXTRA_WEATHER = "Weather";

    private static Weather mWeather;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        PendingIntent pendingIntent;

        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
        if (mWeather == null) {
            Intent newIntent = new Intent(context, WeatherInfoActivity.class);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            pendingIntent = PendingIntent.getActivity(context, 0, newIntent, 0);
            views.setImageViewResource(R.id.widget_icon, R.drawable.weather_preview);
            views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        } else {
            views.setImageViewResource(R.id.widget_icon,
                    WeatherApi.iconResourceBuilder(context).weather(mWeather).build());
            views.setTextViewText(R.id.widget_city, mWeather.getCity());
            views.setTextViewText(R.id.widget_temperature,
                    context.getString(R.string.temperature_format, mWeather.getTemperature()));
            Intent intent = new Intent(context, WeatherWidget.class);
            intent.putExtra(EXTRA_WEATHER, mWeather);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, AppWidgetManager.getInstance(context)
                    .getAppWidgetIds(new ComponentName(context.getApplicationContext(), WeatherWidget.class)));
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        super.onReceive(context, intent);

        if (SEND_ITEM.equals(intent.getAction())) {
            weatherUpdate(context, intent);
        } else if (mWeather != null) {
            if (((ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE))
                    .getActiveNetworkInfo().isConnectedOrConnecting()) {
                new JSONParserAsync() {
                    @Override
                    protected void onPostExecute(Weather weather) {
                        Toast.makeText(context, "updated", Toast.LENGTH_SHORT).show();
                        mWeather = weather;
                        onUpdate(context, AppWidgetManager.getInstance(context),
                                intent.getExtras().getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS));
                    }
                }.execute(WeatherApi.locationBuilder()
                        .location(mWeather.getCity())
                        .untis(WeatherApi.LocationBuilder.Units.METRIC)
                        .build());
            } else {
                weatherUpdate(context, intent);
            }
        }
    }

    private void weatherUpdate(Context context, Intent intent) {
        mWeather = (Weather) intent.getSerializableExtra(EXTRA_WEATHER);
        onUpdate(context, AppWidgetManager.getInstance(context),
                intent.getExtras().getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS));
    }
}

