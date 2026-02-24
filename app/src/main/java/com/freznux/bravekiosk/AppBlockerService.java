package com.freznux.bravekiosk;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import java.util.List;

public class AppBlockerService extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        CharSequence pkg = event.getPackageName();
        if (pkg == null) return;

        // 1. Block the Settings App
        if (pkg.toString().equals("com.android.settings")) {
            scoldAndKick();
            return;
        }

        // 2. Block YouTube inside Brave Browser
        if (pkg.toString().equals("com.brave.browser")) {
            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root != null) {
                // Find Brave's URL bar text
                List<AccessibilityNodeInfo> urlBars = root.findAccessibilityNodeInfosByViewId("com.brave.browser:id/url_bar");
                if (!urlBars.isEmpty()) {
                    CharSequence url = urlBars.get(0).getText();
                    if (url != null) {
                        String urlStr = url.toString().toLowerCase();
                        
                        // Trigger if they are on YouTube (Modify "your-kiosk-domain" to your actual site)
                        if ((urlStr.contains("youtube.com") || urlStr.contains("youtu.be")) 
                             && !urlStr.contains("your-kiosk-domain.com")) {
                            scoldAndKick();
                        }
                    }
                }
            }
        }
    }

    private void scoldAndKick() {
        Toast.makeText(this, "are bhai padh na!", Toast.LENGTH_SHORT).show();
        performGlobalAction(GLOBAL_ACTION_BACK);

        Intent intent = getPackageManager().getLaunchIntentForPackage("com.brave.browser");
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    public void onInterrupt() {}
}
