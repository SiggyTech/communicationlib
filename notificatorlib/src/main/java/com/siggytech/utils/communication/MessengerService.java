package com.siggytech.utils.communication;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;


public class MessengerService extends Service {

    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_WRITE = 2;
    public int LocalPort = 1984;
    public Context context;
    public String TAG = "MessengerService";

    public static String packageName;
    public static String messageTittle;
    public static String imei;
    public static String clientname;
    public static String api_key;
    public static String messageText;
    public static String idgroup;
    public static String iconName;

    WebSocketListener webSocketListenerCoinPrice;
    OkHttpClient clientCoinPrice;
    Response responseObj;


    public MessengerService(Context applicationContext) {
        super();
        context = applicationContext;
        Log.i("HERE", "here I am!");
    }

    public MessengerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1,new Notification());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.siggy.service";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        String res = readFromFile(context);

        String resArray[] = res.split(";");
        imei = resArray[0];
        clientname = resArray[1];
        api_key = resArray[2];
        idgroup = resArray[3];
        iconName = resArray[4];

        int idResIcon = getResourceId(iconName, "drawable", packageName);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(idResIcon)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @Override
    public void onStart(Intent intent, int startid) {
        context = getApplicationContext();//probando...
        try {

            Bundle extras = intent.getExtras();

            if (extras == null) {
                Log.d(TAG, "extras null");
                String res = readFromFile(context);

                String resArray[] = res.split(";");
                imei = resArray[0];
                clientname = resArray[1];
                api_key = resArray[2];
                idgroup = resArray[3];
                iconName = resArray[4];

            } else {
                Log.d(TAG, "extras not null");

                imei = extras.get("imei").toString();
                clientname = extras.get("clientname").toString();
                api_key = extras.get("api_key").toString();
                idgroup = extras.get("groupid").toString();
                iconName =  extras.get("iconName").toString();

                Utils.writeToFile(imei + ";" + clientname + ";" + api_key + ";" + String.valueOf(idgroup) + ";" + iconName, this.context);
            }
        }
        catch(Exception ex) {
            Log.d("intent.getExtras", "Error: " + ex.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        context = getApplicationContext();//probando...
        try {

            Bundle extras = intent.getExtras();

            if (extras == null) {
                Log.d("Service", "null");
            } else {
                Log.d("Service", "not null");

                packageName = extras.get("packageName").toString();
                iconName = extras.get("iconName").toString();

            }
        } catch(Exception ex) {
            Log.d("intent.getExtras", "Error: " + ex.getMessage());
        }

        startTimer();


        return START_STICKY;
    }

    private void webSocketConnection(){
        clientCoinPrice = new OkHttpClient();

        String url = "ws://" + Conf.SERVER_IP + ":" + Conf.SERVER_MSG_PORT + "?imei=" + imei + "&groupId=" + idgroup + "&API_KEY="+ api_key +"&clientName=" + clientname;
        Log.e(TAG, url);

        Request requestCoinPrice = new Request.Builder().url(url).build();

        webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                responseObj = response;
                //webSocket.send("{ \"packageName\": \"packageName\", \"messageText\": \"messageText\", \"messageTittle\": \"messageTittle\", \"from\": \"BLUEBIRD1\" }");
                Log.e(TAG, "onOpen");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {

                try {
                    Log.e(TAG, "MESSAGE String: " + text);
                    JSONObject obj = new JSONObject(text);

                    addNotification(obj.getString("messageTittle"), obj.getString("messageText"), obj.getString("packageName"), obj.getString("iconName"), obj.getString("notificationMessage"));
                } catch(Exception ex){
                    Log.e(TAG, "Error MESSAGE String: " + ex.getMessage());
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                Log.e(TAG, "MESSAGE bytes: " + bytes.hex());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000, null);
                webSocket.cancel();
                Log.e(TAG, "CLOSE: " + code + " " + reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                //TODO: stuff
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                //TODO: stuff
            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("EXIT", "ondestroy!");

        try {
            responseObj.body().close();
        }
        catch(Exception ex){Log.e("EXIT", ex.getMessage());}


        Intent broadcastIntent = new Intent(this, MessengerBroadcastReceiver.class);

        sendBroadcast(broadcastIntent);
    }

    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //

        webSocketConnection();
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                //Log.i("in timer", "in timer ++++  "+ (counter++));
            }
        };
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void addNotification(String title, String text, String packageName, String iconName, String notificationMessage) {

        PackageManager pmg = context.getPackageManager();
        String name = "";
        Intent LaunchIntent = null;
        int idResIcon = getResourceId(iconName, "drawable", packageName);
        try {
            if (pmg != null) {
                ApplicationInfo app = context.getPackageManager().getApplicationInfo(packageName, 0);
                name = (String) pmg.getApplicationLabel(app);
                LaunchIntent = pmg.getLaunchIntentForPackage(packageName);
            }
            Log.d("intent.getExtras", "Found!: " + name);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Intent intent = LaunchIntent; // new Intent();
        intent.putExtra("notificationMessage", notificationMessage);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pIntent)
                .setSound(uri)
                .setSmallIcon(idResIcon)
                .setAutoCancel(true);

        // Add as notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "Your_channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
                    manager.createNotificationChannel(channel);
                    builder.setChannelId(channelId);

        }

        manager.notify(0, builder.build());

        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        Log.e("screen on.........", ""+isScreenOn);
        if(isScreenOn==false)
        {
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MyLock");
            wl.acquire(10000);
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");

            wl_cpu.acquire(10000);
        }
    }

    public int getResourceId(String pVariableName, String pResourcename, String pPackageName) {
        try {
            return getResources().getIdentifier(pVariableName, pResourcename, pPackageName);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("config.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }

        return ret;
    }

}
