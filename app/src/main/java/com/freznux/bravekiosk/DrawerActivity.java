package com.freznux.bravekiosk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class DrawerActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUI();
    }

    private void buildUI() {
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#1e293b"));
        mainLayout.setPadding(40, 40, 40, 40);

        TextView header = new TextView(this);
        header.setText("Allowed Applications");
        header.setTextColor(Color.WHITE);
        header.setTextSize(24f);
        header.setPadding(0, 0, 0, 40);
        mainLayout.addView(header);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout appList = new LinearLayout(this);
        appList.setOrientation(LinearLayout.VERTICAL);

        PackageManager pm = getPackageManager();
        for (String pkg : getAllowedApps()) {
            try {
                ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
                String appName = pm.getApplicationLabel(info).toString();
                
                Button btn = new Button(this);
                btn.setText(appName);
                btn.setBackgroundColor(Color.parseColor("#3b82f6"));
                btn.setTextColor(Color.WHITE);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
                params.setMargins(0, 0, 0, 20);
                btn.setLayoutParams(params);
                
                btn.setOnClickListener(v -> {
                    KioskLogger.log("Launching: " + pkg);
                    Intent intent = pm.getLaunchIntentForPackage(pkg);
                    if (intent != null) startActivity(intent);
                });
                appList.addView(btn);
            } catch (Exception e) {
                KioskLogger.log("App not installed: " + pkg);
            }
        }

        // --- Action Buttons ---
        Button refreshBtn = new Button(this);
        refreshBtn.setText("🔄 Refresh Dashboard");
        refreshBtn.setBackgroundColor(Color.parseColor("#10b981"));
        refreshBtn.setTextColor(Color.WHITE);
        refreshBtn.setOnClickListener(v -> {
            KioskLogger.log("Manual Refresh Triggered");
            startActivity(new Intent(this, SplashActivity.class));
            finish();
        });
        
        Button logsBtn = new Button(this);
        logsBtn.setText("📋 View System Logs");
        logsBtn.setBackgroundColor(Color.parseColor("#f59e0b"));
        logsBtn.setTextColor(Color.WHITE);
        logsBtn.setOnClickListener(v -> startActivity(new Intent(this, LogsActivity.class)));

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(-1, -2);
        btnParams.setMargins(0, 40, 0, 20);
        refreshBtn.setLayoutParams(btnParams);
        logsBtn.setLayoutParams(btnParams);

        appList.addView(refreshBtn);
        appList.addView(logsBtn);
        
        scrollView.addView(appList);
        mainLayout.addView(scrollView);
        setContentView(mainLayout);
    }

    private List<String> getAllowedApps() {
        List<String> list = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences("KioskConfig", MODE_PRIVATE);
        String json = prefs.getString("json_data", "{\"allowed_apps\":[\"com.brave.browser\"]}");
        try {
            JSONArray arr = new JSONObject(json).getJSONArray("allowed_apps");
            for (int i=0; i<arr.length(); i++) list.add(arr.getString(i));
        } catch (Exception e) {
            KioskLogger.log("JSON Parse Error: " + e.getMessage());
        }
        return list;
    }
}
