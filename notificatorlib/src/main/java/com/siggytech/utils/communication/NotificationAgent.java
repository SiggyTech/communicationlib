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

    public void register(Context context, int idGroup, String API_KEY, String nameClient, String iconName){

        Intent intent = new Intent(context, NotificationService.class);

        intent.putExtra("imei", getIMEINumber(context));
        intent.putExtra("clientname", nameClient);
        intent.putExtra("groupid", idGroup);
        intent.putExtra("api_key", API_KEY);
        intent.putExtra("iconName", iconName);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("imei", getIMEINumber(context));
        editor.putString("clientname", nameClient);
        editor.putString("groupid", String.valueOf(idGroup));
        editor.putString("api_key", API_KEY);
        editor.putString("iconName", iconName);
        editor.commit();

        Utils.writeToFile(getIMEINumber(context).toString() + ";" + nameClient + ";" +
                               API_KEY + ";" + String.valueOf(idGroup) + ";" + iconName, context);


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
