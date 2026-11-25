package io.github.sumyabazar1.weather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Domáca TV: launcher sa pýta na počasie pre riadok vpravo hore. Odpovieme z pamäte, ak je čítanie
 * mladšie ako 30 minút, inak raz načítame Open-Meteo pre uložené mesto. Bez mesta neodpovieme nič.
 */
public final class HomeWeatherReceiver extends BroadcastReceiver {

    static final String ACTION_STATE = "sk.berho.domacatv.weather.STATE";
    static final String PERMISSION = "sk.berho.domacatv.permission.RADIO_STATE";
    static final String LAUNCHER = "com.gathin.tvhome";
    private static final long FRESH_MS = 30 * 60_000L;

    @Override
    public void onReceive(Context context, Intent intent) {
        final Context app = context.getApplicationContext();
        final PendingResult pending = goAsync();
        new Thread(() -> {
            try {
                reply(app);
            } catch (Exception ignored) {
                // Bez siete alebo bez mesta: riadok na Domove ostane, ako bol.
            } finally {
                pending.finish();
            }
        }, "home-weather").start();
    }

    private static void reply(Context ctx) throws Exception {
        SharedPreferences place = ctx.getSharedPreferences("place", Context.MODE_PRIVATE);
        String name = place.getString("name", "");
        if (name.isEmpty()) return;
        SharedPreferences cache = ctx.getSharedPreferences("home_weather", Context.MODE_PRIVATE);
        long now = System.currentTimeMillis();
        if (now - cache.getLong("time", 0) > FRESH_MS || !name.equals(cache.getString("place", ""))) {
            double lat = Double.longBitsToDouble(place.getLong("lat", 0));
            double lon = Double.longBitsToDouble(place.getLong("lon", 0));
            Forecast.Report r = Forecast.fetch(lat, lon, 1);
            cache.edit()
                    .putLong("time", now)
                    .putString("place", name)
                    .putInt("temp", Forecast.degrees(r.tempC, false))
                    .putInt("code", r.code)
                    .putBoolean("day", r.day)
                    .apply();
        }
        String sky = ctx.getString(Forecast.sky(cache.getInt("code", 0), cache.getBoolean("day", true)));
        Intent out = new Intent(ACTION_STATE).setPackage(LAUNCHER)
                .putExtra("temp", cache.getInt("temp", 0))
                .putExtra("sky", sky)
                .putExtra("code", cache.getInt("code", 0))
                .putExtra("place", name);
        ctx.sendBroadcast(out, PERMISSION);
    }
}
