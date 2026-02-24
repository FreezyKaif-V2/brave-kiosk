package com.freznux.bravekiosk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
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
        buildAestheticUI();
    }

    private void buildAestheticUI() {
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#0f172a"));
        mainLayout.setPadding(60, 80, 60, 40);

        TextView header = new TextView(this);
        header.setText("Study Tools");
        header.setTextColor(Color.WHITE);
        header.setTextSize(28f);
        header.setTypeface(null, Typeface.BOLD);
        header.setPadding(0, 0, 0, 60);
        mainLayout.addView(header);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout appList = new LinearLayout(this);
        appList.setOrientation(LinearLayout.VERTICAL);

        PackageManager pm = getPackageManager();
        for (String pkg : getAllowedApps()) {
            try {
                ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
                String appName = pm.getApplicationLabel(info).toString();
                Drawable icon = pm.getApplicationIcon(info);
                
                // Create sleek card for each app
                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.HORIZONTAL);
                card.setGravity(Gravity.CENTER_VERTICAL);
                card.setPadding(40, 30, 40, 30);
                
                GradientDrawable shape = new GradientDrawable();
                shape.setCornerRadius(24f);
                shape.setColor(Color.parseColor("#1e293b")); // Lighter slate
                card.setBackground(shape);
                
                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
                cardParams.setMargins(0, 0, 0, 30);
                card.setLayoutParams(cardParams);
                
                ImageView img = new ImageView(this);
                img.setImageDrawable(icon);
                img.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
                
                TextView name = new TextView(this);
                name.setText(appName);
                name.setTextColor(Color.WHITE);
                name.setTextSize(20f);
                name.setPadding(40, 0, 0, 0);
                
                card.addView(img);
                card.addView(name);
                
                card.setOnClickListener(v -> {
                    KioskLogger.log("Launched: " + appName);
                    Intent intent = pm.getLaunchIntentForPackage(pkg);
                    if (intent != null) startActivity(intent);
                });
                appList.addView(card);
            } catch (Exception e) {
                KioskLogger.log("App not found: " + pkg);
            }
        }

        // Action Buttons at the bottom
        appList.addView(createActionButton("🔄 Refresh Dashboard", "#059669", v -> {
            startActivity(new Intent(this, SplashActivity.class));
            finish();
        }));
        
        appList.addView(createActionButton("📋 View System Logs", "#d97706", v -> {
            startActivity(new Intent(this, LogsActivity.class));
        }));

        scrollView.addView(appList);
        mainLayout.addView(scrollView);
        setContentView(mainLayout);
    }

    private TextView createActionButton(String text, String colorHex, android.view.View.OnClickListener listener) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(18f);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(0, 30, 0, 30);
        
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(24f);
        shape.setColor(Color.parseColor(colorHex));
        btn.setBackground(shape);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 30, 0, 0);
        btn.setLayoutParams(params);
        btn.setOnClickListener(listener);
        return btn;
    }

    private List<String> getAllowedApps() {
        List<String> list = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences("KioskConfig", MODE_PRIVATE);
        String json = prefs.getString("json_data", "{\"allowed_apps\":[\"com.brave.browser\"]}");
        try {
            JSONArray arr = new JSONObject(json).getJSONArray("allowed_apps");
            for (int i=0; i<arr.length(); i++) list.add(arr.getString(i));
        } catch (Exception e) {}
        return list;
    }
}
