package com.siggytech.utils.notificatorlib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MyBroadcastReceiver  extends BroadcastReceiver {

    //final MyBroadcastReceiver context=this;
    //private final String DEBUG_TAG = "BroadcastReceiver";
    @Override
    public void onReceive(final Context context, final Intent bootintent) {
        //Log.d(DEBUG_TAG, "onReceive BroadcastReceiver");

        Bundle extras = bootintent.getExtras();

        Intent startServiceIntent = new Intent(context, MessengerService.class);
        try {
            startServiceIntent.putExtra("packageName", extras.get("packageName").toString());
            startServiceIntent.putExtra("messageText", extras.get("messageText").toString());
            startServiceIntent.putExtra("messageTittle", extras.get("messageTittle").toString());
        } catch(Exception ee) {
        }

        context.startService(startServiceIntent);
    }
}