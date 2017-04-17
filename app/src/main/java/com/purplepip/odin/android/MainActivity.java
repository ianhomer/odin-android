package com.purplepip.odin.android;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView t = (TextView) findViewById(R.id.text);
        t.append("\nTesting");

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            t.append("\nMIDI enabled");
            MidiManager m = (MidiManager) getSystemService(Context.MIDI_SERVICE);
            MidiDeviceInfo[] infos = m.getDevices();
            t.append("\nMIDI device count : " + infos.length);

            for (MidiDeviceInfo info : infos) {
                t.append("\n" + info.toString());
            }
        } else {
            t.append("\nNo MIDI");
        }

        t.append("\n ... DONE");

    }
}
