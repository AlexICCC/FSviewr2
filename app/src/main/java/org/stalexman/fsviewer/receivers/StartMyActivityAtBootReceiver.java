package org.stalexman.fsviewer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.WindowManager;

import org.stalexman.fsviewer.FullscreenActivity;

/**
 * Created by Алекс on 09.11.2016.
 *  BroadcastReceiver, срабатывающий при загрузке.
 *  Запускает FullscreenActivity
 *
 */

public class StartMyActivityAtBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent myStarterIntent = new Intent(context, FullscreenActivity.class);
            myStarterIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            myStarterIntent.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            context.startActivity(myStarterIntent);
        }
    }

}