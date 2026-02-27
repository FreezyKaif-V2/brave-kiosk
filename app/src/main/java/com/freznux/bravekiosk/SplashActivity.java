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
import android.provider.Settings;
import android.view.Gravity;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SharedPreferences prefs = getSharedPreferences("KioskConfig", MODE_PRIVATE);
        prefs.edit().putBoolean("kiosk_paused", false).apply(); 
        
        checkAndRequestDefaultLauncher();
        forceReviveAccessibility(); // THE NEW SHIELD
        
        String kioskName = prefs.getString("kiosk_name", "Saif M9 Kiosk");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#0f172a"), Color.parseColor("#020617")});
        layout.setBackground(bg);
        
        final TextView title = new TextView(this);
        title.setText(kioskName);
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

        title.setScaleX(1.6f); title.setScaleY(1.6f); title.setAlpha(0f);
        tagline.setScaleX(1.6f); tagline.setScaleY(1.6f); tagline.setAlpha(0f);
        loader.setAlpha(0f);

        title.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(800).setInterpolator(new OvershootInterpolator(0.8f)).start();
        tagline.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(800).setInterpolator(new DecelerateInterpolator()).start();
        loader.animate().alpha(1f).setDuration(800).start();

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

    // THE FIX 1: Neutralize the Back Button
    @Override
    public void onBackPressed() {}

    // THE FIX 2: Force Accessibility On via Core Settings
    private void forceReviveAccessibility() {
        try {
            String service = "com.freznux.bravekiosk/com.freznux.bravekiosk.AppBlockerService";
            String enabledServices = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            
            if (enabledServices == null || !enabledServices.contains(service)) {
                Settings.Secure.putString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, service);
                Settings.Secure.putString(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, "1");
            }
        } catch (Exception e) {
            // Fails silently if ADB permission isn't granted yet
        }
    }

    private void checkAndRequestDefaultLauncher() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        android.content.pm.ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY);
        
        if (resolveInfo != null && !getPackageName().equals(resolveInfo.activityInfo.packageName)) {
            Toast.makeText(this, "Locking in! Please set Kiosk as Default Home App.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(android.provider.Settings.ACTION_HOME_SETTINGS));
        }
    }

    private void fetchConfigFromPi() {
        new Thread(() -> {
            SharedPreferences prefs = getSharedPreferences("KioskConfig", MODE_PRIVATE);
            try {
                String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                String baseIp = prefs.getString("server_ip", "http://127.0.0.1:5000");
                String piUrl = baseIp + "/api/config?device_id=" + deviceId;
                
                URL url = new URL(piUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(1000); 
                conn.setReadTimeout(1000);
                
                if (conn.getResponseCode() == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) response.append(line);
                    in.close();
                    
                    JSONObject json = new JSONObject(response.toString());
                    String updatedName = json.optString("kiosk_name", "Saif M9 Kiosk");
                    
                    prefs.edit().putString("json_data", response.toString())
                         .putString("kiosk_name", updatedName)
                         .putBoolean("server_online", true).apply();
                } else {
                    prefs.edit().putBoolean("server_online", false).apply();
                }
            } catch (Exception e) {
                prefs.edit().putBoolean("server_online", false).apply();
            }
        }).start();
    }
}
