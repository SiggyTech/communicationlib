package com.siggytech.utils.communication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;



public class MessengerBroadcastReceiver extends BroadcastReceiver {

    public String packageName;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(MessengerService.class.getSimpleName(), "Service onReceive! Oooooooooooooppppssssss!!!!");


        Bundle extras = intent.getExtras();

        if (extras == null) {
            Log.d("Service", "null");
        } else {
            Log.d("Service", "not null");

            packageName = extras.get("packageName").toString();


        }

        Log.e("BroadcastReceiver ", "packageName: " + packageName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, MessengerService.class));
        } else {
            context.startService(new Intent(context, MessengerService.class));
        }
    }
}