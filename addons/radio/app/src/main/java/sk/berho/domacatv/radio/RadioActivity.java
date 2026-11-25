package sk.berho.domacatv.radio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/** Obrazovka rádia: zoznam staníc ovládaný šípkami. OK prehrá/zastaví, podržanie OK odstráni stanicu. */
public final class RadioActivity extends Activity {

    private static final String PREFS = "radio";
    private static final String KEY_STATIONS = "stations";
    private static final int ACCENT = 0xFFE8553D;

    private final List<Stations.Station> stations = new ArrayList<>();
    private TextView status;
    private ListView list;
    private RowAdapter adapter;

    // Riadky: 0 = Zastaviť, potom stanice, potom Pridať a Obnoviť.
    private int stationsStart() { return 1; }
    private int addRow() { return 1 + stations.size(); }
    private int resetRow() { return 2 + stations.size(); }

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        loadStations();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF15100E);
        root.setPadding(dp(64), dp(36), dp(64), dp(24));

        TextView title = new TextView(this);
        title.setText(R.string.title);
        title.setTextSize(34);
        title.setTextColor(Color.WHITE);
        root.addView(title);

        status = new TextView(this);
        status.setTextSize(20);
        status.setTextColor(ACCENT);
        status.setPadding(0, dp(6), 0, dp(4));
        root.addView(status);

        TextView hint = new TextView(this);
        hint.setText(R.string.hint);
        hint.setTextSize(14);
        hint.setTextColor(0xFFAAA09C);
        hint.setPadding(0, 0, 0, dp(12));
        root.addView(hint);

        list = new ListView(this);
        list.setDivider(null);
        list.setSelector(new ColorDrawable(Color.TRANSPARENT));
        list.setItemsCanFocus(false);
        adapter = new RowAdapter();
        list.setAdapter(adapter);
        list.setOnItemClickListener((p, v, pos, id) -> onRow(pos));
        list.setOnItemLongClickListener((p, v, pos, id) -> {
            int i = pos - stationsStart();
            if (i >= 0 && i < stations.size()) {
                confirmDelete(i);
                return true;
            }
            return false;
        });
        root.addView(list, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(root);
        list.requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RadioService.listener = () -> runOnUiThread(this::refresh);
        refresh();
    }

    @Override
    protected void onPause() {
        RadioService.listener = null;
        super.onPause();
    }

    private void refresh() {
        String n = RadioService.currentName;
        switch (RadioService.state) {
            case CONNECTING: status.setText(getString(R.string.status_connecting, n)); break;
            case PLAYING: status.setText(getString(R.string.status_playing, n)); break;
            case ERROR: status.setText(getString(R.string.status_error, n)); break;
            default: status.setText(R.string.status_idle);
        }
        adapter.notifyDataSetChanged();
    }

    private void onRow(int pos) {
        if (pos == 0) {
            send(new Intent(this, RadioService.class).setAction(RadioService.ACTION_STOP));
        } else if (pos == addRow()) {
            showAdd();
        } else if (pos == resetRow()) {
            confirmReset();
        } else {
            Stations.Station s = stations.get(pos - stationsStart());
            boolean active = s.url.equals(RadioService.currentUrl)
                    && RadioService.state != RadioService.State.ERROR
                    && RadioService.state != RadioService.State.IDLE;
            if (active) {
                send(new Intent(this, RadioService.class).setAction(RadioService.ACTION_STOP));
            } else {
                send(new Intent(this, RadioService.class).setAction(RadioService.ACTION_PLAY)
                        .putExtra(RadioService.EXTRA_NAME, s.name)
                        .putExtra(RadioService.EXTRA_URL, s.url));
            }
        }
    }

    private void send(Intent i) {
        if (RadioService.ACTION_PLAY.equals(i.getAction())) startForegroundService(i);
        else if (RadioService.state != RadioService.State.IDLE) startService(i);
        refresh();
    }

    private void showAdd() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(24), dp(8), dp(24), 0);
        final EditText name = new EditText(this);
        name.setHint(R.string.add_name);
        name.setSingleLine();
        final EditText url = new EditText(this);
        url.setHint(R.string.add_url);
        url.setSingleLine();
        url.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        box.addView(name);
        box.addView(url);
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_title)
                .setView(box)
                .setPositiveButton(R.string.save, (d, w) -> {
                    String n = name.getText().toString().trim();
                    String u = url.getText().toString().trim();
                    if (Stations.isValid(n, u)) {
                        stations.add(new Stations.Station(n, u));
                        saveStations();
                        refresh();
                    } else {
                        Toast.makeText(this, R.string.add_invalid, Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDelete(int i) {
        Stations.Station s = stations.get(i);
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_title, s.name))
                .setPositiveButton(R.string.delete, (d, w) -> {
                    stations.remove(i);
                    saveStations();
                    refresh();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.reset_title)
                .setPositiveButton(R.string.reset_ok, (d, w) -> {
                    stations.clear();
                    stations.addAll(Stations.defaults());
                    saveStations();
                    refresh();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void loadStations() {
        SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
        String text = p.getString(KEY_STATIONS, null);
        stations.clear();
        stations.addAll(text == null ? Stations.defaults() : Stations.decode(text));
    }

    private void saveStations() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putString(KEY_STATIONS, Stations.encode(stations)).apply();
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private final class RowAdapter extends BaseAdapter {
        @Override public int getCount() { return stations.size() + 3; }
        @Override public Object getItem(int pos) { return pos; }
        @Override public long getItemId(int pos) { return pos; }

        @Override
        public View getView(int pos, View convert, ViewGroup parent) {
            TextView t = convert instanceof TextView ? (TextView) convert : new TextView(RadioActivity.this);
            t.setTextSize(22);
            t.setGravity(Gravity.CENTER_VERTICAL);
            t.setPadding(dp(20), dp(10), dp(20), dp(10));
            t.setMinHeight(dp(52));
            StateListDrawable bg = new StateListDrawable();
            bg.addState(new int[]{android.R.attr.state_selected}, new ColorDrawable(0xFF3A2620));
            bg.addState(new int[]{android.R.attr.state_focused}, new ColorDrawable(0xFF3A2620));
            bg.addState(new int[]{}, new ColorDrawable(Color.TRANSPARENT));
            t.setBackground(bg);
            int color = Color.WHITE;
            if (pos == 0) {
                t.setText(R.string.stop);
                color = 0xFFAAA09C;
            } else if (pos == addRow()) {
                t.setText(R.string.add);
                color = 0xFFAAA09C;
            } else if (pos == resetRow()) {
                t.setText(R.string.reset);
                color = 0xFF7D726E;
            } else {
                Stations.Station s = stations.get(pos - stationsStart());
                boolean active = s.url.equals(RadioService.currentUrl)
                        && RadioService.state != RadioService.State.IDLE;
                t.setText(active ? "▶  " + s.name : s.name);
                if (active) color = ACCENT;
            }
            t.setTextColor(color);
            return t;
        }
    }
}
