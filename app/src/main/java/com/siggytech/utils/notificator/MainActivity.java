package com.siggytech.utils.notificator;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.widget.LinearLayout;

import com.siggytech.utils.communication.ChatControl;
import com.siggytech.utils.communication.Conf;
import com.siggytech.utils.communication.NotificationAgent;
import com.siggytech.utils.communication.PTTButton;

public class MainActivity extends AppCompatActivity {

    PTTButton pttButton;
    LinearLayout linearLayout;
    String TAG = "SAMPLE APP";
    String API_KEY = "";
    //String name = "";
    String name = "";
    //String name = "";
    ChatControl ch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        linearLayout = findViewById(R.id.linear1);

        Conf.SERVER_IP = ""; //Set dedicated IP server.
        //Conf.SERVER_IP = "192.168.1.148";

        onNewIntent(getIntent());

        //check permissions
        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO


            };
            if (!hasPermissions(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, 112 );
            } else {
                System.out.println(getApplicationContext().getPackageName());
                System.out.println(getResources().getIdentifier("siggy_logo",
                        "drawable", getPackageName()));

                //addPTTButton();

                //subscribeForNotifications();

                addChatListView();
            }
        }
        else{
            System.out.println(getApplicationContext().getPackageName());
            System.out.println(getResources().getIdentifier("siggy_logo",
                    "drawable", getPackageName()));

            //addPTTButton();

            //subscribeForNotifications();
            addChatListView();

            //YAS
        }




    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            System.out.println(getApplicationContext().getPackageName());
            System.out.println(getResources().getIdentifier("siggy_logo",
                    "drawable", getPackageName()));

            addPTTButton();

            subscribeForNotifications();
        } else {
                    // exit app
        }
    }
    private void subscribeForNotifications(){
        NotificationAgent na = new NotificationAgent();
        na.register(this, 1, API_KEY, name);
    }
    private void addPTTButton(){

        pttButton = new PTTButton(this, 1, API_KEY, name, PTTButton.AudioQuality.HIGH);

        pttButton.setWidth(200);
        pttButton.setHeight(200);
        pttButton.setText("Hablar!");

        linearLayout.addView(pttButton);


    }
    @SuppressWarnings("deprecation")
    private String getIMEINumber() {
        String IMEINumber = "";
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyMgr = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                IMEINumber = telephonyMgr.getImei();
            } else {
                IMEINumber = telephonyMgr.getDeviceId();
            }
        }
        return IMEINumber;
    }
    @Override
    public void onNewIntent(Intent intent){
        Bundle extras = intent.getExtras();
        if(extras != null){
            if(extras.containsKey("notificationMessage"))
            {

                System.out.println("Message from notifcation: " + extras.getString("notificationMessage").toString());
            }
        }


    }
    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    public void addChatListView()
    {
        Conf.DATE_FORMAT = 2; //dd-mm-yyyy hh24:mm:ss
        Conf.LOCAL_USER = "Yo"; //user name to show in my device. Default: Me
        ch = new ChatControl(this, 6870, API_KEY, getIMEINumber(), "Felipe",
                "Titulo del mensaje", "Texto del Mensaje",
                getApplicationContext().getPackageName(),getResources().getIdentifier("siggy_logo",
                "drawable", getPackageName()), "Mensaje a la actividad" );//user name to show to others
        linearLayout.addView(ch);
    }
}
