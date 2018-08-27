package com.noisepages.nettoyeur.midi;

public interface MidiReceiver {

    public static class DummyReceiver implements MidiReceiver {
        public void onNoteOff(int channel, int key, int velocity) {
        }

        public void onNoteOn(int channel, int key, int velocity) {
        }

        public void onPolyAftertouch(int channel, int key, int velocity) {
        }

        public void onControlChange(int channel, int controller, int value) {
        }

        public void onProgramChange(int channel, int program) {
        }

        public void onAftertouch(int channel, int velocity) {
        }

        public void onPitchBend(int channel, int value) {
        }

        public void onRawByte(byte value) {
        }

        public boolean beginBlock() {
            return false;
        }

        public void endBlock() {
        }
    }

    boolean beginBlock();

    void endBlock();

    void onAftertouch(int i, int i2);

    void onControlChange(int i, int i2, int i3);

    void onNoteOff(int i, int i2, int i3);

    void onNoteOn(int i, int i2, int i3);

    void onPitchBend(int i, int i2);

    void onPolyAftertouch(int i, int i2, int i3);

    void onProgramChange(int i, int i2);

    void onRawByte(byte b);
}
