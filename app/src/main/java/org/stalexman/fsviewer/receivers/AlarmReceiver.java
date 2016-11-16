package org.stalexman.fsviewer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.WindowManager;

import org.stalexman.fsviewer.FullscreenActivity;

/**
 * Created by Алекс on 09.11.2016.
 *
 * Broadcast receiver, срабатывающий по таймеру.
 * Выставляется в SettingsActivity
 * Запускает FullscreenActivity
 */


public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //При запуске
        if (intent.getAction().equals("org.stalexman.fsviewer.START")) {
            Intent myStarterIntent = new Intent(context, FullscreenActivity.class);
            myStarterIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            myStarterIntent.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            context.startActivity(myStarterIntent);
        }
        //При выключении
        // Пока при выключении программа падает, а не выключается.
        // Надо думать...
        if (intent.getAction().equals("org.stalexman.fsviewer.STOP")) {
            Intent myCloserIntent = new Intent(context, FullscreenActivity.class);
            myCloserIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            context.startActivity(myCloserIntent);

              }
    }
}