package com.freznux.bravekiosk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
        
        // Deep modern gradient background
        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.parseColor("#0f172a"), Color.parseColor("#020617")}
        );
        layout.setBackground(bg);
        
        TextView title = new TextView(this);
        title.setText("Saif M9 Kiosk");
        title.setTextColor(Color.WHITE);
        title.setTextSize(42f);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        
        TextView tagline = new TextView(this);
        tagline.setText("Padhai karle beta, tera hi bhala hai");
        tagline.setTextColor(Color.parseColor("#38bdf8")); // Bright accent blue
        tagline.setTextSize(18f);
        tagline.setTypeface(null, Typeface.ITALIC);
        tagline.setGravity(Gravity.CENTER);
        tagline.setPadding(0, 10, 0, 80);

        // Sleek horizontal loading bar
        ProgressBar loader = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        loader.setIndeterminate(true);
        LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(400, 20);
        loader.setLayoutParams(pbParams);
        
        layout.addView(title);
        layout.addView(tagline);
        layout.addView(loader);
        setContentView(layout);

        fetchConfigFromPi();
    }

    private void fetchConfigFromPi() {
        new Thread(() -> {
            try {
                // REPLACE YOUR_PI_IP BELOW
                String piUrl = "http://192.168.1.103:5000/api/config";
                KioskLogger.log("Fetching: " + piUrl);
                
                URL url = new URL(piUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) { response.append(line); }
                in.close();
                
                SharedPreferences prefs = getSharedPreferences("KioskConfig", MODE_PRIVATE);
                prefs.edit().putString("json_data", response.toString()).apply();
                KioskLogger.log("Config updated successfully.");
            } catch (Exception e) {
                KioskLogger.log("Using cached config. Network error: " + e.getMessage());
            }
            
            // Wait to let the aesthetic loading animation play
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent i = new Intent(SplashActivity.this, DrawerActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }, 2500);
        }).start();
    }
}
