package com.freznux.bravekiosk;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        SharedPreferences prefs = getSharedPreferences("KioskConfig", MODE_PRIVATE);
        
        // IF KIOSK IS EXITED/PAUSED, DO NOT BLOCK ANYTHING
        if (prefs.getBoolean("kiosk_paused", false)) {
            return;
        }

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

        if (pkg.toString().equals("com.brave.browser")) {
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root != null) {
                List<AccessibilityNodeInfo> urlBars = root.findAccessibilityNodeInfosByViewId("com.brave.browser:id/url_bar");
                if (!urlBars.isEmpty() && urlBars.get(0).getText() != null) {
                    String urlStr = urlBars.get(0).getText().toString().toLowerCase();
                    if ((urlStr.contains("youtube.com") || urlStr.contains("youtu.be")) 
                         && !urlStr.contains("saifm9kiosk.netlify.app")) {
                        if (now - lastRedirectTime > 3000) {
                            lastRedirectTime = now;
                            breakBraveLoop();
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

    private void breakBraveLoop() {
        Toast.makeText(this, "Redirecting to Study Portal...", Toast.LENGTH_SHORT).show();
        Intent resetIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://saifm9kiosk.netlify.app"));
        resetIntent.setPackage("com.brave.browser");
        resetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(resetIntent);
    }

    @Override
    public void onInterrupt() {}
}
