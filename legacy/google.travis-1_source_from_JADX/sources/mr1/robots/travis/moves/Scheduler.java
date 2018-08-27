package mr1.robots.travis.moves;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Scheduler {
    public static int audioLatency = 0;
    private volatile Map<Runnable, ScheduledFuture<?>> futures = new IdentityHashMap();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void post(Runnable runnable) {
        removeFinished();
        this.futures.put(runnable, this.scheduler.schedule(runnable, 0, TimeUnit.MILLISECONDS));
    }

    public void postDelayed(Runnable runnable, long millis) {
        removeFinished();
        this.futures.put(runnable, this.scheduler.schedule(runnable, millis, TimeUnit.MILLISECONDS));
    }

    public void removeCallbacks(Runnable runnable) {
        if (doesExist(runnable)) {
            ((ScheduledFuture) this.futures.get(runnable)).cancel(true);
            this.futures.remove(runnable);
        }
    }

    private boolean doesExist(Runnable runnable) {
        if (this.futures.containsKey(runnable)) {
            return true;
        }
        return false;
    }

    private boolean isFinished(Runnable runnable) {
        if (this.futures.containsKey(runnable)) {
            return ((ScheduledFuture) this.futures.get(runnable)).isDone();
        }
        return true;
    }

    private void removeFinished() {
        Iterator it = this.futures.entrySet().iterator();
        while (it.hasNext()) {
            if (((ScheduledFuture) this.futures.get(((Entry) it.next()).getKey())).isDone()) {
                it.remove();
            }
        }
    }

    public void setAudioLatency(int latency) {
        audioLatency = latency;
    }
}
