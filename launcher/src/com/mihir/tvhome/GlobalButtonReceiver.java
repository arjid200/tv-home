package com.mihir.tvhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

public class GlobalButtonReceiver extends BroadcastReceiver {
    private static final String TAG = "GlobalButtonReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        Log.i(TAG, "onReceive action: " + action);

        // Ensure accessibility service remains active on boot
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            try {
                String enabledServices = Settings.Secure.getString(context.getContentResolver(),
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
                String myService = context.getPackageName() + "/" + RemoteKeyService.class.getName();
                if (enabledServices == null || !enabledServices.contains(myService)) {
                    String updated = (enabledServices == null || enabledServices.isEmpty())
                            ? myService
                            : enabledServices + ":" + myService;
                    Settings.Secure.putString(context.getContentResolver(),
                            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, updated);
                    Settings.Secure.putInt(context.getContentResolver(),
                            Settings.Secure.ACCESSIBILITY_ENABLED, 1);
                    Log.i(TAG, "Re-enabled RemoteKeyService on boot");
                }
            } catch (Throwable t) {
                Log.w(TAG, "Could not self-heal accessibility on boot: " + t.getMessage());
            }
            return;
        }

        // Google TV standard app launch broadcast from physical remote buttons
        if ("com.google.global_button.ACTION_LAUNCH_APP".equals(action)) {
            Intent launchIntent = intent.getParcelableExtra("launchIntent");
            String launchPkg = intent.getStringExtra("launchPackageName");
            Log.i(TAG, "ACTION_LAUNCH_APP received for package: " + launchPkg);

            if (launchIntent != null) {
                try {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(launchIntent);
                    return;
                } catch (Throwable t) {
                    Log.w(TAG, "Failed launching launchIntent: " + t.getMessage());
                }
            }

            if (!TextUtils.isEmpty(launchPkg)) {
                launchPackage(context, launchPkg);
                return;
            }
        }

        // Global button intent or Realtek / Sony action menu broadcast
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event != null) {
            int code = event.getKeyCode();
            Log.i(TAG, "Key event received: code=" + code + " action=" + event.getAction());

            // Handle Settings (gear button)
            if (code == KeyEvent.KEYCODE_SETTINGS 
                    || code == 176 
                    || code == 197 /* KEYCODE_BUTTON_10 on Sony SmartUI */
                    || code == 312 /* KEYCODE_QUICK_SETTINGS */
                    || code == KeyEvent.KEYCODE_MENU) {
                openSettings(context);
                return;
            }

            // Handle YouTube button
            if (code == KeyEvent.KEYCODE_GUIDE 
                    || code == 172
                    || code == 188 /* KEYCODE_BUTTON_1 */
                    || code == 189 /* KEYCODE_BUTTON_2 */
                    || code == 190 /* KEYCODE_BUTTON_3 */
                    || code == 191 /* KEYCODE_BUTTON_4 */
                    || code == 192 /* KEYCODE_BUTTON_5 */
                    || code == 183 /* KEYCODE_PROG_RED */
                    || code == 297 /* KEYCODE_FEATURED_APP_1 */
                    || code == 298 /* KEYCODE_FEATURED_APP_2 */
                    || code == 301 /* KEYCODE_FEATURED_APP_5 */
                    || code == 302 /* KEYCODE_FEATURED_APP_6 */) {
                launchPackage(context, "com.google.android.youtube.tv");
            }
        }
    }

    private static void openSettings(Context context) {
        try {
            Intent s = new Intent(Settings.ACTION_SETTINGS);
            s.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(s);
        } catch (Throwable t) {
            Log.e(TAG, "Failed to open settings: " + t.getMessage());
        }
    }

    private static void launchPackage(Context context, String pkg) {
        try {
            Intent intent = context.getPackageManager().getLeanbackLaunchIntentForPackage(pkg);
            if (intent == null) intent = context.getPackageManager().getLaunchIntentForPackage(pkg);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                Log.w(TAG, "No launch intent found for " + pkg);
            }
        } catch (Throwable t) {
            Log.e(TAG, "Failed to launch package " + pkg + ": " + t.getMessage());
        }
    }
}
