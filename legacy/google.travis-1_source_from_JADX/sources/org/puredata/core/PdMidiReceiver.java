package org.puredata.core;

public interface PdMidiReceiver extends PdMidiListener {
    void receiveMidiByte(int i, int i2);
}
