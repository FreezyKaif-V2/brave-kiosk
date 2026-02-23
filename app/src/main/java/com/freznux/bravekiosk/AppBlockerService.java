package com.freznux.bravekiosk;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class AppBlockerService extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            CharSequence pkg = event.getPackageName();
            if (pkg != null && pkg.toString().equals("com.android.settings")) {
                
                // 1. Scold the user
                Toast.makeText(this, "Abe Chor, Dimag Mat Laga! Padh Padh Padh !!!", Toast.LENGTH_SHORT).show();
                
                // 2. Perform a global "Back" button press to kill the settings window
                performGlobalAction(GLOBAL_ACTION_BACK);

                // 3. Force Brave back to the front
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.brave.browser");
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    public void onInterrupt() {}
}
