package com.freznux.bravekiosk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class KioskActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launchBrave();
    }

    @Override
    protected void onResume() {
        super.onResume();
        launchBrave();
    }

    private void launchBrave() {
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.brave.browser");
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
}
