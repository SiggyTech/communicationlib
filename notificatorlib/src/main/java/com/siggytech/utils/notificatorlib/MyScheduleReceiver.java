package com.siggytech.utils.notificatorlib;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by fsoto on 9/13/19.
 */

public class MyScheduleReceiver  extends BroadcastReceiver {

    // Restart service every 30 seconds
    private static long REPEAT_TIME = 1000 * 30;
    private static final String DEBUG_TAG = "MyScheduleReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(DEBUG_TAG, "MyScheduleReceiver - onRecive valor de FREC");
        REPEAT_TIME = 1000 * intent.getIntExtra("FREC", 30);

        AlarmManager service = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, MyBroadcastReceiver.class);
        Bundle extras = intent.getExtras();
        i.putExtra("packageName", extras.get("packageName").toString());
        i.putExtra("messageText", extras.get("messageText").toString());
        i.putExtra("messageTittle", extras.get("messageTittle").toString());


        PendingIntent pending = PendingIntent.getBroadcast(context, 0, i,
                PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar cal = Calendar.getInstance();
        // Start 30 seconds after boot completed or intent in Manifest
        cal.add(Calendar.SECOND, 3);
        //
        // Fetch every 30 seconds
        // InexactRepeating allows Android to optimize the energy consumption
        service.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(), REPEAT_TIME, pending);



    }
}