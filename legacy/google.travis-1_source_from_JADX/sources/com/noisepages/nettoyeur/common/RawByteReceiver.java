package com.noisepages.nettoyeur.common;

public interface RawByteReceiver {
    boolean beginBlock();

    void endBlock();

    void onBytesReceived(int i, byte[] bArr);
}
