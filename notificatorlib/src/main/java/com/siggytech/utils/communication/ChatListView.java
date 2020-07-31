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

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;

public class ChatListView extends RecyclerView implements AsyncTaskCompleteListener<TaskMessage> {
    private String TAG = ChatListView.class.getSimpleName();
    List<ChatModel> lsChat = new ArrayList<>();
    Handler timerHandler = new Handler();
    Context context;
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
        this.packageName = packageName;
        this.resIcon = resIcon;
        this.gson = Utils.getGson();

        timerHandler.postDelayed(timerRunnable,0);

        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);

            String url = "ws://" + Conf.SERVER_IP + ":" + Conf.SERVER_CHAT_PORT_IN + "?imei=" + getIMEINumber() + "&groupId=" + idGroup + "&API_KEY="+ API_KEY +"&clientName=" + nameClient;

            socket = Socket.Builder.with(url).build().connect();

            if(!Utils.isMyServiceRunning(WebSocketChatService.class,context)){
                Intent i = new Intent(context, WebSocketChatService.class);
                i.putExtra("name", nameClient);
                i.putExtra("idGroup",idGroup);
                i.putExtra("imei",getIMEINumber());
                i.putExtra("apiKey",API_KEY);
                context.startService(i);
            }else{
                Utils.traces("ChatListView WebSocketChatService already exists");
            }



        } catch(Exception ex){
            Utils.traces("On new ChatListView : "+Utils.exceptionToString(ex));
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
        intent.putExtra(ChatControl.NOTIFICATION_MESSAGE, notificationMessage);
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
            Utils.traces("ChatListView sendMessage : "+Utils.exceptionToString(e));
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
            Utils.traces("ChatListView onTaskCompleted : "+Utils.exceptionToString(e));
        }
    }

    private void saveHistory(MessageModel model){
        //TODO
    }


    public void onMessageReceiver(String text){
        try {
            notificationMessage = text; //message for activity passed through notification
            JSONObject jObject = new JSONObject(text);
            from = new JSONObject(jObject.getString("data")).getString("from");
            messageText = new JSONObject(jObject.getString("data")).getString("text");
            dateTime = Utils.getStringDate();

            newMessage = true;
        } catch(Exception ex){
            Utils.traces("ChatListView onMessageReceiver : "+Utils.exceptionToString(ex));
        }
    }

}
