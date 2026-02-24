package com.freznux.bravekiosk;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
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
        buildDrawerUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        buildDrawerUI(); // Refresh apps if config changed
    }

    private void buildDrawerUI() {
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

        List<String> allowedApps = getAllowedApps();
        PackageManager pm = getPackageManager();

        for (String pkg : allowedApps) {
            try {
                ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
                String appName = pm.getApplicationLabel(info).toString();
                
                Button btn = new Button(this);
                btn.setText(appName);
                btn.setBackgroundColor(Color.parseColor("#3b82f6"));
                btn.setTextColor(Color.WHITE);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, 20);
                btn.setLayoutParams(params);
                
                btn.setOnClickListener(v -> {
                    Intent launchIntent = pm.getLaunchIntentForPackage(pkg);
                    if (launchIntent != null) {
                        startActivity(launchIntent);
                    }
                });
                appList.addView(btn);
            } catch (Exception e) {}
        }

        // Add the Clear Recents Button
        Button clearBtn = new Button(this);
        clearBtn.setText("Clear Recents & Optimize");
        clearBtn.setBackgroundColor(Color.parseColor("#ef4444")); // Red button
        clearBtn.setTextColor(Color.WHITE);
        clearBtn.setOnClickListener(v -> clearRecents());
        
        LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        clearParams.setMargins(0, 80, 0, 0);
        clearBtn.setLayoutParams(clearParams);
        appList.addView(clearBtn);

        scrollView.addView(appList);
        mainLayout.addView(scrollView);
        setContentView(mainLayout);
    }

    private List<String> getAllowedApps() {
        List<String> list = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences("KioskConfig", MODE_PRIVATE);
        String json = prefs.getString("json_data", "{\"allowed_apps\":[\"com.brave.browser\"]}");
        try {
            JSONObject obj = new JSONObject(json);
            JSONArray arr = obj.getJSONArray("allowed_apps");
            for (int i=0; i<arr.length(); i++) {
                list.add(arr.getString(i));
            }
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
            Toast.makeText(this, "Memory optimized. Recents cleared.", Toast.LENGTH_SHORT).show();
        }
    }
}
