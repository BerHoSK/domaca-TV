package sk.berho.domacatv.radio;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;

/** Prehráva stream v popredí, takže rádio hrá aj na domovskej obrazovke. Jedna stanica naraz. */
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

    private MediaPlayer player;
    private AudioManager audio;
    private AudioFocusRequest focus;

    private final AudioManager.OnAudioFocusChangeListener onFocus = change -> {
        // Iná aplikácia (Netflix, YouTube) prevzala zvuk: rádio zastavíme.
        if (change == AudioManager.AUDIOFOCUS_LOSS) stopAll();
    };

    @Override
    public void onCreate() {
        super.onCreate();
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
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
        releasePlayer();
        currentName = name;
        currentUrl = url;
        setState(State.CONNECTING);
        startForeground(NOTIFICATION, notification(getString(R.string.status_connecting, name)));

        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        focus = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attrs)
                .setOnAudioFocusChangeListener(onFocus)
                .build();
        audio.requestAudioFocus(focus);

        final MediaPlayer mp = new MediaPlayer();
        player = mp;
        try {
            mp.setAudioAttributes(attrs);
            mp.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
            mp.setDataSource(url);
            mp.setOnPreparedListener(p -> {
                if (p != player) return;
                p.start();
                setState(State.PLAYING);
                updateNotification(getString(R.string.status_playing, currentName));
            });
            mp.setOnErrorListener((p, what, extra) -> {
                if (p == player) fail();
                return true;
            });
            mp.setOnCompletionListener(p -> {
                if (p == player) fail();
            });
            mp.prepareAsync();
        } catch (Exception e) {
            fail();
        }
    }

    private void fail() {
        releasePlayer();
        abandonFocus();
        setState(State.ERROR);
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    private void stopAll() {
        releasePlayer();
        abandonFocus();
        currentName = null;
        currentUrl = null;
        setState(State.IDLE);
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    private void releasePlayer() {
        if (player != null) {
            MediaPlayer p = player;
            player = null;
            try {
                p.reset();
            } catch (Exception ignored) {
            }
            p.release();
        }
    }

    private void abandonFocus() {
        if (focus != null) {
            audio.abandonAudioFocusRequest(focus);
            focus = null;
        }
    }

    private static void setState(State s) {
        state = s;
        if (listener != null) listener.run();
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
        releasePlayer();
        abandonFocus();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
