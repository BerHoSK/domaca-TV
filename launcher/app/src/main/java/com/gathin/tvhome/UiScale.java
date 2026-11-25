package com.gathin.tvhome;

import android.content.Context;

/**
 * Domáca TV: veľkosť dlaždíc aplikácií (80 až 120 %), nastavená tlačidlom s lupou vpravo hore.
 * Mení len dlaždice a ikony; hodiny, pozdrav a tlačidlá hore ostávajú rovnaké. Uložené len v tomto TV.
 */
final class UiScale {

    static final float[] LEVELS = {0.8f, 0.9f, 1.0f, 1.1f, 1.2f};
    static final String PREFS = "domaca_tv";
    /** Minúty medzi fotkami na Domove aj v šetriči; minúty nečinnosti Domova pred šetričom (0 = vypnutý). */
    static final String KEY_PHOTO_MINUTES = "photo_minutes";
    static final String KEY_IDLE_MINUTES = "idle_minutes";
    static final int[] PHOTO_MINUTES = {1, 2, 5, 10};
    static final int[] IDLE_MINUTES = {0, 2, 5, 10, 15};

    static int get(Context ctx, String key, int fallback) {
        return prefs(ctx).getInt(key, fallback);
    }

    static void put(Context ctx, String key, int value) {
        prefs(ctx).edit().putInt(key, value).apply();
    }
    private static final String KEY = "tile_scale";
    private static final String KEY_ADJUSTING = "tile_scale_adjusting";
    private static final int DEFAULT = 2;

    static int level(Context ctx) {
        int i = prefs(ctx).getInt(KEY, DEFAULT);
        return i < 0 || i >= LEVELS.length ? DEFAULT : i;
    }

    static float factor(Context ctx) {
        return LEVELS[level(ctx)];
    }

    static String percent(Context ctx) {
        return Math.round(factor(ctx) * 100) + " %";
    }

    /** Posun o krok (-1 menšie, +1 väčšie); vráti true, ak sa veľkosť zmenila. */
    static boolean step(Context ctx, int delta) {
        int i = Math.max(0, Math.min(LEVELS.length - 1, level(ctx) + delta));
        if (i == level(ctx)) return false;
        prefs(ctx).edit().putInt(KEY, i).putBoolean(KEY_ADJUSTING, true).commit();
        return true;
    }

    /** Po prekreslení Domova pokračovať v úprave na tlačidle s lupou. */
    static boolean takeAdjusting(Context ctx) {
        boolean a = prefs(ctx).getBoolean(KEY_ADJUSTING, false);
        if (a) prefs(ctx).edit().putBoolean(KEY_ADJUSTING, false).apply();
        return a;
    }

    /** Domáca TV: priehľadnosť ikon v percentách (100, 85, 70, 50); označená ikona je vždy plná. */
    static final int[] ALPHAS = {100, 85, 70, 50};
    private static final String KEY_ALPHA = "tile_alpha";

    static int alphaPercent(Context ctx) {
        int a = prefs(ctx).getInt(KEY_ALPHA, 100);
        return a < 30 || a > 100 ? 100 : a;
    }

    static void setAlphaPercent(Context ctx, int percent) {
        prefs(ctx).edit().putInt(KEY_ALPHA, percent).apply();
    }

    private static android.content.SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private UiScale() {
    }
}
