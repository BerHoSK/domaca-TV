package com.gathin.tvhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

/**
 * Domáca TV: čo práve hrá doplnok Rádio (sk.berho.domacatv.radio).
 * Rádio posiela stav ako broadcast chránený podpisovým oprávnením, takže ho prijmeme len od aplikácie
 * podpísanej rovnakým kľúčom. Pri registrácii sa rádia opýtame na aktuálny stav.
 */
final class NowPlaying {

    static final String ACTION_STATE = "sk.berho.domacatv.radio.STATE";
    static final String ACTION_QUERY = "sk.berho.domacatv.radio.QUERY";
    static final String PERMISSION = "sk.berho.domacatv.permission.RADIO_STATE";
    static final String RADIO_PACKAGE = "sk.berho.domacatv.radio";

    /** Názov hrajúcej stanice, alebo null, keď nič nehrá. */
    private String station;
    private final Context ctx;
    private final Runnable onChange;
    private boolean registered;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            String name = i.getBooleanExtra("playing", false) ? i.getStringExtra("name") : null;
            if (name != null && name.trim().isEmpty()) name = null;
            boolean changed = name == null ? station != null : !name.equals(station);
            station = name;
            if (changed) onChange.run();
        }
    };

    NowPlaying(Context ctx, Runnable onChange) {
        this.ctx = ctx;
        this.onChange = onChange;
    }

    String station() {
        return station;
    }

    void start() {
        if (registered) return;
        try {
            IntentFilter filter = new IntentFilter(ACTION_STATE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Posiela ho iná aplikácia (Rádio), preto exported; chráni ho podpisové oprávnenie.
                ctx.registerReceiver(receiver, filter, PERMISSION, null, Context.RECEIVER_EXPORTED);
            } else {
                ctx.registerReceiver(receiver, filter, PERMISSION, null);
            }
            registered = true;
            ctx.sendBroadcast(new Intent(ACTION_QUERY).setPackage(RADIO_PACKAGE), PERMISSION);
        } catch (RuntimeException ignored) {
            // Bez rádia alebo bez oprávnenia: riadok sa jednoducho nezobrazí.
        }
    }

    void stop() {
        if (!registered) return;
        registered = false;
        try {
            ctx.unregisterReceiver(receiver);
        } catch (RuntimeException ignored) {
        }
    }
}
