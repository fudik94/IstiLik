package com.example.istivat;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class HistoryManagerTest {

    @Test
    public void parseHistory_emptyString_returnsEmptyList() {
        List<String[]> result = HistoryManager.parseHistory("");
        assertTrue(result.isEmpty());
    }

    @Test
    public void parseHistory_nullString_returnsEmptyList() {
        List<String[]> result = HistoryManager.parseHistory(null);
        assertTrue(result.isEmpty());
    }

    @Test
    public void parseHistory_singleEntry_returnsOneParsedEntry() {
        String raw = "2026-03-26|50.0|24";
        List<String[]> result = HistoryManager.parseHistory(raw);
        assertEquals(1, result.size());
        assertEquals("2026-03-26", result.get(0)[0]);
        assertEquals("50.0", result.get(0)[1]);
        assertEquals("24", result.get(0)[2]);
    }

    @Test
    public void parseHistory_twoEntries_returnsTwoEntries() {
        String raw = "2026-03-26|50.0|24\n2026-03-25|30.0|28";
        List<String[]> result = HistoryManager.parseHistory(raw);
        assertEquals(2, result.size());
    }

    @Test
    public void parseHistory_malformedEntry_skipsIt() {
        String raw = "bad_entry\n2026-03-26|50.0|24";
        List<String[]> result = HistoryManager.parseHistory(raw);
        assertEquals(1, result.size());
    }

    @Test
    public void serializeHistory_roundTrip() {
        String original = "2026-03-26|50.0|24\n2026-03-25|30.0|28";
        List<String[]> parsed = HistoryManager.parseHistory(original);
        String serialized = HistoryManager.serializeHistory(parsed);
        assertEquals(original, serialized);
    }

    @Test
    public void serializeHistory_emptyList_returnsEmptyString() {
        List<String[]> empty = HistoryManager.parseHistory("");
        assertEquals("", HistoryManager.serializeHistory(empty));
    }

    @Test
    public void deleteEntryAt_removesCorrectEntry() {
        String raw = "2026-03-26|50.0|24\n2026-03-25|30.0|28\n2026-03-24|20.0|32";
        List<String[]> entries = HistoryManager.parseHistory(raw);
        HistoryManager.deleteEntryAt(entries, 1);
        assertEquals(2, entries.size());
        assertEquals("50.0", entries.get(0)[1]);
        assertEquals("20.0", entries.get(1)[1]);
    }
}
