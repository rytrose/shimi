package com.noisepages.nettoyeur.midi;

import com.noisepages.nettoyeur.common.RawByteReceiver;
import mr1.robots.travis.moves.gestures.emotions.DecayTypes;

public class FromWireConverter implements RawByteReceiver {
    /* renamed from: $SWITCH_TABLE$com$noisepages$nettoyeur$midi$FromWireConverter$State */
    private static /* synthetic */ int[] f14x44433b78;
    private int channel;
    private int firstByte;
    private final MidiReceiver midiReceiver;
    private State midiState = State.NONE;

    private enum State {
        NOTE_OFF,
        NOTE_ON,
        POLY_TOUCH,
        CONTROL_CHANGE,
        PROGRAM_CHANGE,
        AFTERTOUCH,
        PITCH_BEND,
        NONE
    }

    /* renamed from: $SWITCH_TABLE$com$noisepages$nettoyeur$midi$FromWireConverter$State */
    static /* synthetic */ int[] m3x44433b78() {
        int[] iArr = f14x44433b78;
        if (iArr == null) {
            iArr = new int[State.values().length];
            try {
                iArr[State.AFTERTOUCH.ordinal()] = 6;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[State.CONTROL_CHANGE.ordinal()] = 4;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[State.NONE.ordinal()] = 8;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[State.NOTE_OFF.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[State.NOTE_ON.ordinal()] = 2;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[State.PITCH_BEND.ordinal()] = 7;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[State.POLY_TOUCH.ordinal()] = 3;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[State.PROGRAM_CHANGE.ordinal()] = 5;
            } catch (NoSuchFieldError e8) {
            }
            f14x44433b78 = iArr;
        }
        return iArr;
    }

    public FromWireConverter(MidiReceiver midiReceiver) {
        this.midiReceiver = midiReceiver;
    }

    public void onBytesReceived(int nBytes, byte[] buffer) {
        for (int i = 0; i < nBytes; i++) {
            processByte(buffer[i]);
        }
    }

    private void processByte(int b) {
        if (b < 0) {
            this.midiState = State.values()[(b >> 4) & 7];
            if (this.midiState != State.NONE) {
                this.channel = b & 15;
                this.firstByte = -1;
                return;
            }
            this.midiReceiver.onRawByte((byte) b);
            return;
        }
        switch (m3x44433b78()[this.midiState.ordinal()]) {
            case 1:
                if (this.firstByte < 0) {
                    this.firstByte = b;
                    return;
                }
                this.midiReceiver.onNoteOff(this.channel, this.firstByte, b);
                this.firstByte = -1;
                return;
            case 2:
                if (this.firstByte < 0) {
                    this.firstByte = b;
                    return;
                }
                this.midiReceiver.onNoteOn(this.channel, this.firstByte, b);
                this.firstByte = -1;
                return;
            case 3:
                if (this.firstByte < 0) {
                    this.firstByte = b;
                    return;
                }
                this.midiReceiver.onPolyAftertouch(this.channel, this.firstByte, b);
                this.firstByte = -1;
                return;
            case DecayTypes.EXPONENTIAL_DELAY /*4*/:
                if (this.firstByte < 0) {
                    this.firstByte = b;
                    return;
                }
                this.midiReceiver.onControlChange(this.channel, this.firstByte, b);
                this.firstByte = -1;
                return;
            case DecayTypes.NO_DECAY /*5*/:
                this.midiReceiver.onProgramChange(this.channel, b);
                return;
            case DecayTypes.RAPID_DECAY /*6*/:
                this.midiReceiver.onAftertouch(this.channel, b);
                return;
            case DecayTypes.SUPER_RAPID_DECAY /*7*/:
                if (this.firstByte < 0) {
                    this.firstByte = b;
                    return;
                }
                this.midiReceiver.onPitchBend(this.channel, ((b << 7) | this.firstByte) - 8192);
                this.firstByte = -1;
                return;
            default:
                this.midiReceiver.onRawByte((byte) b);
                return;
        }
    }

    public boolean beginBlock() {
        return this.midiReceiver.beginBlock();
    }

    public void endBlock() {
        this.midiReceiver.endBlock();
    }
}
