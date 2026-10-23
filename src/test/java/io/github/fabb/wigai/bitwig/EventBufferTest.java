package io.github.fabb.wigai.bitwig;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class EventBufferTest {

    private final AtomicLong clock = new AtomicLong(1000);

    private EventBuffer buffer(int capacity) {
        return new EventBuffer(capacity, 250, clock::get);
    }

    @Test
    void eventsAreNumberedAndFilteredByType() throws Exception {
        EventBuffer b = buffer(10);
        b.add("track", "track:0:mute", Map.of("value", true));
        b.add("transport", "transport:playing", Map.of("playing", true));
        assertEquals(2, b.latestSeq());
        assertEquals(2, b.since(0, null, 0).size());
        List<EventBuffer.Event> transport = b.since(0, List.of("transport"), 0);
        assertEquals(1, transport.size());
        assertEquals(2, transport.get(0).seq());
        assertTrue(b.since(2, null, 0).isEmpty());
        assertEquals("transport", transport.get(0).toMap().get("type"));
        assertEquals(true, transport.get(0).toMap().get("playing"));
    }

    @Test
    void rapidChangesToOneThingKeepOnlyTheLatest() throws Exception {
        EventBuffer b = buffer(10);
        b.add("track", "track:0:volume", Map.of("value", "-1.0 dB"));
        clock.addAndGet(100);
        b.add("track", "track:0:volume", Map.of("value", "-2.0 dB"));
        List<EventBuffer.Event> all = b.since(0, null, 0);
        assertEquals(1, all.size());
        assertEquals("-2.0 dB", all.get(0).data().get("value"));
        assertEquals(2, all.get(0).seq());

        clock.addAndGet(1000);
        b.add("track", "track:0:volume", Map.of("value", "-3.0 dB"));
        assertEquals(2, b.since(0, null, 0).size(), "changes further apart than the window are kept");
    }

    @Test
    void overflowIsReportedAsMissedEvents() throws Exception {
        EventBuffer b = buffer(3);
        for (int i = 0; i < 5; i++) {
            b.add("track", "k" + i, Map.of());
        }
        assertEquals(3, b.since(0, null, 0).size());
        assertTrue(b.missedSince(0));
        assertTrue(b.missedSince(1));
        assertFalse(b.missedSince(2));
    }

    @Test
    void waitingReturnsAsSoonAsAnEventArrives() throws Exception {
        EventBuffer b = new EventBuffer(10, 250);
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(150);
            } catch (InterruptedException ignored) {
            }
            b.add("clip", "clip:0:0", Map.of("state", "playing"));
        });
        long start = System.currentTimeMillis();
        t.start();
        List<EventBuffer.Event> got = b.since(0, null, 5000);
        long took = System.currentTimeMillis() - start;
        assertEquals(1, got.size());
        assertTrue(took < 2000, "returned after " + took + " ms");

        start = System.currentTimeMillis();
        assertTrue(b.since(1, null, 200).isEmpty());
        assertTrue(System.currentTimeMillis() - start >= 150, "waits when nothing arrives");
    }
}
