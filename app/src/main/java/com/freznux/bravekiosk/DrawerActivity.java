package com.freznux.bravekiosk;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
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
        mainLayout.setPadding(40, 60, 40, 40);

        TextView header = new TextView(this);
        header.setText("Study Tools");
        header.setTextColor(Color.WHITE);
        header.setTextSize(34f);
        header.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        header.setGravity(Gravity.CENTER);
        header.setPadding(0, 0, 0, 50);
        mainLayout.addView(header);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout scrollContent = new LinearLayout(this);
        scrollContent.setOrientation(LinearLayout.VERTICAL);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        grid.setAlignmentMode(GridLayout.ALIGN_MARGINS);
        grid.setUseDefaultMargins(true);

        PackageManager pm = getPackageManager();
        for (String pkg : getAllowedApps()) {
            try {
                ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
                String appName = pm.getApplicationLabel(info).toString();
                Drawable icon = pm.getApplicationIcon(info);
                
                LinearLayout appItem = new LinearLayout(this);
                appItem.setOrientation(LinearLayout.VERTICAL);
                appItem.setGravity(Gravity.CENTER);
                
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                params.setMargins(10, 20, 10, 40);
                appItem.setLayoutParams(params);
                
                ImageView img = new ImageView(this);
                img.setImageDrawable(icon);
                // SHRUNK ICONS for crispness (100x100 instead of 140x140)
                img.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
                
                TextView name = new TextView(this);
                name.setText(appName);
                name.setTextColor(Color.parseColor("#e2e8f0")); // Softer white
                name.setTextSize(13f);
                name.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                name.setGravity(Gravity.CENTER);
                name.setSingleLine(true);
                name.setEllipsize(TextUtils.TruncateAt.END);
                name.setPadding(0, 15, 0, 0);
                
                appItem.addView(img);
                appItem.addView(name);
                
                appItem.setOnClickListener(v -> {
                    Intent intent = pm.getLaunchIntentForPackage(pkg);
                    if (intent != null) startActivity(intent);
                });
                grid.addView(appItem);
            } catch (Exception e) {}
        }
        scrollContent.addView(grid);

        // ACTION BUTTONS
        LinearLayout actionsLayout = new LinearLayout(this);
        actionsLayout.setOrientation(LinearLayout.VERTICAL);
        actionsLayout.setPadding(20, 60, 20, 20);

        actionsLayout.addView(createActionButton("🧹 Clear Recents & Optimize", "#ef4444", v -> clearRecents()));
        actionsLayout.addView(createActionButton("🔄 Refresh Dashboard", "#059669", v -> {
            startActivity(new Intent(this, SplashActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }));
        actionsLayout.addView(createActionButton("📋 View System Logs", "#d97706", v -> {
            startActivity(new Intent(this, LogsActivity.class));
        }));

        scrollContent.addView(actionsLayout);
        scrollView.addView(scrollContent);
        mainLayout.addView(scrollView);
        setContentView(mainLayout);
    }

    private TextView createActionButton(String text, String colorHex, android.view.View.OnClickListener listener) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(16f);
        btn.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(0, 35, 0, 35);
        
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(24f);
        shape.setColor(Color.parseColor(colorHex));
        btn.setBackground(shape);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, 30);
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

    private void clearRecents() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            List<ApplicationInfo> packages = getPackageManager().getInstalledApplications(0);
            for (ApplicationInfo packageInfo : packages) {
                if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) continue;
                if (packageInfo.packageName.equals(getPackageName())) continue;
                am.killBackgroundProcesses(packageInfo.packageName);
            }
            Toast.makeText(this, "Memory Optimized.", Toast.LENGTH_SHORT).show();
        }
    }
}
