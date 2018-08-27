package com.noisepages.nettoyeur.midi.util;

public interface SystemMessageReceiver {

    public static class DummyReceiver implements SystemMessageReceiver {
        public void onSystemExclusive(byte[] sysex) {
        }

        public void onTimeCode(int value) {
        }

        public void onSongPosition(int pointer) {
        }

        public void onSongSelect(int index) {
        }

        public void onTuneRequest() {
        }

        public void onTimingClock() {
        }

        public void onStart() {
        }

        public void onContinue() {
        }

        public void onStop() {
        }

        public void onActiveSensing() {
        }

        public void onSystemReset() {
        }
    }

    void onActiveSensing();

    void onContinue();

    void onSongPosition(int i);

    void onSongSelect(int i);

    void onStart();

    void onStop();

    void onSystemExclusive(byte[] bArr);

    void onSystemReset();

    void onTimeCode(int i);

    void onTimingClock();

    void onTuneRequest();
}
