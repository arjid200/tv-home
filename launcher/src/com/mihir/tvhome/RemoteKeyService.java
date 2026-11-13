package com.mihir.tvhome;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

public class RemoteKeyService extends AccessibilityService {
    private static final String TAG = "RemoteKeyService";
    private long lastTriggerTime = 0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {}

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        int code = event.getKeyCode();
        int action = event.getAction();

        if (action == KeyEvent.ACTION_DOWN) {
            long now = System.currentTimeMillis();
            if (now - lastTriggerTime < 500) {
                // Debounce rapid repeats
                return true;
            }

            // Settings button (Sony remote gear icon / KEYCODE_SETTINGS / QUICK_SETTINGS / BUTTON_10)
            if (code == KeyEvent.KEYCODE_SETTINGS 
                    || code == 176 
                    || code == 197 /* KEYCODE_BUTTON_10 on Sony SmartUI */
                    || code == 312 /* KEYCODE_QUICK_SETTINGS */
                    || code == KeyEvent.KEYCODE_MENU) {
                lastTriggerTime = now;
                Log.i(TAG, "Opening settings from key: " + code);
                Intent s = new Intent(Settings.ACTION_SETTINGS);
                s.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(s);
                return true;
            }

            // Dedicated YouTube button (Sony remote maps to 201 [BUTTON_14] or 172 [GUIDE])
            if (code == 201 /* KEYCODE_BUTTON_14 */ 
                    || code == KeyEvent.KEYCODE_GUIDE 
                    || code == 172) {
                lastTriggerTime = now;
                Log.i(TAG, "Launching YouTube from key: " + code);
                launchPackage("com.google.android.youtube.tv");
                return true;
            }

            // Dedicated Netflix button (Sony remote maps to 190 [BUTTON_3])
            if (code == 190 /* KEYCODE_BUTTON_3 */) {
                lastTriggerTime = now;
                Log.i(TAG, "Launching Netflix from key: " + code);
                launchPackage("com.netflix.ninja");
                return true;
            }

            // Dedicated Prime Video button (Sony remote maps to 188 [BUTTON_1])
            if (code == 188 /* KEYCODE_BUTTON_1 */) {
                lastTriggerTime = now;
                Log.i(TAG, "Launching Prime Video from key: " + code);
                launchPackage("com.amazon.amazonvideo.livingroom");
                return true;
            }

            // Dedicated Sony Pictures Core button (maps to 245)
            if (code == 245) {
                lastTriggerTime = now;
                Log.i(TAG, "Launching Sony Pictures Core from key: " + code);
                launchPackage("com.sonypicturescore");
                return true;
            }

            // Dedicated Disney+ / Hotstar button (maps to 196 [BUTTON_9] or 3005)
            if (code == 196 || code == 3005) {
                lastTriggerTime = now;
                Log.i(TAG, "Launching Disney+/Hotstar from key: " + code);
                if (!launchPackage("in.startv.hotstar")) {
                    launchPackage("com.disney.disneyplus");
                }
                return true;
            }

            // Dedicated YouTube Music button (maps to 202 [BUTTON_15])
            if (code == 202 /* KEYCODE_BUTTON_15 */) {
                lastTriggerTime = now;
                Log.i(TAG, "Launching YouTube Music from key: " + code);
                launchPackage("com.google.android.youtube.tvmusic");
                return true;
            }
        }
        return super.onKeyEvent(event);
    }

    private boolean launchPackage(String pkg) {
        try {
            Intent intent = getPackageManager().getLeanbackLaunchIntentForPackage(pkg);
            if (intent == null) intent = getPackageManager().getLaunchIntentForPackage(pkg);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            } else {
                Log.w(TAG, "No launch intent found for " + pkg);
            }
        } catch (Throwable t) {
            Log.e(TAG, "Failed to launch " + pkg + ": " + t.getMessage());
        }
        return false;
    }
}
