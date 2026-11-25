package com.gathin.tvhome;

import android.service.dreams.DreamService;
import android.view.View;
import android.widget.TextView;

/**
 * Screensaver: the launcher's thin clock and date on black, over a slow ambient glow (ClockScene).
 * No network, no images: cheap enough for a 1 GB TV.
 */
public class ClockDream extends DreamService {

    private ClockScene scene;
    private NowPlaying nowPlaying;
    private TextView radioLine;

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LauncherLocale.wrap(base));   // the theme's locale, as ClockActivity
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(false);   // any key wakes the TV back to where it was
        setFullscreen(true);
        setScreenBright(false);
        setContentView(R.layout.dream_clock);
        ClockFormat.apply(getWindow().getDecorView(), Config.load(this).clockFormat);
        scene = new ClockScene(getWindow().getDecorView());
        scene.useHomeWallpaper(this);
        // Domáca TV: meniny pod dátumom a hrajúca stanica.
        TextView nameDay = getWindow().getDecorView().findViewById(R.id.dreamNameDay);
        String names = getResources().getBoolean(R.bool.name_day_sk) ? NameDays.today() : "";
        if (!names.isEmpty()) {
            nameDay.setText(getString(R.string.name_day, names));
            nameDay.setVisibility(View.VISIBLE);
        }
        radioLine = getWindow().getDecorView().findViewById(R.id.dreamRadio);
        nowPlaying = new NowPlaying(this, this::showStation);
        nowPlaying.start();
    }

    private void showStation() {
        String station = nowPlaying != null ? nowPlaying.station() : null;
        if (radioLine == null) return;
        radioLine.setText(station == null ? "" : getString(R.string.dream_now_playing, station));
        radioLine.setVisibility(station == null ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();
        scene.start();
    }

    @Override
    public void onDreamingStopped() {
        scene.stop();
        super.onDreamingStopped();
    }

    @Override
    public void onDetachedFromWindow() {
        if (scene != null) scene.stop();
        if (nowPlaying != null) nowPlaying.stop();
        super.onDetachedFromWindow();
    }
}
