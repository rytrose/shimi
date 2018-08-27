package org.puredata.android.midi;

import com.noisepages.nettoyeur.midi.MidiReceiver;
import org.puredata.core.PdMidiReceiver;

public class PdToMidiAdapter implements PdMidiReceiver {
    private final MidiReceiver receiver;

    public PdToMidiAdapter(MidiReceiver receiver) {
        this.receiver = receiver;
    }

    public void receiveProgramChange(int channel, int value) {
        this.receiver.onProgramChange(channel, value);
    }

    public void receivePolyAftertouch(int channel, int pitch, int value) {
        this.receiver.onPolyAftertouch(channel, pitch, value);
    }

    public void receivePitchBend(int channel, int value) {
        this.receiver.onPitchBend(channel, value);
    }

    public void receiveNoteOn(int channel, int pitch, int velocity) {
        this.receiver.onNoteOn(channel, pitch, velocity);
    }

    public void receiveMidiByte(int port, int value) {
        this.receiver.onRawByte((byte) value);
    }

    public void receiveControlChange(int channel, int controller, int value) {
        this.receiver.onControlChange(channel, controller, value);
    }

    public void receiveAftertouch(int channel, int value) {
        this.receiver.onAftertouch(channel, value);
    }

    public boolean beginBlock() {
        return this.receiver.beginBlock();
    }

    public void endBlock() {
        this.receiver.endBlock();
    }
}
