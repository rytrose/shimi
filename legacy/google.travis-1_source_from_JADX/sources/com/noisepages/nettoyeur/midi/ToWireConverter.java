package com.noisepages.nettoyeur.midi;

import com.noisepages.nettoyeur.common.RawByteReceiver;

public class ToWireConverter implements MidiReceiver {
    private final RawByteReceiver rawReceiver;

    public ToWireConverter(RawByteReceiver rawReceiver) {
        this.rawReceiver = rawReceiver;
    }

    public void onNoteOff(int ch, int note, int vel) {
        write(128, ch, note, vel);
    }

    public void onNoteOn(int ch, int note, int vel) {
        write(144, ch, note, vel);
    }

    public void onPolyAftertouch(int ch, int note, int vel) {
        write(160, ch, note, vel);
    }

    public void onControlChange(int ch, int ctl, int val) {
        write(176, ch, ctl, val);
    }

    public void onProgramChange(int ch, int pgm) {
        write(192, ch, pgm);
    }

    public void onAftertouch(int ch, int vel) {
        write(208, ch, vel);
    }

    public void onPitchBend(int ch, int val) {
        val += 8192;
        write(224, ch, val & 127, val >> 7);
    }

    public void onRawByte(byte value) {
        writeBytes(value);
    }

    private void write(int msg, int ch, int a) {
        writeBytes(firstByte(msg, ch), (byte) a);
    }

    private void write(int msg, int ch, int a, int b) {
        writeBytes(firstByte(msg, ch), (byte) a, (byte) b);
    }

    private byte firstByte(int msg, int ch) {
        return (byte) ((ch & 15) | msg);
    }

    private void writeBytes(byte... out) {
        this.rawReceiver.onBytesReceived(out.length, out);
    }

    public boolean beginBlock() {
        return this.rawReceiver.beginBlock();
    }

    public void endBlock() {
        this.rawReceiver.endBlock();
    }
}
