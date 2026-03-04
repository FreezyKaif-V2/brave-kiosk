package com.freznux.bravekiosk;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

public class AppBlockerService extends AccessibilityService {
    
    private long lastActiveTime = System.currentTimeMillis();
    private long lastRedirectTime = 0;
    private static final long THIRTY_MINUTES = 30 * 60 * 1000;

    // NEW: When the service connects, immediately lock it as a Foreground Service
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        createNotificationChannel();
        
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, "KIOSK_CHANNEL");
        } else {
            builder = new Notification.Builder(this);
        }
        
        Notification notification = builder
                .setContentTitle("Study Kiosk Active")
                .setContentText("Enforcing allowed applications...")
                .setSmallIcon(android.R.drawable.ic_secure) // Native lock icon
                .setOngoing(true)
                .build();
                
        // This makes the OS treat the Kiosk exactly like a Call Recorder
        startForeground(192, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "KIOSK_CHANNEL", 
                    "Kiosk Enforcement Service", 
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        SharedPreferences prefs = getSharedPreferences("KioskConfig", MODE_PRIVATE);
        
        if (prefs.getBoolean("kiosk_paused", false)) return;

        long now = System.currentTimeMillis();
        if (now - lastActiveTime > THIRTY_MINUTES) {
            lastActiveTime = now;
            Intent intent = new Intent(this, SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return;
        }
        lastActiveTime = now; 

        CharSequence pkg = event.getPackageName();
        if (pkg == null) return;

        if (isAppBlocked(pkg.toString(), prefs)) {
            scoldAndKick();
            return;
        }

        String pkgStr = pkg.toString();
        if (pkgStr.equals("com.brave.browser") || pkgStr.equals("com.android.chrome")) {
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root != null) {
                List<AccessibilityNodeInfo> urlBars = root.findAccessibilityNodeInfosByViewId(pkgStr + ":id/url_bar");
                if (!urlBars.isEmpty() && urlBars.get(0).getText() != null) {
                    String urlStr = urlBars.get(0).getText().toString().toLowerCase();
                    if ((urlStr.contains("youtube.com") || urlStr.contains("youtu.be")) 
                         && !urlStr.contains("saifm9kiosk.netlify.app")) {
                        if (now - lastRedirectTime > 3000) {
                            lastRedirectTime = now;
                            breakBrowserLoop(pkgStr);
                        }
                    }
                }
            }
        }
    }

    private boolean isAppBlocked(String packageName, SharedPreferences prefs) {
        String json = prefs.getString("json_data", "{\"blocked_apps\":[\"com.android.settings\"]}");
        try {
            JSONArray arr = new JSONObject(json).getJSONArray("blocked_apps");
            for (int i=0; i<arr.length(); i++) {
                if (arr.getString(i).equals(packageName)) return true;
            }
        } catch (Exception e) {}
        return false;
    }

    private void scoldAndKick() {
        Toast.makeText(this, "are bhai padh na!", Toast.LENGTH_SHORT).show();
        performGlobalAction(GLOBAL_ACTION_BACK);
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void breakBrowserLoop(String browserPackage) {
        Toast.makeText(this, "Redirecting to Study Portal...", Toast.LENGTH_SHORT).show();
        Intent resetIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://saifm9kiosk.netlify.app"));
        resetIntent.setPackage(browserPackage);
        resetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(resetIntent);
    }

    @Override
    public void onInterrupt() {}
}
