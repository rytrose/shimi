package org.puredata.android.io;

import android.util.Log;
import mr1.robots.travis.moves.gestures.emotions.DecayTypes;
import org.puredata.android.utils.Properties;

public final class VersionedAudioFormat {
    private static final boolean hasEclair = (Properties.version >= 5);

    private static class FormatCupcake {
        private FormatCupcake() {
        }

        static {
            Log.i("Pd Version", "loading class for Cupcake");
        }

        static int getInFormat(int inChannels) {
            switch (inChannels) {
                case 1:
                    return 2;
                case 2:
                    return 3;
                default:
                    throw new IllegalArgumentException("illegal number of input channels: " + inChannels);
            }
        }

        static int getOutFormat(int outChannels) {
            switch (outChannels) {
                case 1:
                    return 2;
                case 2:
                    return 3;
                default:
                    throw new IllegalArgumentException("illegal number of output channels: " + outChannels);
            }
        }
    }

    private static class FormatEclair {
        private FormatEclair() {
        }

        static {
            Log.i("Pd Version", "loading class for Eclair");
        }

        static int getInFormat(int inChannels) {
            switch (inChannels) {
                case 1:
                    return 16;
                case 2:
                    return 12;
                default:
                    throw new IllegalArgumentException("illegal number of input channels: " + inChannels);
            }
        }

        static int getOutFormat(int outChannels) {
            switch (outChannels) {
                case 1:
                    return 4;
                case 2:
                    return 12;
                case DecayTypes.EXPONENTIAL_DELAY /*4*/:
                    return 204;
                case DecayTypes.RAPID_DECAY /*6*/:
                    return 252;
                case 8:
                    return 1020;
                default:
                    throw new IllegalArgumentException("illegal number of output channels: " + outChannels);
            }
        }
    }

    private VersionedAudioFormat() {
    }

    public static int getInFormat(int inChannels) {
        return hasEclair ? FormatEclair.getInFormat(inChannels) : FormatCupcake.getInFormat(inChannels);
    }

    public static int getOutFormat(int outChannels) {
        return hasEclair ? FormatEclair.getOutFormat(outChannels) : FormatCupcake.getOutFormat(outChannels);
    }
}
