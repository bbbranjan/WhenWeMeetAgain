package com.example.suyashl.WhenWeMeetAgain;

/**
 * Created by SuyashL on 24/1/15.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReminderService extends WakeReminderIntentService {

    public ReminderService() {
        super("ReminderService");
    }

    @Override
    void doReminderWork(Intent intent) {
        Log.d("ReminderService", "Doing work.");
        Long rowId = intent.getExtras().getLong(RemindersDbAdapter.KEY_ROWID);

        NotificationManager mgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, ReminderEditActivity.class);
        notificationIntent.putExtra(RemindersDbAdapter.KEY_ROWID, rowId);

        PendingIntent pi = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);

        Context ctx2 = this;
        RemindersDbAdapter objDB = new RemindersDbAdapter(ctx2);
        Notification note;


        note = new Notification(android.R.drawable.stat_sys_warning, objDB.getNotif(RemindersDbAdapter.KEY_ROWID), System.currentTimeMillis());
        note.setLatestEventInfo(this,  getString(R.string.notify_new_task_title), objDB.getNotif(RemindersDbAdapter.KEY_ROWID), pi);
        note.defaults |= Notification.DEFAULT_SOUND;
        note.flags |= Notification.FLAG_AUTO_CANCEL;

        int id = (int)((long)rowId);
        mgr.notify(id, note);
    }
}

