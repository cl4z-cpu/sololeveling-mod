package com.sololeveling.mod.power;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Scheduler {
    private static class Entry {
        int delay;
        final Runnable task;
        Entry(int delay, Runnable task) { this.delay = delay; this.task = task; }
    }

    private static final List<Entry> TASKS = new ArrayList<>();
    private static final List<Entry> PENDING = new ArrayList<>();

    public static void later(int ticks, Runnable r) { PENDING.add(new Entry(ticks, r)); }

    public static void clear() { TASKS.clear(); PENDING.clear(); }

    public static void tick() {
        TASKS.addAll(PENDING);
        PENDING.clear();
        Iterator<Entry> it = TASKS.iterator();
        List<Runnable> toRun = new ArrayList<>();
        while (it.hasNext()) {
            Entry e = it.next();
            if (--e.delay <= 0) { toRun.add(e.task); it.remove(); }
        }
        for (Runnable r : toRun) {
            try { r.run(); } catch (Exception ex) { ex.printStackTrace(); }
        }
    }
}
