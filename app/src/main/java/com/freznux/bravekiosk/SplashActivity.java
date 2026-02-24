package com.freznux.bravekiosk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Build the sleek UI programmatically
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.parseColor("#0f172a")); // Sleek dark blue
        
        TextView title = new TextView(this);
        title.setText("Saif M9 Kiosk");
        title.setTextColor(Color.WHITE);
        title.setTextSize(36f);
        title.setGravity(Gravity.CENTER);
        
        TextView tagline = new TextView(this);
        tagline.setText("Padhai karle beta, tera hi bhala hai");
        tagline.setTextColor(Color.parseColor("#94a3b8"));
        tagline.setTextSize(18f);
        tagline.setGravity(Gravity.CENTER);
        tagline.setPadding(0, 20, 0, 0);
        
        layout.addView(title);
        layout.addView(tagline);
        setContentView(layout);

        fetchConfigFromPi();
    }

    private void fetchConfigFromPi() {
        new Thread(() -> {
            try {
                // REPLACE 'YOUR_PI_IP' WITH YOUR ACTUAL PI IP ADDRESS BEFORE RUNNING THIS SCRIPT
                URL url = new URL("http://192.168.1.103:5000/api/config");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) { response.append(line); }
                in.close();
                
                SharedPreferences prefs = getSharedPreferences("KioskConfig", MODE_PRIVATE);
                prefs.edit().putString("json_data", response.toString()).apply();
            } catch (Exception e) {
                // Ignore failure, we will just use the last cached version
            }
            
            // Wait 3 seconds to let them read the scolding, then return to Drawer
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent i = new Intent(SplashActivity.this, DrawerActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }, 3000);
        }).start();
    }
}
