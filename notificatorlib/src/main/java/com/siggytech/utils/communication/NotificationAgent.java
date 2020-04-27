package com.siggytech.utils.communication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;

import static android.content.Context.TELEPHONY_SERVICE;


public class NotificationAgent {

    public void register(Context context, int idGroup, String API_KEY, String nameClient){
        Intent intent = new Intent(context, MessengerService.class);

        intent.putExtra("imei", getIMEINumber(context).toString());
        intent.putExtra("clientname", nameClient);
        intent.putExtra("groupid", idGroup);
        intent.putExtra("api_key", API_KEY);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("imei", getIMEINumber(context).toString());
        editor.putString("clientname", nameClient);
        editor.putString("groupid", String.valueOf(idGroup));
        editor.putString("api_key", API_KEY);
        editor.commit();

        context.startService(intent);
    }
    @SuppressWarnings("deprecation")
    private String getIMEINumber(Context context) {
        String IMEINumber = "";
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyMgr = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                IMEINumber = telephonyMgr.getImei();
            } else {
                IMEINumber = telephonyMgr.getDeviceId();
            }
        }
        return IMEINumber;
    }

}
