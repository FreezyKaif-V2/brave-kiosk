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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
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
        KioskLogger.log("--- Splash Screen Started ---");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        
        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.parseColor("#0f172a"), Color.parseColor("#020617")}
        );
        layout.setBackground(bg);
        
        final TextView title = new TextView(this);
        title.setText("Saif M9 Kiosk");
        title.setTextColor(Color.WHITE);
        title.setTextSize(46f);
        title.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        title.setGravity(Gravity.CENTER);
        
        final TextView tagline = new TextView(this);
        tagline.setText("Padhai karle beta, tera hi bhala hai");
        tagline.setTextColor(Color.parseColor("#38bdf8"));
        tagline.setTextSize(18f);
        tagline.setTypeface(Typeface.create("sans-serif-medium", Typeface.ITALIC));
        tagline.setGravity(Gravity.CENTER);
        tagline.setPadding(0, 10, 0, 80);

        final ProgressBar loader = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        loader.setIndeterminate(true);
        loader.setLayoutParams(new LinearLayout.LayoutParams(400, 10));
        
        layout.addView(title);
        layout.addView(tagline);
        layout.addView(loader);
        setContentView(layout);

        // Animation Sequence
        title.setScaleX(1.6f); title.setScaleY(1.6f); title.setAlpha(0f);
        tagline.setScaleX(1.6f); tagline.setScaleY(1.6f); tagline.setAlpha(0f);
        loader.setAlpha(0f);

        title.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(800).setInterpolator(new OvershootInterpolator(0.8f)).start();
        tagline.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(800).setInterpolator(new DecelerateInterpolator()).start();
        loader.animate().alpha(1f).setDuration(800).start();

        // Start Network Fetch
        fetchConfigFromPi();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            title.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(400).setInterpolator(new AccelerateInterpolator()).start();
            tagline.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(400).setInterpolator(new AccelerateInterpolator()).start();
            loader.animate().alpha(0f).setDuration(300).start();
            
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent i = new Intent(SplashActivity.this, DrawerActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                overridePendingTransition(0, 0); 
                finish();
            }, 380);
        }, 1400); 
    }

    private void fetchConfigFromPi() {
        new Thread(() -> {
            try {
                String piUrl = "http://192.168.1.103:5000/api/config";
                KioskLogger.log("Fetching config from: " + piUrl);
                
                URL url = new URL(piUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000); // 3 seconds timeout
                conn.setReadTimeout(3000);
                
                int responseCode = conn.getResponseCode();
                KioskLogger.log("Server responded with code: " + responseCode);
                
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) { response.append(line); }
                    in.close();
                    
                    getSharedPreferences("KioskConfig", MODE_PRIVATE).edit().putString("json_data", response.toString()).apply();
                    KioskLogger.log("Config successfully updated and saved.");
                } else {
                    KioskLogger.log("Fetch failed. Using cached configuration.");
                }
            } catch (Exception e) {
                KioskLogger.log("Network Exception: " + e.getMessage());
            }
        }).start();
    }
}
