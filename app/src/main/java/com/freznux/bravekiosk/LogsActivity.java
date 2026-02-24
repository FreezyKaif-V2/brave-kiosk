package com.freznux.bravekiosk;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

public class LogsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.BLACK);
        
        TextView logText = new TextView(this);
        logText.setText(KioskLogger.logs.toString());
        logText.setTextColor(Color.GREEN);
        logText.setPadding(20, 20, 20, 20);
        logText.setTextSize(14f);
        
        scroll.addView(logText);
        setContentView(scroll);
    }
}
