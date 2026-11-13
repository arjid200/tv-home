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
            if (now - lastTriggerTime < 600) {
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

            // YouTube button (Sony / Android TV partner keys)
            if (code == KeyEvent.KEYCODE_GUIDE 
                    || code == 172
                    || code == 188 /* KEYCODE_BUTTON_1 */
                    || code == 189 /* KEYCODE_BUTTON_2 */
                    || code == 190 /* KEYCODE_BUTTON_3 */
                    || code == 191 /* KEYCODE_BUTTON_4 */
                    || code == 192 /* KEYCODE_BUTTON_5 */
                    || code == KeyEvent.KEYCODE_PROG_RED 
                    || code == 183
                    || code == 297 /* KEYCODE_FEATURED_APP_1 */
                    || code == 298 /* KEYCODE_FEATURED_APP_2 */
                    || code == 301 /* KEYCODE_FEATURED_APP_5 */
                    || code == 302 /* KEYCODE_FEATURED_APP_6 */) {
                lastTriggerTime = now;
                Log.i(TAG, "Launching YouTube from key: " + code);
                Intent yt = getPackageManager().getLeanbackLaunchIntentForPackage("com.google.android.youtube.tv");
                if (yt == null) yt = getPackageManager().getLaunchIntentForPackage("com.google.android.youtube.tv");
                if (yt != null) {
                    yt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(yt);
                    return true;
                }
            }
        }
        return super.onKeyEvent(event);
    }
}
