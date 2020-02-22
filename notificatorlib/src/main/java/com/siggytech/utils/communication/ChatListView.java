package com.siggytech.utils.communication;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ListView;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import static android.content.Context.TELEPHONY_SERVICE;

public class ChatListView extends ListView {

    private String TAG = "ChatListView";
    List<ChatModel> lsChat = new ArrayList<>();
    CustomAdapterBubble customAdapterBubble;
    Handler timerHandler = new Handler();
    Context context;
    private int idGroup;
    private String API_KEY;
    private String imei;
    private String name;
    private boolean newMessage = false;
    private String messageText;
    private String from;
    private String dateTime;
    private String messageTittle, notificationText, packageName, notificationMessage;
    int resIcon;
    private Socket socket;

    public void addNotification(String title, String text, String packageName, int resIcon, String notificationMessage) {

        PackageManager pmg = context.getPackageManager();
        String name = "";
        Intent LaunchIntent = null;

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
                .setSmallIcon(R.drawable.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pIntent)
                .setSound(uri)
                .setSmallIcon(resIcon)
                .setAutoCancel(true)
                ;

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
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,TAG);
            wl.acquire(10000);
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");

            wl_cpu.acquire(10000);
        }
    }

    public boolean isRunning(Context ctx) {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (ctx.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName()))
                return true;
        }

        return false;
    }

    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

            if(newMessage) {
                try{

                    //if(!isRunning(context)){
                    //    addNotification(messageTittle, notificationText, packageName, resIcon, notificationMessage);
                    //}


                    newMessage = false;
                    lsChat.add(new ChatModel(1L, AESUtils.decrypt(messageText), from, dateTime)); //TODO agregar fecha a la caja de texto
                    SetAdapter();
                }
                catch (Exception e){e.printStackTrace();}

            }

            timerHandler.postDelayed(timerRunnable,100);
        }
    };

    public void sendMessage(String from, String text, String dateTime){

        try{

            socket.send("Message", "{\n" +
                    "    \"from\": \"" + from +  "\",\n" +
                    "    \"text\": \"" + text +  "\", \n" +
                    "    \"dateTime\": \"" + dateTime +  "\" \n" +
                    "}");

            lsChat.add(new ChatModel(1L, AESUtils.decrypt(text), Conf.LOCAL_USER, dateTime)); //TODO agregar fecha a la caja de texto y from
            SetAdapter();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    public ChatListView (Context context, int idGroup, String API_KEY, String nameClient, String messageTittle, String messageText, String packageName, int resIcon){
        super(context);
        this.context = context;
        this.idGroup = idGroup;
        this.API_KEY = API_KEY;
        this.name = nameClient;

        imei = getIMEINumber();

        this.messageTittle = messageTittle;
        this.notificationText = messageText;
        this.packageName = packageName;
        this.resIcon = resIcon;

        timerHandler.postDelayed(timerRunnable,0);

        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);

            String url = "ws://" + Conf.SERVER_IP + ":" + Conf.SERVER_CHAT_PORT_IN + "?imei=" + imei + "&groupId=" + idGroup + "&API_KEY="+ API_KEY +"&clientName=" + name;

            socket = Socket.Builder.with(url).build().connect();

            webSocketConnection();
        }
        catch(Exception ex){
            Log.e(TAG, "error en webSocketConnection: " + ex.getMessage());
        }
    }
    public void SetAdapter(){
        customAdapterBubble = new CustomAdapterBubble(lsChat, context);
        this.setAdapter(customAdapterBubble);
        this.setSelection(this.getAdapter().getCount()-1);
    }
    private void webSocketConnection(){

        WebSocketListener webSocketListenerCoinPrice;
        OkHttpClient clientCoinPrice = new OkHttpClient();

        String url = "ws://" + Conf.SERVER_IP + ":" + Conf.SERVER_CHAT_PORT + "?imei=" + this.getIMEINumber() + "&groupId=" + this.idGroup + "&API_KEY="+ this.API_KEY +"&clientName=" + this.name;
        Log.e(TAG, url);

        Request requestCoinPrice = new Request.Builder().url(url).build();

        //OLD: Request requestCoinPrice = new Request.Builder().url("ws://" + Conf.SERVER_IP + ":" + Conf.SERVER_WS_PORT).build();

        webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                //webSocket.send("{\n" +
                //        "    \"type\": \"subscribe\",\n" +
                //        "    \"channels\": [{ \"name\": \"ticker\", \"product_ids\": [\"product\"] }]\n" +
                //        "}");
                Log.e(TAG, "onOpen");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.e(TAG, "MESSAGE String: " + text); //here comes the message
                try {
                    notificationMessage = text; //message for activity passed through notification
                    JSONObject jObject = new JSONObject(text);
                    from = new JSONObject(jObject.getString("data")).getString("from");
                    messageText = new JSONObject(jObject.getString("data")).getString("text");
                    //dateTime = new JSONObject(jObject.getString("data")).getString("dateTime");
                    SimpleDateFormat sdf;
                    switch(Conf.DATE_FORMAT) {
                        case 0:
                            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            break;
                        case 1:
                            sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                            break;
                        default:
                            sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                            break;
                    }

                    Date now = new Date();
                    String strDate = sdf.format(now);


                    dateTime = strDate;
                    newMessage = true;
                }
                catch(Exception ex){
                    Log.e(TAG, ex.getMessage());
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                try
                {
                    Log.e(TAG, "MESSAGE bytes: " + bytes.hex());
                }
                catch(Exception ex){
                    System.out.print(ex.getMessage());
                }
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
    @SuppressWarnings("deprecation")
    private String getIMEINumber() {
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
