package sk.berho.domacatv.radio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public class StationsTest {
    @Test
    public void defaultsMatchRodinar() {
        List<Stations.Station> d = Stations.defaults();
        assertEquals(13, d.size());
        for (Stations.Station s : d) assertTrue(s.name, Stations.isValid(s.name, s.url));
    }

    @Test
    public void encodeDecodeRoundTrip() {
        List<Stations.Station> back = Stations.decode(Stations.encode(Stations.defaults()));
        assertEquals(13, back.size());
        assertEquals("Rádio Lumen", back.get(0).name);
        assertEquals("http://audio.lumen.sk:8000/live128.mp3", back.get(0).url);
    }

    @Test
    public void badLinesAreSkipped() {
        List<Stations.Station> s = Stations.decode("bez tabulatora\n\tbez nazvu\nOK\thttps://example.sk/x.mp3\nZle\tftp://x\n");
        assertEquals(1, s.size());
        assertEquals("OK", s.get(0).name);
        assertTrue(Stations.decode(null).isEmpty());
    }

    @Test
    public void validation() {
        assertFalse(Stations.isValid("", "https://example.sk/a"));
        assertFalse(Stations.isValid("A", "example.sk"));
        assertTrue(Stations.isValid("A", "http://example.sk/a.mp3"));
    }
}
