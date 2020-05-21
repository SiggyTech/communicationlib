package com.siggytech.utils.communication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
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
import android.os.Handler;
import android.os.PowerManager;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.siggytech.utils.communication.async.AsyncTaskCompleteListener;
import com.siggytech.utils.communication.async.CallTask;
import com.siggytech.utils.communication.async.TaskMessage;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;

public class ChatListView extends RecyclerView implements AsyncTaskCompleteListener<TaskMessage> {
    private String TAG = ChatListView.class.getSimpleName();
    List<ChatModel> lsChat = new ArrayList<>();
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
    private String packageName, notificationMessage;
    int resIcon;
    private Socket socket;
    private Gson gson;
    private Activity mActivity;

    public ChatListView (Context context, Activity activity, int idGroup, String API_KEY, String nameClient, String packageName, int resIcon){
        super(context);
        this.context = context;
        this.mActivity = activity;
        this.idGroup = idGroup;
        this.API_KEY = API_KEY;
        this.name = nameClient;

        imei = getIMEINumber();

        this.packageName = packageName;
        this.resIcon = resIcon;
        this.gson = Utils.GetGson();

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
        setAdapter();
    }

    private void setAdapter(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setStackFromEnd(true);
        this.setLayoutManager(linearLayoutManager);
        this.setHasFixedSize(true);
        this.setAdapter(new CustomAdapterBubble(lsChat, context,mActivity));
        this.getLayoutManager().scrollToPosition(this.getAdapter().getItemCount()-1);
    }

    private void notifyItemInserted(){
        this.getAdapter().notifyItemInserted(lsChat.size() - 1);
        this.getLayoutManager().scrollToPosition(this.getAdapter().getItemCount()-1);
    }

    private void webSocketConnection(){
        WebSocketListener webSocketListenerCoinPrice;
        OkHttpClient clientCoinPrice = new OkHttpClient();

        String url = "ws://" + Conf.SERVER_IP + ":" + Conf.SERVER_CHAT_PORT + "?imei=" + this.getIMEINumber() + "&groupId=" + this.idGroup + "&API_KEY="+ this.API_KEY +"&clientName=" + this.name;
        Log.e(TAG, url);

        Request requestCoinPrice = new Request.Builder().url(url).build();

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
                    //newMessageType = jObject.getString("event");
                    dateTime = Utils.GetStringDate();

                    newMessage = true;

                } catch(Exception ex){
                    Log.e(TAG, ex.getMessage());
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                try {
                    Log.e(TAG, "MESSAGE bytes: " + bytes.hex()); //
                } catch(Exception ex){
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

    public void addNotification(String title, String text, String packageName, Class<?> launchClass , int resIcon, String notificationMessage) {
        PackageManager pmg = context.getPackageManager();
        String name = "";
        Intent LaunchIntent = null;

        try {
            if (pmg != null) {
                ApplicationInfo app = context.getPackageManager().getApplicationInfo(packageName, 0);
                name = (String) pmg.getApplicationLabel(app);

                LaunchIntent = new Intent(context, launchClass);
            }
            Log.d("intent.getExtras", "Found!: " + name);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        // handle build version above android oreo
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
                mNotificationManager.getNotificationChannel(Conf.FOREGROUND_CHANNEL_ID) == null) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(Conf.FOREGROUND_CHANNEL_ID, name, importance);
            channel.enableVibration(false);
            mNotificationManager.createNotificationChannel(channel);
        }



        Intent intent = LaunchIntent; // new Intent();
        intent.putExtra("notificationMessage", notificationMessage);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);


        // notification builder
        NotificationCompat.Builder notificationBuilder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder = new NotificationCompat.Builder(context, Conf.FOREGROUND_CHANNEL_ID);
        } else {
            notificationBuilder = new NotificationCompat.Builder(context);
        }

        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        notificationBuilder
                .setSmallIcon(R.drawable.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(text)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pIntent)
                .setSound(uri)
                .setSmallIcon(resIcon)
                .setAutoCancel(true);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        }

        Notification notification =  notificationBuilder.build();
        // Cancel the notification after its selected
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(Conf.NOTIFICATION_ID_FOREGROUND_SERVICE,notification);


        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        Log.e("screen on.........", ""+isScreenOn);
        if(!isScreenOn) {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,TAG);
            wl.acquire(10000);
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyCpuLock");

            wl_cpu.acquire(10000);
        }
    }

    private boolean appInForeground(@NonNull Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        if (runningAppProcesses == null) {
            return false;
        }

        for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses) {
            if (runningAppProcess.processName.equals(context.getPackageName()) &&
                    runningAppProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if(newMessage) {
                try{
                    MessageModel model = gson.fromJson(AESUtils.decText(messageText),MessageModel.class);
                    if(!appInForeground(context)){
                        String messageText = context.getString(R.string.new_message);
                        if(Utils.MESSAGE_TYPE.MESSAGE.equals(model.getType())){
                            messageText = model.getMessage();
                        }
                        addNotification(from, messageText, packageName,mActivity.getClass(), resIcon, notificationMessage);
                    }
                    newMessage = false;
                    lsChat.add(new ChatModel(1L, model, from, dateTime,false));
                    notifyItemInserted();
                    saveHistory(model);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            timerHandler.postDelayed(timerRunnable,100);
        }
    };

    public void sendMessage(String from, String encryptedData, String dateTime, String type){
        try{
            socket.sendOnOpen(type, "{\n" +
                    "    \"from\": \"" + from +  "\",\n" +
                    "    \"text\": \"" + encryptedData +  "\", \n" +
                    "    \"dateTime\": \"" + dateTime +  "\" \n" +
                    "}");

            MessageModel model = gson.fromJson(AESUtils.decText(encryptedData),MessageModel.class);
            lsChat.add(new ChatModel(1L, model, Conf.LOCAL_USER, dateTime,true));
            notifyItemInserted();
            saveHistory(model);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void callToBase64(MessageModel messageModel){
        try{
            new CallTask(context,this).execute(messageModel);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskCompleted(TaskMessage result) {
        try {
            if (result != null && result.getMessageModel() != null) {
                sendMessage(result.getMessageModel().getFrom()
                        ,AESUtils.encText(gson.toJson(result.getMessageModel()))
                        ,result.getMessageModel().getDate()
                        ,result.getMessageModel().getType());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void saveHistory(MessageModel model){

    }
}
