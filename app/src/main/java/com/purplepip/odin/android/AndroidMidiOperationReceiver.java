package com.purplepip.odin.android;

import android.content.Context;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import com.purplepip.odin.common.OdinException;
import com.purplepip.odin.midi.RawMessage;
import com.purplepip.odin.sequencer.Operation;
import com.purplepip.odin.sequencer.OperationReceiver;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Android MIDI operation receiver.
 */
public class AndroidMidiOperationReceiver implements OperationReceiver {
    Set<MidiInputPort> inputPorts = new HashSet<>();
    private MainActivity logger;

    public AndroidMidiOperationReceiver(Context context, final MainActivity logger) {
        this.logger = logger;
        // For now lets connect to all input ports.
        MidiManager m = (MidiManager) context.getSystemService(Context.MIDI_SERVICE);
        logger.log("Connecting to MIDI ports");

        MidiDeviceInfo[] infos = m.getDevices();
        for (final MidiDeviceInfo info : infos) {
            m.openDevice(info, new MidiManager.OnDeviceOpenedListener() {
                @Override
                public void onDeviceOpened(MidiDevice device) {
                    if (device == null) {
                        Log.e(AndroidMidiOperationReceiver.class.getName(),
                                "could not open device " + info);
                    } else {
                        int i = 0;
                        for (MidiDeviceInfo.PortInfo portInfo : info.getPorts()) {
                            if (portInfo.getType() == MidiDeviceInfo.PortInfo.TYPE_INPUT) {
                                inputPorts.add(device.openInputPort(i));
                            }
                            i++;
                        }
                    }
                }
            }, new Handler(Looper.getMainLooper()));
       }
    }

    @Override
    public void send(Operation operation, long l) throws OdinException {
        if (logger != null) {
            //logger.log("Note " + operation.getNumber());
        }
        RawMessage message = new RawMessage(operation);
        for (MidiInputPort inputPort : inputPorts) {
            try {
                inputPort.send(message.getBytes(), 0, message.getLength());
            } catch (IOException e) {
                Log.e(AndroidMidiOperationReceiver.class.getName(),
                        "Cannot send MIDI message", e);
            }
        }
    }

    public void close() {
        for (MidiInputPort inputPort : inputPorts) {
            try {
                inputPort.close();
            } catch (IOException e) {
                Log.e(AndroidMidiOperationReceiver.class.getName(),
                        "Cannot close port", e);
            }
        }
        inputPorts.clear();
    }
}
