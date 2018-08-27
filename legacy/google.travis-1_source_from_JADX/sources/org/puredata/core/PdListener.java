package org.puredata.core;

public interface PdListener {

    public static class Adapter implements PdListener {
        public void receiveBang(String source) {
        }

        public void receiveFloat(String source, float x) {
        }

        public void receiveSymbol(String source, String symbol) {
        }

        public void receiveList(String source, Object... args) {
        }

        public void receiveMessage(String source, String symbol, Object... args) {
        }
    }

    void receiveBang(String str);

    void receiveFloat(String str, float f);

    void receiveList(String str, Object... objArr);

    void receiveMessage(String str, String str2, Object... objArr);

    void receiveSymbol(String str, String str2);
}
