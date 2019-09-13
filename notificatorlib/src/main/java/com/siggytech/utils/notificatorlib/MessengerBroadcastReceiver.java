package com.siggytech.utils.notificatorlib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by fsoto on 9/13/19.
 */

public class MessengerBroadcastReceiver extends BroadcastReceiver {

    public String packageName;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(MessengerService.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");


        Bundle extras = intent.getExtras();

        if (extras == null) {
            Log.d("Service", "null");
        } else {
            Log.d("Service", "not null");

            packageName = extras.get("packageName").toString();

        }

        Log.d("BroadcastReceiver ", "packageName: " + packageName);

        Intent serviceIntent = new Intent(context, MessengerService.class);
        serviceIntent.putExtra("packageName", packageName);
        context.startService(serviceIntent);
    }
}