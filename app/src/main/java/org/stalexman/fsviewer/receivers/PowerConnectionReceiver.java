package org.stalexman.fsviewer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.WindowManager;

import org.stalexman.fsviewer.FullscreenActivity;

/**
 * Created by Алекс on 09.11.2016.
 *
 * BroadcastReceiver, который заставляет FullscreenActivity запускаться при установке на зарядку
 * Запускает FullscreenActivity
 */

public class PowerConnectionReceiver extends BroadcastReceiver {

    public PowerConnectionReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
            Intent myStarterIntent = new Intent(context, FullscreenActivity.class);
            myStarterIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            myStarterIntent.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            context.startActivity(myStarterIntent);
        }
    }

}