package io.github.fabb.wigai.bitwig;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongSupplier;

/**
 * A bounded, numbered log of change events that readers poll by sequence number, optionally waiting for new
 * ones. Rapid changes to the same thing (a fader being dragged) are coalesced: an event replaces the previous
 * event with the same key when that one is younger than the coalescing window, so readers see the latest value
 * without a flood.
 */
public final class EventBuffer {

    public record Event(long seq, long timeMs, String type, String key, Map<String, Object> data) {
        public Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("seq", seq);
            m.put("time_ms", timeMs);
            m.put("type", type);
            m.putAll(data);
            return m;
        }
    }

    private final int capacity;
    private final long coalesceMs;
    private final LongSupplier clock;
    private final Deque<Event> events = new ArrayDeque<>();
    private final Map<String, Event> lastByKey = new HashMap<>();
    private long seq;
    private long maxDroppedSeq;

    public EventBuffer(int capacity, long coalesceMs) {
        this(capacity, coalesceMs, System::currentTimeMillis);
    }

    EventBuffer(int capacity, long coalesceMs, LongSupplier clock) {
        this.capacity = capacity;
        this.coalesceMs = coalesceMs;
        this.clock = clock;
    }

    public synchronized void add(String type, String key, Map<String, Object> data) {
        long now = clock.getAsLong();
        Event previous = lastByKey.get(key);
        if (previous != null && now - previous.timeMs() < coalesceMs) {
            events.remove(previous);
        }
        Event e = new Event(++seq, now, type, key, data);
        events.addLast(e);
        lastByKey.put(key, e);
        while (events.size() > capacity) {
            Event old = events.removeFirst();
            lastByKey.remove(old.key(), old);
            maxDroppedSeq = old.seq();
        }
        notifyAll();
    }

    /** The newest sequence number handed out so far (0 before any event). */
    public synchronized long latestSeq() {
        return seq;
    }

    /**
     * Events after {@code since} whose type is in {@code types} (all types when null or empty), waiting up to
     * {@code waitMs} for at least one to arrive.
     */
    public synchronized List<Event> since(long since, Collection<String> types, long waitMs) throws InterruptedException {
        long deadline = clock.getAsLong() + Math.max(0, waitMs);
        List<Event> found = collect(since, types);
        while (found.isEmpty()) {
            long left = deadline - clock.getAsLong();
            if (left <= 0) {
                break;
            }
            wait(left);
            found = collect(since, types);
        }
        return found;
    }

    private List<Event> collect(long since, Collection<String> types) {
        List<Event> out = new ArrayList<>();
        for (Iterator<Event> it = events.iterator(); it.hasNext(); ) {
            Event e = it.next();
            if (e.seq() > since && (types == null || types.isEmpty() || types.contains(e.type()))) {
                out.add(e);
            }
        }
        return out;
    }

    /** Whether events after {@code since} were already pushed out of the buffer. */
    public synchronized boolean missedSince(long since) {
        return since < maxDroppedSeq;
    }
}
