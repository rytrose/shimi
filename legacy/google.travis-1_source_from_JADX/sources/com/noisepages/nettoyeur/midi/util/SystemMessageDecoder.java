package com.noisepages.nettoyeur.midi.util;

import java.io.ByteArrayOutputStream;
import mr1.robots.travis.moves.gestures.emotions.DecayTypes;

public class SystemMessageDecoder {
    /* renamed from: $SWITCH_TABLE$com$noisepages$nettoyeur$midi$util$SystemMessageDecoder$State */
    private static /* synthetic */ int[] f0xbebd6a95;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private int firstByte = -1;
    private final SystemMessageReceiver receiver;
    private State state = State.NONE;

    private enum State {
        SYSTEM_EXCLUSIVE,
        TIME_CODE,
        SONG_POSITION,
        SONG_SELECT,
        NONE
    }

    /* renamed from: $SWITCH_TABLE$com$noisepages$nettoyeur$midi$util$SystemMessageDecoder$State */
    static /* synthetic */ int[] m0xbebd6a95() {
        int[] iArr = f0xbebd6a95;
        if (iArr == null) {
            iArr = new int[State.values().length];
            try {
                iArr[State.NONE.ordinal()] = 5;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[State.SONG_POSITION.ordinal()] = 3;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[State.SONG_SELECT.ordinal()] = 4;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[State.SYSTEM_EXCLUSIVE.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[State.TIME_CODE.ordinal()] = 2;
            } catch (NoSuchFieldError e5) {
            }
            f0xbebd6a95 = iArr;
        }
        return iArr;
    }

    public SystemMessageDecoder(SystemMessageReceiver receiver) {
        this.receiver = receiver;
    }

    public boolean decodeByte(byte value) {
        switch (value) {
            case (byte) -16:
                this.buffer.reset();
                this.state = State.SYSTEM_EXCLUSIVE;
                return true;
            case (byte) -15:
                this.state = State.TIME_CODE;
                return true;
            case (byte) -14:
                this.firstByte = -1;
                this.state = State.SONG_POSITION;
                return true;
            case (byte) -13:
                this.state = State.SONG_SELECT;
                return true;
            case (byte) -10:
                this.receiver.onTuneRequest();
                this.state = State.NONE;
                return true;
            case (byte) -9:
                if (this.state == State.SYSTEM_EXCLUSIVE) {
                    this.receiver.onSystemExclusive(this.buffer.toByteArray());
                }
                this.state = State.NONE;
                return true;
            case (byte) -8:
                this.receiver.onTimingClock();
                return true;
            case (byte) -6:
                this.receiver.onStart();
                return true;
            case (byte) -5:
                this.receiver.onContinue();
                return true;
            case (byte) -4:
                this.receiver.onStop();
                return true;
            case (byte) -2:
                this.receiver.onActiveSensing();
                return true;
            case (byte) -1:
                this.receiver.onSystemReset();
                return true;
            default:
                if (value >= (byte) 0) {
                    switch (m0xbebd6a95()[this.state.ordinal()]) {
                        case 1:
                            this.buffer.write(value);
                            return true;
                        case 2:
                            this.receiver.onTimeCode(value);
                            this.state = State.NONE;
                            return true;
                        case 3:
                            if (this.firstByte < 0) {
                                this.firstByte = value;
                                return true;
                            }
                            this.receiver.onSongPosition((value << 7) | this.firstByte);
                            this.state = State.NONE;
                            return true;
                        case DecayTypes.EXPONENTIAL_DELAY /*4*/:
                            this.receiver.onSongSelect(value);
                            this.state = State.NONE;
                            return true;
                        default:
                            return false;
                    }
                }
                this.state = State.NONE;
                return false;
        }
    }
}
