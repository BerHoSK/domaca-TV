package com.gathin.tvhome;

import android.content.Context;
import android.content.res.Configuration;

/**
 * Domáca TV: veľkosť celej domovskej obrazovky jedným tlačidlom vpravo hore (A / AA / AAA).
 * Mení hustotu (dpi) kontextu Domova, takže sa naraz zmenšia alebo zväčšia dlaždice, ikony aj texty.
 * Voľba je uložená len v tomto TV.
 */
final class UiScale {

    static final float[] LEVELS = {0.85f, 1.0f, 1.15f};
    static final String[] LABELS = {"A", "AA", "AAA"};
    private static final String PREFS = "domaca_tv";
    private static final String KEY = "ui_scale";
    private static final int DEFAULT = 1;

    static int level(Context ctx) {
        int i = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY, DEFAULT);
        return i < 0 || i >= LEVELS.length ? DEFAULT : i;
    }

    static String label(Context ctx) {
        return LABELS[level(ctx)];
    }

    /** Ďalšia veľkosť v poradí A → AA → AAA → A; uloží ju. */
    static void next(Context ctx) {
        int i = (level(ctx) + 1) % LEVELS.length;
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY, i).commit();
    }

    /** base s upravenou hustotou, alebo base, keď je zvolená normálna veľkosť. */
    static Context wrap(Context base) {
        float s;
        try {
            s = LEVELS[level(base)];
        } catch (RuntimeException e) {
            return base;
        }
        if (s == 1.0f) return base;
        Configuration c = new Configuration(base.getResources().getConfiguration());
        c.densityDpi = Math.round(c.densityDpi * s);
        return base.createConfigurationContext(c);
    }

    private UiScale() {
    }
}
