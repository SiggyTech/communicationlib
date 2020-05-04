package com.siggytech.utils.notificator;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.siggytech.utils.communication.ChatControl;
import com.siggytech.utils.communication.Conf;
import com.siggytech.utils.communication.PTTButton;

public class MainActivity extends AppCompatActivity {
    
    PTTButton pttButton;
    Boolean keyDown = false;
    LinearLayout linearLayout;
    String API_KEY = "";

    String name = "";
    ChatControl ch;

    boolean isChat = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        linearLayout = findViewById(R.id.linear1);

        Conf.SEND_FILES = true;
        Conf.CHAT_BASIC = false;

        Conf.SERVER_IP = ""; //Set dedicated IP server.


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
                if(isChat){
                    addChatListView();
                }else addPTTButton();
            }
        }
        else{
            System.out.println(getApplicationContext().getPackageName());
            System.out.println(getResources().getIdentifier("siggy_logo",
                    "drawable", getPackageName()));

            if(isChat){
                addChatListView();
            }else addPTTButton();

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(!keyDown  && keyCode == 25 && event.getAction() == KeyEvent.ACTION_DOWN) //25 down volume key on testing device.
            if(pttButton!=null)
                pttButton.startTalking();

        keyDown = true;
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == 25 && event.getAction() == KeyEvent.ACTION_UP ) {
            keyDown = false;
            if(pttButton!=null)
                pttButton.stopTalking();
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            System.out.println(getApplicationContext().getPackageName());
            System.out.println(getResources().getIdentifier("siggy_logo",
                    "drawable", getPackageName()));
            if(isChat){
                addChatListView();
            }else addPTTButton();

        } else {
           //TODO ask permission again          // exit app
            Toast.makeText(MainActivity.this,"Missing implement.",Toast.LENGTH_LONG).show();
        }
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
            if(extras.containsKey("notificationMessage")) {
                System.out.println("Message from notification: " + extras.getString("notificationMessage").toString());
            }
        }
        super.onNewIntent(intent);
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

    public void addChatListView() {
        Conf.DATE_FORMAT = 2; //dd-mm-yyyy hh24:mm:ss
        Conf.LOCAL_USER = "Yo"; //user name to show in my device. Default: Me
        ch = new ChatControl(this, 1, API_KEY, getIMEINumber(), "Felipe",
                getApplicationContext().getPackageName(),
                getResources().getIdentifier("siggy_logo", "drawable", getPackageName()),
                this);//user name to show to others
        linearLayout.addView(ch);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }
}
