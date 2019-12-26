package com.siggytech.utils.communication;

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
        Log.d("MyBroadcastReceiver", "onReceive BroadcastReceiver");

        Intent startServiceIntent = new Intent(context, MessengerService.class);

        context.startService(startServiceIntent);
    }
}