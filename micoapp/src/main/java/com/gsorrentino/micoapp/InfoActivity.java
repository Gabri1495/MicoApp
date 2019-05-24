package com.gsorrentino.micoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

/**
 * Semplice {@link android.app.Activity} riassuntiva
 */
public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        String version = BuildConfig.VERSION_NAME + "(" + BuildConfig.VERSION_CODE + ")";
        ((TextView) findViewById(R.id.info_version_textView)).setText(version);
    }
}
