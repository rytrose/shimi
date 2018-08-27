package org.puredata.core;

public interface PdMidiListener {
    boolean beginBlock();

    void endBlock();

    void receiveAftertouch(int i, int i2);

    void receiveControlChange(int i, int i2, int i3);

    void receiveNoteOn(int i, int i2, int i3);

    void receivePitchBend(int i, int i2);

    void receivePolyAftertouch(int i, int i2, int i3);

    void receiveProgramChange(int i, int i2);
}
