package com.purplepip.odin.android;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.purplepip.odin.common.OdinException;
import com.purplepip.odin.music.MeasureProvider;
import com.purplepip.odin.music.StaticMeasureProvider;
import com.purplepip.odin.sequencer.OdinSequencer;
import com.purplepip.odin.sequencer.OdinSequencerConfiguration;
import com.purplepip.odin.sequencer.SequenceBuilder;
import com.purplepip.odin.series.DefaultMicrosecondPositionProvider;
import com.purplepip.odin.series.StaticBeatsPerMinute;
import com.purplepip.odin.series.Tick;

public class MainActivity extends AppCompatActivity {
    private AndroidMidiOperationReceiver androidMidiOperationReceiver;
    private TextView logWindow;

    public void log(String message) {
        logWindow = (TextView) findViewById(R.id.text);
        logWindow.append("\n" + message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        log("Testing");

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            log("MIDI enabled");
            MidiManager m = (MidiManager) getSystemService(Context.MIDI_SERVICE);
            MidiDeviceInfo[] infos = m.getDevices();
            log("MIDI device count : " + infos.length);

            for (MidiDeviceInfo info : infos) {
                log("" + info.toString());
            }
            try {
                startSequencer();
            } catch (OdinException e) {
                log("Cannot start sequencer " + e.getMessage());
            }
        } else {
            log("No MIDI");
        }

        log(" ... DONE");
    }

    @Override
    protected void onDestroy() {
        if (androidMidiOperationReceiver != null) {
            androidMidiOperationReceiver.close();
        }
    }

    private void startSequencer() throws OdinException {
        androidMidiOperationReceiver
                = new AndroidMidiOperationReceiver(this, this);
        MeasureProvider measureProvider = new StaticMeasureProvider(4);
        OdinSequencer sequencer = new OdinSequencer(
                    new OdinSequencerConfiguration()
                            .setBeatsPerMinute(new StaticBeatsPerMinute(120))
                            .setMeasureProvider(measureProvider)
                            .setOperationReceiver(androidMidiOperationReceiver)
                            .setMicrosecondPositionProvider(new DefaultMicrosecondPositionProvider()));

            new SequenceBuilder(sequencer, measureProvider)
                    .addMetronome()
                    .addPattern(Tick.BEAT, 2)
                    .withChannel(9) .withNote(42)   .addPattern(Tick.QUARTER, 61435)
                    .addPattern(Tick.EIGHTH, 127)
                    .withNote(46)   .addPattern(Tick.TWO_THIRDS, 7);

            sequencer.start();
    }
}
