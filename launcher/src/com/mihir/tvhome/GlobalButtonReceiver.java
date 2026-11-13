package com.mihir.tvhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;

public class GlobalButtonReceiver extends BroadcastReceiver {
    private static final String TAG = "GlobalButtonReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event != null && event.getAction() == KeyEvent.ACTION_UP) {
            int code = event.getKeyCode();
            Log.i(TAG, "GlobalButtonReceiver onReceive keycode: " + code);

            if (code == KeyEvent.KEYCODE_SETTINGS || code == 176 || code == 312) {
                Intent s = new Intent(Settings.ACTION_SETTINGS);
                s.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(s);
            } else if (code == KeyEvent.KEYCODE_GUIDE 
                || code == 298 
                || code == 297 
                || code == KeyEvent.KEYCODE_PROG_RED 
                || code == KeyEvent.KEYCODE_BUTTON_2 
                || code == KeyEvent.KEYCODE_BUTTON_3 
                || code == KeyEvent.KEYCODE_BUTTON_1) {
                Intent yt = context.getPackageManager().getLeanbackLaunchIntentForPackage("com.google.android.youtube.tv");
                if (yt == null) yt = context.getPackageManager().getLaunchIntentForPackage("com.google.android.youtube.tv");
                if (yt != null) {
                    yt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(yt);
                }
            }
        }
    }
}
