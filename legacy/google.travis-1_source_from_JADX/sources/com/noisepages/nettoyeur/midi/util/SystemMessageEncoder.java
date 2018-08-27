package com.noisepages.nettoyeur.midi.util;

import com.noisepages.nettoyeur.midi.MidiReceiver;

public class SystemMessageEncoder implements SystemMessageReceiver {
    private final MidiReceiver receiver;

    public SystemMessageEncoder(MidiReceiver receiver) {
        this.receiver = receiver;
    }

    public void onSystemExclusive(byte[] sysex) {
        this.receiver.beginBlock();
        this.receiver.onRawByte((byte) -16);
        for (byte b : sysex) {
            this.receiver.onRawByte(b);
        }
        this.receiver.onRawByte((byte) -9);
        this.receiver.endBlock();
    }

    public void onTimeCode(int value) {
        this.receiver.beginBlock();
        this.receiver.onRawByte((byte) -15);
        this.receiver.onRawByte((byte) value);
        this.receiver.endBlock();
    }

    public void onSongPosition(int pointer) {
        if (pointer < 0 || pointer > 16383) {
            throw new IllegalArgumentException("song position pointer out of range: " + pointer);
        }
        this.receiver.beginBlock();
        this.receiver.onRawByte((byte) -14);
        this.receiver.onRawByte((byte) (pointer & 127));
        this.receiver.onRawByte((byte) (pointer >> 7));
        this.receiver.endBlock();
    }

    public void onSongSelect(int index) {
        if (index < 0 || index > 127) {
            throw new IllegalArgumentException("song index out of range: " + index);
        }
        this.receiver.beginBlock();
        this.receiver.onRawByte((byte) -13);
        this.receiver.onRawByte((byte) (index & 127));
        this.receiver.endBlock();
    }

    public void onTuneRequest() {
        this.receiver.onRawByte((byte) -10);
    }

    public void onTimingClock() {
        this.receiver.onRawByte((byte) -8);
    }

    public void onStart() {
        this.receiver.onRawByte((byte) -6);
    }

    public void onContinue() {
        this.receiver.onRawByte((byte) -5);
    }

    public void onStop() {
        this.receiver.onRawByte((byte) -4);
    }

    public void onActiveSensing() {
        this.receiver.onRawByte((byte) -2);
    }

    public void onSystemReset() {
        this.receiver.onRawByte((byte) -1);
    }
}
