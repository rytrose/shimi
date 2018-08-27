package org.puredata.core.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.puredata.core.PdBase;
import org.puredata.core.PdListener;
import org.puredata.core.PdReceiver;

public abstract class PdDispatcher implements PdReceiver {
    private final Map<String, Set<PdListener>> listeners = new HashMap();

    public abstract void print(String str);

    public synchronized void addListener(String symbol, PdListener listener) {
        Set<PdListener> selected = (Set) this.listeners.get(symbol);
        if (selected == null) {
            if (PdBase.subscribe(symbol) != 0) {
                throw new IllegalArgumentException("bad symbol: " + symbol);
            }
            selected = new HashSet();
            this.listeners.put(symbol, selected);
        }
        selected.add(listener);
    }

    public synchronized void removeListener(String symbol, PdListener listener) {
        Set<PdListener> selected = (Set) this.listeners.get(symbol);
        if (selected != null) {
            selected.remove(listener);
            if (selected.isEmpty()) {
                PdBase.unsubscribe(symbol);
                this.listeners.remove(symbol);
            }
        }
    }

    public synchronized void release() {
        for (String symbol : this.listeners.keySet()) {
            PdBase.unsubscribe(symbol);
        }
        this.listeners.clear();
    }

    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }

    public synchronized void receiveBang(String source) {
        Set<PdListener> selected = (Set) this.listeners.get(source);
        if (selected != null) {
            for (PdListener listener : selected) {
                listener.receiveBang(source);
            }
        }
    }

    public synchronized void receiveFloat(String source, float x) {
        Set<PdListener> selected = (Set) this.listeners.get(source);
        if (selected != null) {
            for (PdListener listener : selected) {
                listener.receiveFloat(source, x);
            }
        }
    }

    public synchronized void receiveSymbol(String source, String symbol) {
        Set<PdListener> selected = (Set) this.listeners.get(source);
        if (selected != null) {
            for (PdListener listener : selected) {
                listener.receiveSymbol(source, symbol);
            }
        }
    }

    public synchronized void receiveList(String source, Object... args) {
        Set<PdListener> selected = (Set) this.listeners.get(source);
        if (selected != null) {
            for (PdListener listener : selected) {
                listener.receiveList(source, args);
            }
        }
    }

    public synchronized void receiveMessage(String source, String symbol, Object... args) {
        Set<PdListener> selected = (Set) this.listeners.get(source);
        if (selected != null) {
            for (PdListener listener : selected) {
                listener.receiveMessage(source, symbol, args);
            }
        }
    }
}
