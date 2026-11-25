package com.gathin.tvhome;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

/** Domáca TV: meniny a hrajúca stanica v šetriči (ClockDream aj ClockActivity, rovnaké rozloženie dream_clock). */
final class DreamExtras {

    private final Context ctx;
    private final TextView radioLine;
    private final NowPlaying nowPlaying;

    DreamExtras(Context ctx, View root) {
        this.ctx = ctx;
        TextView nameDay = root.findViewById(R.id.dreamNameDay);
        String names = ctx.getResources().getBoolean(R.bool.name_day_sk) ? NameDays.today() : "";
        if (nameDay != null && !names.isEmpty()) {
            nameDay.setText(ctx.getString(R.string.name_day, names));
            nameDay.setVisibility(View.VISIBLE);
        }
        radioLine = root.findViewById(R.id.dreamRadio);
        nowPlaying = new NowPlaying(ctx, this::showStation);
    }

    void start() {
        nowPlaying.start();
    }

    void stop() {
        nowPlaying.stop();
    }

    private void showStation() {
        if (radioLine == null) return;
        String station = nowPlaying.station();
        radioLine.setText(station == null ? "" : ctx.getString(R.string.dream_now_playing, station));
        radioLine.setVisibility(station == null ? View.GONE : View.VISIBLE);
    }
}
