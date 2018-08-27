package org.puredata.core;

public interface PdReceiver extends PdListener {

    public static class Adapter extends org.puredata.core.PdListener.Adapter implements PdReceiver {
        public void print(String s) {
        }
    }

    void print(String str);
}
