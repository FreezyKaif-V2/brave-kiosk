package com.freznux.bravekiosk;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class KioskLogger {
    public static StringBuilder logs = new StringBuilder("=== Kiosk Diagnostic Logs ===\n");
    public static void log(String msg) {
        String time = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
        logs.insert(30, "[" + time + "] " + msg + "\n");
    }
}
