package com.noisepages.nettoyeur.usb.midi;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import com.noisepages.nettoyeur.common.RawByteReceiver;
import com.noisepages.nettoyeur.midi.FromWireConverter;
import com.noisepages.nettoyeur.midi.MidiDevice;
import com.noisepages.nettoyeur.midi.MidiReceiver;
import com.noisepages.nettoyeur.midi.ToWireConverter;
import com.noisepages.nettoyeur.usb.ConnectionFailedException;
import com.noisepages.nettoyeur.usb.DeviceNotConnectedException;
import com.noisepages.nettoyeur.usb.InterfaceNotAvailableException;
import com.noisepages.nettoyeur.usb.UsbDeviceWithInfo;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@TargetApi(12)
public class UsbMidiDevice extends UsbDeviceWithInfo implements MidiDevice {
    private static final int[] midiPayloadSize = new int[]{-1, -1, 2, 3, 3, 1, 2, 3, 3, 3, 3, 3, 2, 2, 3, 1};
    private UsbDeviceConnection connection = null;
    private final List<UsbMidiInterface> interfaces = new ArrayList();

    public class UsbMidiInput {
        private final ConcurrentMap<Integer, FromWireConverter> converters;
        private final UsbInterface iface;
        private final UsbEndpoint inputEndpoint;
        private volatile Thread inputThread;

        /* renamed from: com.noisepages.nettoyeur.usb.midi.UsbMidiDevice$UsbMidiInput$1 */
        class C00051 extends Thread {
            private final byte[] inputBuffer;
            private final byte[] tmpBuffer = new byte[3];

            C00051() {
                this.inputBuffer = new byte[UsbMidiInput.this.inputEndpoint.getMaxPacketSize()];
            }

            public void run() {
                while (!C00051.interrupted()) {
                    int nRead = UsbMidiDevice.this.connection.bulkTransfer(UsbMidiInput.this.inputEndpoint, this.inputBuffer, this.inputBuffer.length, 50);
                    for (int i = 0; i < nRead; i += 4) {
                        int b = this.inputBuffer[i];
                        int cable = (b >> 4) & 15;
                        int n = UsbMidiDevice.midiPayloadSize[b & 15];
                        if (n >= 0) {
                            for (int j = 0; j < n; j++) {
                                this.tmpBuffer[j] = this.inputBuffer[(i + j) + 1];
                            }
                            convertBytes((FromWireConverter) UsbMidiInput.this.converters.get(Integer.valueOf(-1)), n);
                            convertBytes((FromWireConverter) UsbMidiInput.this.converters.get(Integer.valueOf(cable)), n);
                        }
                    }
                }
            }

            private void convertBytes(FromWireConverter converter, int n) {
                if (converter != null) {
                    converter.onBytesReceived(n, this.tmpBuffer);
                }
            }
        }

        private UsbMidiInput(UsbInterface iface, UsbEndpoint endpoint) {
            this.converters = new ConcurrentHashMap();
            this.inputThread = null;
            this.iface = iface;
            this.inputEndpoint = endpoint;
        }

        public String toString() {
            return "in:" + this.inputEndpoint;
        }

        public void setReceiver(MidiReceiver receiver) {
            setReceiverInternal(-1, receiver);
        }

        public void setReceiver(int cable, MidiReceiver receiver) {
            if (cable < 0 || cable > 15) {
                throw new IllegalArgumentException("Cable number out of range");
            }
            setReceiverInternal(cable, receiver);
        }

        private void setReceiverInternal(int cable, MidiReceiver receiver) {
            if (receiver != null) {
                this.converters.put(Integer.valueOf(cable), new FromWireConverter(receiver));
            } else {
                this.converters.remove(Integer.valueOf(cable));
            }
        }

        public void start() throws DeviceNotConnectedException, InterfaceNotAvailableException {
            if (UsbMidiDevice.this.connection == null) {
                throw new DeviceNotConnectedException();
            }
            stop();
            if (UsbMidiDevice.this.connection.claimInterface(this.iface, true)) {
                this.inputThread = new C00051();
                this.inputThread.start();
                return;
            }
            throw new InterfaceNotAvailableException();
        }

        public void stop() {
            if (this.inputThread != null) {
                this.inputThread.interrupt();
                try {
                    this.inputThread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                this.inputThread = null;
            }
        }
    }

    public class UsbMidiInterface {
        private final UsbInterface iface;
        private final List<UsbMidiInput> inputs;
        private final List<UsbMidiOutput> outputs;

        private UsbMidiInterface(UsbInterface iface, List<UsbMidiInput> inputs, List<UsbMidiOutput> outputs) {
            this.iface = iface;
            this.inputs = inputs;
            this.outputs = outputs;
        }

        private UsbInterface getInterface() {
            return this.iface;
        }

        public String toString() {
            return this.iface.toString();
        }

        public List<UsbMidiInput> getInputs() {
            return Collections.unmodifiableList(this.inputs);
        }

        public List<UsbMidiOutput> getOutputs() {
            return Collections.unmodifiableList(this.outputs);
        }

        public void stop() {
            for (UsbMidiInput input : this.inputs) {
                input.stop();
            }
        }
    }

    public class UsbMidiOutput {
        private volatile int cable;
        private final UsbInterface iface;
        private final byte[] outBuffer;
        private final UsbEndpoint outputEndpoint;
        private final ToWireConverter toWire;

        /* renamed from: com.noisepages.nettoyeur.usb.midi.UsbMidiDevice$UsbMidiOutput$1 */
        class C00911 implements RawByteReceiver {
            private boolean inBlock = false;
            private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int writeIndex = 0;

            C00911() {
            }

            public synchronized void onBytesReceived(int nBytes, byte[] buffer) {
                if (UsbMidiDevice.this.connection != null) {
                    int start = 0;
                    while (start < nBytes) {
                        int end = start + 1;
                        while (end < nBytes) {
                            if (buffer[end] != (byte) -9 && buffer[end] < (byte) 0) {
                                break;
                            }
                            end++;
                        }
                        processChunk(buffer, start, end);
                        start = end;
                    }
                    transfer();
                }
            }

            private void processChunk(byte[] buffer, int start, int end) {
                int cin = (buffer[start] >> 4) & 15;
                int start2;
                byte[] access$0;
                int i;
                if (cin < 8 || cin >= 15 || end - start != UsbMidiDevice.midiPayloadSize[cin]) {
                    while (true) {
                        start2 = start;
                        if (start2 >= end) {
                            start = start2;
                            return;
                        }
                        access$0 = UsbMidiOutput.this.outBuffer;
                        i = this.writeIndex;
                        this.writeIndex = i + 1;
                        access$0[i] = (byte) (UsbMidiOutput.this.cable | 15);
                        access$0 = UsbMidiOutput.this.outBuffer;
                        i = this.writeIndex;
                        this.writeIndex = i + 1;
                        start = start2 + 1;
                        access$0[i] = buffer[start2];
                        access$0 = UsbMidiOutput.this.outBuffer;
                        i = this.writeIndex;
                        this.writeIndex = i + 1;
                        access$0[i] = (byte) 0;
                        access$0 = UsbMidiOutput.this.outBuffer;
                        i = this.writeIndex;
                        this.writeIndex = i + 1;
                        access$0[i] = (byte) 0;
                        transferIfFull();
                    }
                } else {
                    access$0 = UsbMidiOutput.this.outBuffer;
                    i = this.writeIndex;
                    this.writeIndex = i + 1;
                    access$0[i] = (byte) (UsbMidiOutput.this.cable | cin);
                    start2 = start;
                    while (start2 < end) {
                        access$0 = UsbMidiOutput.this.outBuffer;
                        i = this.writeIndex;
                        this.writeIndex = i + 1;
                        start = start2 + 1;
                        access$0[i] = buffer[start2];
                        start2 = start;
                    }
                    while ((this.writeIndex & 3) != 0) {
                        access$0 = UsbMidiOutput.this.outBuffer;
                        i = this.writeIndex;
                        this.writeIndex = i + 1;
                        access$0[i] = (byte) 0;
                    }
                    transferIfFull();
                    start = start2;
                }
            }

            private void transferIfFull() {
                if (this.writeIndex >= UsbMidiOutput.this.outBuffer.length) {
                    transfer();
                }
            }

            private void transfer() {
                if (this.inBlock) {
                    this.outputStream.write(UsbMidiOutput.this.outBuffer, 0, this.writeIndex);
                } else {
                    UsbMidiDevice.this.connection.bulkTransfer(UsbMidiOutput.this.outputEndpoint, UsbMidiOutput.this.outBuffer, this.writeIndex, 0);
                }
                this.writeIndex = 0;
            }

            public boolean beginBlock() {
                this.outputStream.reset();
                this.inBlock = true;
                return true;
            }

            public void endBlock() {
                if (this.inBlock) {
                    UsbMidiDevice.this.connection.bulkTransfer(UsbMidiOutput.this.outputEndpoint, this.outputStream.toByteArray(), this.outputStream.size(), 0);
                    return;
                }
                throw new IllegalStateException("Not in block mode");
            }
        }

        private UsbMidiOutput(UsbInterface iface, UsbEndpoint ep) {
            this.toWire = new ToWireConverter(new C00911());
            this.iface = iface;
            this.outputEndpoint = ep;
            this.outBuffer = new byte[ep.getMaxPacketSize()];
            setVirtualCable(0);
        }

        public String toString() {
            return "out:" + this.outputEndpoint;
        }

        public void setVirtualCable(int c) {
            this.cable = (c << 4) & 240;
        }

        public MidiReceiver getMidiOut() throws DeviceNotConnectedException, InterfaceNotAvailableException {
            if (UsbMidiDevice.this.connection == null) {
                throw new DeviceNotConnectedException();
            } else if (UsbMidiDevice.this.connection.claimInterface(this.iface, true)) {
                return this.toWire;
            } else {
                throw new InterfaceNotAvailableException();
            }
        }
    }

    public static List<UsbMidiDevice> getMidiDevices(Context context) {
        List<UsbMidiDevice> midiDevices = new ArrayList();
        for (UsbDevice device : ((UsbManager) context.getSystemService("usb")).getDeviceList().values()) {
            UsbMidiDevice midiDevice = new UsbMidiDevice(device);
            if (!midiDevice.getInterfaces().isEmpty()) {
                midiDevices.add(midiDevice);
            }
        }
        return midiDevices;
    }

    private UsbMidiInterface asMidiInterface(UsbInterface iface) {
        List<UsbMidiInput> inputs = new ArrayList();
        List<UsbMidiOutput> outputs = new ArrayList();
        int epCount = iface.getEndpointCount();
        for (int j = 0; j < epCount; j++) {
            UsbEndpoint ep = iface.getEndpoint(j);
            if ((ep.getType() & 3) == 2 && (ep.getMaxPacketSize() & 3) == 0 && ep.getMaxPacketSize() > 0) {
                if ((ep.getDirection() & 128) == 128) {
                    inputs.add(new UsbMidiInput(iface, ep));
                } else {
                    outputs.add(new UsbMidiOutput(iface, ep));
                }
            }
        }
        if (inputs.isEmpty() && outputs.isEmpty()) {
            return null;
        }
        return new UsbMidiInterface(iface, inputs, outputs);
    }

    public static UsbMidiDevice asMidiDevice(UsbDevice device) {
        UsbMidiDevice midiDevice = new UsbMidiDevice(device);
        return !midiDevice.getInterfaces().isEmpty() ? midiDevice : null;
    }

    private UsbMidiDevice(UsbDevice device) {
        super(device);
        int ifaceCount = device.getInterfaceCount();
        for (int i = 0; i < ifaceCount; i++) {
            UsbMidiInterface iface = asMidiInterface(device.getInterface(i));
            if (iface != null) {
                this.interfaces.add(iface);
            }
        }
    }

    public List<UsbMidiInterface> getInterfaces() {
        return Collections.unmodifiableList(this.interfaces);
    }

    public synchronized void open(Context context) throws ConnectionFailedException {
        close();
        this.connection = ((UsbManager) context.getSystemService("usb")).openDevice(this.device);
        if (this.connection == null) {
            throw new ConnectionFailedException();
        }
    }

    public synchronized void close() {
        if (this.connection != null) {
            for (UsbMidiInterface iface : this.interfaces) {
                iface.stop();
                this.connection.releaseInterface(iface.getInterface());
            }
            this.connection.close();
            this.connection = null;
        }
    }

    public synchronized boolean isConnected() {
        return this.connection != null;
    }
}
