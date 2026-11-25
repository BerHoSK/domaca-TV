package com.gathin.tvhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

/**
 * Domáca TV: počasie z doplnku Počasie (io.github.sumyabazar1.weather) pre riadok vpravo hore.
 * Launcher sám nemá internet: pýta sa doplnku, ktorý číta Open-Meteo najviac raz za 30 minút.
 * Odpoveď chráni rovnaké podpisové oprávnenie ako pri Rádiu.
 */
final class NowWeather {

    static final String ACTION_STATE = "sk.berho.domacatv.weather.STATE";
    static final String ACTION_QUERY = "sk.berho.domacatv.weather.QUERY";
    static final String WEATHER_PACKAGE = "io.github.sumyabazar1.weather";
    private static final long REFRESH_MS = 30 * 60_000L;

    /** Posledný riadok, napr. "☁ 4°  Zamračené", alebo null, kým nič neprišlo. */
    private String line;
    private final Context ctx;
    private final Runnable onChange;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean registered;

    private final Runnable ask = new Runnable() {
        @Override
        public void run() {
            try {
                ctx.sendBroadcast(new Intent(ACTION_QUERY).setPackage(WEATHER_PACKAGE), NowPlaying.PERMISSION);
            } catch (RuntimeException ignored) {
            }
            handler.postDelayed(this, REFRESH_MS);
        }
    };

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            String sky = i.getStringExtra("sky");
            String next = symbol(i.getIntExtra("code", 3), sky) + " " + i.getIntExtra("temp", 0) + "°"
                    + (sky == null || sky.isEmpty() ? "" : "  " + sky);
            if (!next.equals(line)) {
                line = next;
                onChange.run();
            }
        }
    };

    NowWeather(Context ctx, Runnable onChange) {
        this.ctx = ctx;
        this.onChange = onChange;
    }

    String line() {
        return line;
    }

    void start() {
        if (registered) return;
        try {
            IntentFilter filter = new IntentFilter(ACTION_STATE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ctx.registerReceiver(receiver, filter, NowPlaying.PERMISSION, null, Context.RECEIVER_EXPORTED);
            } else {
                ctx.registerReceiver(receiver, filter, NowPlaying.PERMISSION, null);
            }
            registered = true;
            handler.post(ask);
        } catch (RuntimeException ignored) {
        }
    }

    void stop() {
        handler.removeCallbacks(ask);
        if (!registered) return;
        registered = false;
        try {
            ctx.unregisterReceiver(receiver);
        } catch (RuntimeException ignored) {
        }
    }

    /** Jednoduchý symbol podľa kódu WMO; symboly sú v bežnom systémovom písme. */
    static String symbol(int code, String sky) {
        if (code <= 1) return "☀";
        if (code <= 48) return "☁";
        if (code >= 95) return "⚡";
        if ((code >= 71 && code <= 77) || code == 85 || code == 86) return "❄";
        return "☂";
    }
}
