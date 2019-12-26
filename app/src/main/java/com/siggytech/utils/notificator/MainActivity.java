package com.siggytech.utils.notificator;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.siggytech.utils.communication.NotificationAgent;
import com.siggytech.utils.communication.PTTButton;

public class MainActivity extends AppCompatActivity {

    PTTButton pttButton;
    LinearLayout linearLayout;
    String TAG = "SAMPLE APP";
    String API_KEY = "HGDJLGOPQJZGMIPEHBSJ";
    String name = "HUAWEI1";
    //String name = "BLACKVIEW1";
    //String name = "BLUEBIRD1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onNewIntent(getIntent());

        //check permissions
        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.INTERNET


            };
            if (!hasPermissions(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, 112 );
            } else {
                //do here
            }
        } else {
            //do here
        }


        System.out.println(getApplicationContext().getPackageName());
        System.out.println(getResources().getIdentifier("siggy_logo",
                "drawable", getPackageName()));


        addPTTButton();

        subscribeForNotifications();

    }
    private void subscribeForNotifications(){
        NotificationAgent na = new NotificationAgent();
        na.register(this, 1, API_KEY, name);
    }
    private void addPTTButton(){
        linearLayout = findViewById(R.id.linear1);
        pttButton = new PTTButton(this, this, 1, API_KEY, name);


        //pttButton = new PTTButton(this, this, 1, "QQQQ-WWWW-EEEE-RRRR", true, "192.168.1.87", 7778);
        //pttButton = new PTTButton(this, this, 1, "QQQQ-WWWW-EEEE-RRRR");
        pttButton.setWidth(200);
        pttButton.setHeight(200);
        pttButton.setText("Hablarz3!");

        linearLayout.addView(pttButton);


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
}
