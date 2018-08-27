package org.puredata.android.utils;

import android.os.Handler;
import android.util.Log;
import org.puredata.core.utils.PdDispatcher;

public class PdUiDispatcher extends PdDispatcher {
    private static final String TAG = PdUiDispatcher.class.getSimpleName();
    private final Handler handler = new Handler();
    private final Thread target = Thread.currentThread();

    public void print(String s) {
        Log.i(TAG, "print: " + s);
    }

    public synchronized void receiveBang(final String source) {
        if (Thread.currentThread().equals(this.target)) {
            super.receiveBang(source);
        } else {
            this.handler.post(new Runnable() {
                public void run() {
                    super.receiveBang(source);
                }
            });
        }
    }

    public synchronized void receiveFloat(final String source, final float x) {
        if (Thread.currentThread().equals(this.target)) {
            super.receiveFloat(source, x);
        } else {
            this.handler.post(new Runnable() {
                public void run() {
                    super.receiveFloat(source, x);
                }
            });
        }
    }

    public synchronized void receiveSymbol(final String source, final String symbol) {
        if (Thread.currentThread().equals(this.target)) {
            super.receiveSymbol(source, symbol);
        } else {
            this.handler.post(new Runnable() {
                public void run() {
                    super.receiveSymbol(source, symbol);
                }
            });
        }
    }

    public synchronized void receiveList(final String source, final Object... args) {
        if (Thread.currentThread().equals(this.target)) {
            super.receiveList(source, args);
        } else {
            this.handler.post(new Runnable() {
                public void run() {
                    super.receiveList(source, args);
                }
            });
        }
    }

    public synchronized void receiveMessage(final String source, final String symbol, final Object... args) {
        if (Thread.currentThread().equals(this.target)) {
            super.receiveMessage(source, symbol, args);
        } else {
            this.handler.post(new Runnable() {
                public void run() {
                    super.receiveMessage(source, symbol, args);
                }
            });
        }
    }
}
