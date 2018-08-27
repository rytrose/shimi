package com.noisepages.nettoyeur.bluetooth.midi;

import com.noisepages.nettoyeur.bluetooth.BluetoothDisabledException;
import com.noisepages.nettoyeur.bluetooth.BluetoothSppConnection;
import com.noisepages.nettoyeur.bluetooth.BluetoothSppConnection.State;
import com.noisepages.nettoyeur.bluetooth.BluetoothSppObserver;
import com.noisepages.nettoyeur.bluetooth.BluetoothUnavailableException;
import com.noisepages.nettoyeur.common.RawByteReceiver;
import com.noisepages.nettoyeur.midi.FromWireConverter;
import com.noisepages.nettoyeur.midi.MidiDevice;
import com.noisepages.nettoyeur.midi.MidiReceiver;
import com.noisepages.nettoyeur.midi.ToWireConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BluetoothMidiDevice implements MidiDevice {
    private final BluetoothSppConnection btConnection;
    private final ToWireConverter toWire = new ToWireConverter(new C00901());

    /* renamed from: com.noisepages.nettoyeur.bluetooth.midi.BluetoothMidiDevice$1 */
    class C00901 implements RawByteReceiver {
        private boolean inBlock = false;
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        C00901() {
        }

        public void onBytesReceived(int nBytes, byte[] buffer) {
            if (this.inBlock) {
                this.outputStream.write(buffer, 0, nBytes);
                return;
            }
            try {
                BluetoothMidiDevice.this.btConnection.write(buffer, 0, nBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public boolean beginBlock() {
            this.outputStream.reset();
            this.inBlock = true;
            return true;
        }

        public void endBlock() {
            if (this.inBlock) {
                try {
                    BluetoothMidiDevice.this.btConnection.write(this.outputStream.toByteArray(), 0, this.outputStream.size());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.inBlock = false;
                return;
            }
            throw new IllegalStateException("Not in block mode");
        }
    }

    public BluetoothMidiDevice(BluetoothSppObserver observer, MidiReceiver receiver) throws BluetoothUnavailableException, BluetoothDisabledException {
        this.btConnection = new BluetoothSppConnection(observer, new FromWireConverter(receiver), 64);
    }

    public void connect(String addr) throws IOException {
        this.btConnection.connect(addr);
    }

    public void close() {
        this.btConnection.stop();
    }

    public MidiReceiver getMidiOut() {
        return this.toWire;
    }

    public State getConnectionState() {
        return this.btConnection.getConnectionState();
    }
}
