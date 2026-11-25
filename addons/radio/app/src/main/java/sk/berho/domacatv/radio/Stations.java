package sk.berho.domacatv.radio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Zoznam staníc: predvolené (z Rodinára+) a ukladanie ako text. Bez Android tried, preto je testovateľný. */
final class Stations {

    static final class Station {
        final String name;
        final String url;

        Station(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }

    /** Predvolené stanice, rovnaké ako DEF_RADIOS v Rodinári+. */
    static List<Station> defaults() {
        List<Station> s = new ArrayList<>();
        s.add(new Station("Rádio Lumen", "http://audio.lumen.sk:8000/live128.mp3"));
        s.add(new Station("Rádio Vlna", "http://stream.radiovlna.sk/vlna-hi.mp3"));
        s.add(new Station("Rádio FM", "https://icecast.stv.livebox.sk/fm_128.mp3"));
        s.add(new Station("Fun Radio", "http://stream.funradio.sk:8000/fun128.mp3"));
        s.add(new Station("Rádio Slovensko", "http://live.slovakradio.sk:8000/Slovensko_128.mp3"));
        s.add(new Station("Rádio Devín", "http://icecast.stv.livebox.sk/devin_128.mp3"));
        s.add(new Station("Europa 2", "https://stream.bauermedia.sk/europa2.mp3"));
        s.add(new Station("Rádio Beta 80-90", "http://109.71.67.102:8000/beta_80a90.mp3"));
        s.add(new Station("Rádio Beta", "http://109.71.67.102:8000/beta_live_high.mp3"));
        s.add(new Station("Rádio Expres", "https://stream.bauermedia.sk/128.mp3"));
        s.add(new Station("Rádio Melody", "https://stream.bauermedia.sk/melody-hi.mp3"));
        s.add(new Station("Hitrádio Slovakia", "https://hitradioslovakia.stream.laut.fm/hitradioslovakia"));
        s.add(new Station("Rádio Regina Západ", "http://icecast.stv.livebox.sk/regina-ba_128.mp3"));
        return s;
    }

    /** Jedna stanica na riadok: názov, tabulátor, adresa. */
    static String encode(List<Station> list) {
        StringBuilder b = new StringBuilder();
        for (Station s : list) {
            b.append(clean(s.name)).append('\t').append(clean(s.url)).append('\n');
        }
        return b.toString();
    }

    /** Opak encode; chybné riadky preskočí. Null alebo prázdny text = prázdny zoznam. */
    static List<Station> decode(String text) {
        if (text == null || text.isEmpty()) return Collections.emptyList();
        List<Station> out = new ArrayList<>();
        for (String line : text.split("\n")) {
            int tab = line.indexOf('\t');
            if (tab <= 0) continue;
            String name = line.substring(0, tab).trim();
            String url = line.substring(tab + 1).trim();
            if (isValid(name, url)) out.add(new Station(name, url));
        }
        return out;
    }

    static boolean isValid(String name, String url) {
        if (name == null || url == null) return false;
        name = name.trim();
        url = url.trim();
        return !name.isEmpty() && (url.startsWith("http://") || url.startsWith("https://")) && url.length() > 10;
    }

    private static String clean(String v) {
        return v.replace('\t', ' ').replace('\n', ' ').trim();
    }

    private Stations() {
    }
}
