package com.freznux.bravekiosk;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
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
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class DrawerActivity extends Activity {
    private static final int CAMERA_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildAestheticUI();
    }

    // THE FIX: Neutralize the Back Button (ONLY ONCE!)
    @Override
    public void onBackPressed() {
        // Do absolutely nothing. The user is trapped.
    }

    private void buildAestheticUI() {
        SharedPreferences prefs = getSharedPreferences("KioskConfig", MODE_PRIVATE);
        String kioskName = prefs.getString("kiosk_name", "Saif M9 Kiosk");
    
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#0f172a"));
        mainLayout.setPadding(40, 60, 40, 40);

        TextView header = new TextView(this);
        header.setText(kioskName);
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
                img.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
                
                TextView name = new TextView(this);
                name.setText(appName);
                name.setTextColor(Color.parseColor("#e2e8f0"));
                name.setTextSize(13f);
                name.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                name.setGravity(Gravity.CENTER);
                name.setSingleLine(true);
                name.setEllipsize(TextUtils.TruncateAt.END);
                name.setPadding(0, 15, 0, 0);
                
                appItem.addView(img);
                appItem.addView(name);
                
                appItem.setOnClickListener(v -> {
                    try {
                        Intent intent = pm.getLaunchIntentForPackage(pkg);
                        if (intent != null) startActivity(intent);
                    } catch (Exception e) {}
                });
                grid.addView(appItem);
            } catch (Exception e) {}
        }
        scrollContent.addView(grid);

        // ACTION BUTTONS
        LinearLayout actionsLayout = new LinearLayout(this);
        actionsLayout.setOrientation(LinearLayout.VERTICAL);
        actionsLayout.setPadding(20, 60, 20, 20);

        boolean isOnline = prefs.getBoolean("server_online", false);
        String currentIp = prefs.getString("server_ip", "Not Set");

        if (isOnline) {
            actionsLayout.addView(createActionButton("📷 Change Server IP (" + currentIp + ")", "#3b82f6", v -> startQRScanner()));
        } else {
            actionsLayout.addView(createActionButton("⚠️ Server Offline - Change IP", "#dc2626", v -> startQRScanner()));
        }

        actionsLayout.addView(createActionButton("🧹 Clear Recents & Optimize", "#059669", v -> clearRecents()));
        actionsLayout.addView(createActionButton("🚪 Exit Kiosk Mode", "#ef4444", v -> showExitDialog()));

        scrollContent.addView(actionsLayout);
        scrollView.addView(scrollContent);
        mainLayout.addView(scrollView);
        setContentView(mainLayout);
    }

    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Enter Admin PIN to Exit");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setGravity(Gravity.CENTER);
        input.setTextColor(Color.WHITE);
        builder.setView(input);

        builder.setPositiveButton("Exit", (dialog, which) -> {
            if (input.getText().toString().equals("0192")) {
                getSharedPreferences("KioskConfig", MODE_PRIVATE).edit().putBoolean("kiosk_paused", true).apply();
                Toast.makeText(DrawerActivity.this, "Kiosk Paused. Change Default Launcher in Settings.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(android.provider.Settings.ACTION_HOME_SETTINGS));
            } else {
                Toast.makeText(DrawerActivity.this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
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

    private void startQRScanner() {
        try {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            } else { launchZxing(); }
        } catch (Exception e) {}
    }

    private void launchZxing() {
        try {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setPrompt("Scan Server QR Code");
            integrator.setOrientationLocked(false);
            integrator.initiateScan();
        } catch (Exception e) { Toast.makeText(this, "Camera error", Toast.LENGTH_SHORT).show(); }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null && result.getContents() != null) {
                getSharedPreferences("KioskConfig", MODE_PRIVATE).edit().putString("server_ip", result.getContents()).apply();
                startActivity(new Intent(this, SplashActivity.class));
                finish();
            } else { super.onActivityResult(requestCode, resultCode, data); }
        } catch (Exception e) {}
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
        try {
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
        } catch (Exception e) {}
    }
}
