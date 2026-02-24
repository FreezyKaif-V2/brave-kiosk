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
        KioskLogger.log("Splash Screen Started");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.parseColor("#0f172a"));
        
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
                // REPLACE YOUR_PI_IP BELOW
                String piUrl = "http://192.168.1.103:5000/api/config";
                KioskLogger.log("Attempting to fetch: " + piUrl);
                
                URL url = new URL(piUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                
                int status = conn.getResponseCode();
                KioskLogger.log("Server Response Code: " + status);
                
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) { response.append(line); }
                in.close();
                
                SharedPreferences prefs = getSharedPreferences("KioskConfig", MODE_PRIVATE);
                prefs.edit().putString("json_data", response.toString()).apply();
                KioskLogger.log("Successfully saved config to device.");
            } catch (Exception e) {
                KioskLogger.log("Network Error: " + e.getMessage());
            }
            
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent i = new Intent(SplashActivity.this, DrawerActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }, 2500);
        }).start();
    }
}
