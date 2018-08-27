package org.puredata.android.midi;

import com.noisepages.nettoyeur.midi.MidiReceiver;
import org.puredata.core.PdBase;

public class MidiToPdAdapter implements MidiReceiver {
    public void onRawByte(byte value) {
        PdBase.sendMidiByte(0, value);
    }

    public void onProgramChange(int channel, int program) {
        PdBase.sendProgramChange(channel, program);
    }

    public void onPolyAftertouch(int channel, int key, int velocity) {
        PdBase.sendPolyAftertouch(channel, key, velocity);
    }

    public void onPitchBend(int channel, int value) {
        PdBase.sendPitchBend(channel, value);
    }

    public void onNoteOn(int channel, int key, int velocity) {
        PdBase.sendNoteOn(channel, key, velocity);
    }

    public void onNoteOff(int channel, int key, int velocity) {
        PdBase.sendNoteOn(channel, key, 0);
    }

    public void onControlChange(int channel, int controller, int value) {
        PdBase.sendControlChange(channel, controller, value);
    }

    public void onAftertouch(int channel, int velocity) {
        PdBase.sendAftertouch(channel, velocity);
    }

    public boolean beginBlock() {
        return false;
    }

    public void endBlock() {
    }
}
