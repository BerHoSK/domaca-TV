package sk.berho.domacatv.radio;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;

/**
 * Prehráva stream v popredí, takže rádio hrá aj na domovskej obrazovke. Jedna stanica naraz.
 * ExoPlayer drží väčšiu vyrovnávaciu pamäť (až 60 s) a pri výpadku streamu sa sám pripojí znova.
 */
public final class RadioService extends Service {

    static final String ACTION_PLAY = "sk.berho.domacatv.radio.PLAY";
    static final String ACTION_STOP = "sk.berho.domacatv.radio.STOP";
    static final String EXTRA_NAME = "name";
    static final String EXTRA_URL = "url";

    enum State { IDLE, CONNECTING, PLAYING, ERROR }

    /** Stav pre obrazovku rádia. Číta sa len z hlavného vlákna. */
    static State state = State.IDLE;
    static String currentName;
    static String currentUrl;
    static Runnable listener;

    private static final String CHANNEL = "radio";
    private static final int NOTIFICATION = 1;
    /** Koľkokrát za sebou skúsiť stream znova, kým to vzdáme. */
    private static final int MAX_RETRIES = 5;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private ExoPlayer player;
    private int retries;

    private final Player.Listener events = new Player.Listener() {
        @Override
        public void onPlaybackStateChanged(int playbackState) {
            if (playbackState == Player.STATE_READY && player != null && player.getPlayWhenReady()) {
                retries = 0;
                setState(State.PLAYING);
                updateNotification(getString(R.string.status_playing, currentName));
            } else if (playbackState == Player.STATE_BUFFERING) {
                setState(State.CONNECTING);
            } else if (playbackState == Player.STATE_ENDED) {
                // Živý stream nemá koniec: server spojenie zavrel, pripojíme sa znova.
                retry();
            }
        }

        @Override
        public void onPlayerError(PlaybackException error) {
            retry();
        }

        @Override
        public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
            // Iná aplikácia (Netflix, YouTube) prevzala zvuk: rádio zastavíme.
            if (!playWhenReady && reason == Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS) {
                stopAll();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel(CHANNEL, getString(R.string.channel),
                NotificationManager.IMPORTANCE_LOW));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        if (ACTION_PLAY.equals(action)) {
            play(intent.getStringExtra(EXTRA_NAME), intent.getStringExtra(EXTRA_URL));
        } else {
            stopAll();
        }
        return START_NOT_STICKY;
    }

    private void play(String name, String url) {
        handler.removeCallbacksAndMessages(null);
        currentName = name;
        currentUrl = url;
        retries = 0;
        setState(State.CONNECTING);
        startForeground(NOTIFICATION, notification(getString(R.string.status_connecting, name)));
        start();
    }

    /** Vytvorí prehrávač pre currentUrl a spustí ho. */
    private void start() {
        releasePlayer();

        DefaultHttpDataSource.Factory http = new DefaultHttpDataSource.Factory()
                .setUserAgent("DomacaTV-Radio/0.2")
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(15000)
                .setReadTimeoutMs(20000);

        // Väčšia vyrovnávacia pamäť: hrať po 3 s, držať 30 až 60 s dopredu, po výpadku čakať 5 s.
        DefaultLoadControl load = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(30000, 60000, 3000, 5000)
                .build();

        player = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(http))
                .setLoadControl(load)
                .setWakeMode(C.WAKE_MODE_NETWORK)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(), true)
                .build();
        player.addListener(events);
        player.setMediaItem(MediaItem.fromUri(currentUrl));
        player.prepare();
        player.play();
    }

    private void retry() {
        if (currentUrl == null) return;
        if (retries >= MAX_RETRIES) {
            fail();
            return;
        }
        retries++;
        setState(State.CONNECTING);
        updateNotification(getString(R.string.status_connecting, currentName));
        releasePlayer();
        handler.postDelayed(this::start, 2000L * retries);
    }

    private void fail() {
        handler.removeCallbacksAndMessages(null);
        releasePlayer();
        setState(State.ERROR);
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    private void stopAll() {
        handler.removeCallbacksAndMessages(null);
        releasePlayer();
        currentName = null;
        currentUrl = null;
        setState(State.IDLE);
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    private void releasePlayer() {
        if (player != null) {
            ExoPlayer p = player;
            player = null;
            p.removeListener(events);
            p.release();
        }
    }

    private void setState(State s) {
        state = s;
        if (listener != null) listener.run();
        announce(this);
    }

    static final String ACTION_STATE = "sk.berho.domacatv.radio.STATE";
    static final String PERMISSION = "sk.berho.domacatv.permission.RADIO_STATE";
    static final String LAUNCHER = "com.gathin.tvhome";

    /** Pošle launcheru Domáca TV, čo hrá (pozdrav na Domove a šetrič). */
    static void announce(android.content.Context ctx) {
        boolean playing = currentName != null && (state == State.PLAYING || state == State.CONNECTING);
        Intent i = new Intent(ACTION_STATE).setPackage(LAUNCHER)
                .putExtra("playing", playing)
                .putExtra("name", playing ? currentName : "");
        try {
            ctx.sendBroadcast(i, PERMISSION);
        } catch (RuntimeException ignored) {
        }
    }

    private Notification notification(String text) {
        return new Notification.Builder(this, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(text)
                .setOngoing(true)
                .build();
    }

    private void updateNotification(String text) {
        getSystemService(NotificationManager.class).notify(NOTIFICATION, notification(text));
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        releasePlayer();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
