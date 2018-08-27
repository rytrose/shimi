package org.puredata.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NativeLoader {
    private static String osArch = null;
    private static String osName = null;

    public static class NativeLibraryLoadError extends UnsatisfiedLinkError {
        private static final long serialVersionUID = 1;

        public NativeLibraryLoadError(String message) {
            super(message);
        }

        public NativeLibraryLoadError(String message, Throwable cause) {
            super(message);
            initCause(cause);
        }
    }

    static {
        detectSystem();
    }

    private static void detectSystem() {
        osArch = System.getProperty("os.arch").toLowerCase();
        if (osArch.indexOf("64") != -1) {
            osArch = "x86_64";
        } else if (osArch.indexOf("86") != -1) {
            osArch = "x86";
        }
        osName = System.getProperty("os.name").toLowerCase();
        if (osName.indexOf("linux") != -1) {
            osName = "linux";
        } else if (osName.indexOf("windows") != -1) {
            osName = "windows";
        } else if (osName.indexOf("mac") != -1) {
            osName = "mac";
        }
    }

    public static void loadLibrary(String library, String osNameCheck, String osArchCheck) {
        if (osArch.equals(osArchCheck)) {
            loadLibrary(library, osNameCheck);
        }
    }

    public static void loadLibrary(String library, String osNameCheck) {
        if (osName.equals(osNameCheck)) {
            loadLibrary(library);
        }
    }

    public static void loadLibrary(String library) {
        try {
            System.loadLibrary(library);
        } catch (UnsatisfiedLinkError e) {
            loadLibraryFromJar(library);
        }
    }

    private static void loadLibraryFromJar(String library) {
        library = System.mapLibraryName(library);
        InputStream in = PdBase.class.getResourceAsStream("natives/" + osName + "/" + osArch + "/" + library);
        if (in == null) {
            in = PdBase.class.getResourceAsStream("natives/" + osName + "/" + library);
        }
        if (in == null) {
            throw new NativeLibraryLoadError("Couldn't find " + library + " for this platform " + osName + "/" + osArch);
        }
        try {
            File fileOut = File.createTempFile(library.replaceFirst("\\.[^.]*$", ""), library.replaceFirst("^.*\\.", "."));
            OutputStream out = new FileOutputStream(fileOut);
            byte[] copyBuffer = new byte[1024];
            while (true) {
                int amountRead = in.read(copyBuffer);
                if (amountRead == -1) {
                    in.close();
                    out.close();
                    System.load(fileOut.toString());
                    fileOut.deleteOnExit();
                    return;
                }
                out.write(copyBuffer, 0, amountRead);
            }
        } catch (IOException error) {
            throw new NativeLibraryLoadError("Failed to save native library " + library + " to temporary file", error);
        }
    }
}
