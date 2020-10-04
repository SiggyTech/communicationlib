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
import com.siggytech.utils.communication.repo.DbHelper;
import com.siggytech.utils.communication.repo.MessageRaw;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.TELEPHONY_SERVICE;

public class ChatListView extends RecyclerView implements AsyncTaskCompleteListener<TaskMessage> {
    private String TAG = ChatListView.class.getSimpleName();
    List<ChatModel> lsChat = new ArrayList<>();
    Context context;
    private String packageName;
    int resIcon;
    private Socket socket;
    private Gson gson;
    private Activity mActivity;
    private long idGroup;
    private DbHelper dbHelper;
    private String apiKey;
    private int limitCount = 10;

    //helpers pagination
    private static final int PAGE_START = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int currentPage = PAGE_START;
    private long TOTAL_PAGES = 1;


    public ChatListView (Context context, Activity activity, long idGroup, String API_KEY, String nameClient, String packageName, int resIcon){
        super(context);
        this.context = context;
        this.mActivity = activity;
        this.packageName = packageName;
        this.resIcon = resIcon;
        this.gson = Utils.getGson();
        this.idGroup = idGroup;
        this.dbHelper = new DbHelper(activity);
        this.apiKey = API_KEY;

        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);

            String url = "ws://" + Conf.SERVER_IP + ":" + Conf.SERVER_CHAT_PORT_IN + "?imei=" + getIMEINumber() + "&groupId=" + idGroup + "&API_KEY="+ API_KEY +"&clientName=" + nameClient;

            socket = Socket.Builder.with(url).build().connect();

            if(!Utils.isServiceRunning(WebSocketChatService.class,context)){
                Intent i = new Intent(context, WebSocketChatService.class);
                i.putExtra("name", nameClient);
                i.putExtra("idGroup",idGroup); //TODO must remove
                i.putExtra("imei",getIMEINumber());
                i.putExtra("apiKey",API_KEY);
                context.startService(i);
            }else{
                Utils.traces("ChatListView WebSocketChatService already exists");
            }
        } catch(Exception ex){
            Utils.traces("On new ChatListView : "+Utils.exceptionToString(ex));
        }

        getTotalPagesCount();
        setAdapter();
    }

    /**
     * To gets total pages count
     */
    private void getTotalPagesCount() {
        try {
            long rowCount = dbHelper.getMessageCount(idGroup, apiKey);
            if(rowCount>0)
                TOTAL_PAGES = (int) Math.ceil((double)rowCount / limitCount);

            if(TOTAL_PAGES==0) TOTAL_PAGES++;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setAdapter(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setStackFromEnd(true);
        this.setLayoutManager(linearLayoutManager);
        this.setHasFixedSize(true);
        this.setAdapter(new CustomAdapterBubble(lsChat, context,mActivity));
        this.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;

                ChatListView.this.post(() -> loadNextPage());
            }

            @Override
            public long getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        Objects.requireNonNull(this.getLayoutManager()).scrollToPosition(Objects.requireNonNull(this.getAdapter()).getItemCount()-1);
    }

    private void notifyItemInserted(){
        Objects.requireNonNull(this.getAdapter()).notifyItemInserted(lsChat.size() - 1);
        Objects.requireNonNull(this.getLayoutManager()).scrollToPosition(this.getAdapter().getItemCount()-1);
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


    public void sendMessage(String from, String encryptedData, String dateTime, String type, long idGroup){
        try{
            socket.sendOnOpen(type, "{\n" +
                    "    \"from\": \"" + from +  "\",\n" +
                    "    \"text\": \"" + encryptedData +  "\", \n" +
                    "    \"dateTime\": \"" + dateTime +  "\", \n" +
                    "    \"idGroup\": \"" + idGroup +  "\" \n" +
                    "}");

            MessageRaw messageRaw = new MessageRaw();
            messageRaw.setUserKey(apiKey);
            messageRaw.setIdGroup(String.valueOf(idGroup));
            messageRaw.setFrom(from);
            messageRaw.setMessage(encryptedData);
            messageRaw.setDate(dateTime);
            messageRaw.setMine(1);

            long id = dbHelper.insertMessage(messageRaw);
            Log.e("KUSSES", "INSERT: "+id+"; ID GROUP:"+idGroup);

            MessageModel model = gson.fromJson(AESUtils.decText(encryptedData),MessageModel.class);
            lsChat.add(new ChatModel(1L, model, Conf.LOCAL_USER, dateTime,true));
            notifyItemInserted();
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
                        ,result.getMessageModel().getType(),
                        idGroup);
            }
        }catch (Exception e){
            Utils.traces("ChatListView onTaskCompleted : "+Utils.exceptionToString(e));
        }
    }


    public void onMessageReceiver(String text){
        try {
            Utils.traces("1.- ChatListView onMessageReceiver");
            JSONObject jObject = new JSONObject(text);
            String idGroupFrom = new JSONObject(jObject.getString("data")).getString("idGroupFrom");

            String from = new JSONObject(jObject.getString("data")).getString("from");
            String messageText1 = new JSONObject(jObject.getString("data")).getString("text");
            String dateTime = Utils.getStringDate();

            MessageModel model = null;
            if(!appInForeground(context)){
                model = gson.fromJson(AESUtils.decText(messageText1),MessageModel.class);

                String messageText = context.getString(R.string.new_message);
                if(Utils.MESSAGE_TYPE.MESSAGE.equals(model.getType())){
                    messageText = model.getMessage();
                }
                addNotification(from, messageText, packageName, mActivity.getClass(), resIcon, text);
            }

            Utils.traces("1.- ChatListView onMessageReceiver idGroup: "+idGroup+"; al que llega: "+idGroupFrom);
            //if message is for the current group info
            if(idGroup == Long.parseLong(idGroupFrom)){
                if(model==null)
                    model = gson.fromJson(AESUtils.decText(messageText1),MessageModel.class);

                Utils.traces("1.- ChatListView onMessageReceiver entro a agregar al chat");
                lsChat.add(new ChatModel(1L, model, from, dateTime,false));
                notifyItemInserted();
            }
        } catch(Exception ex){
            Utils.traces("ChatListView onMessageReceiver : "+Utils.exceptionToString(ex));
        }

    }

    /**
     * closes socket
     */
    public void onDestroy(){
        try{
            if (dbHelper!=null) dbHelper.close();
            //TODO if (socket != null) socket.close(1000,"onDestroy");
        }catch (Exception e){
            Utils.traces("onDestroy ChatListView ex: "+(e!=null?e.getMessage():null));
        }
    }

    /**
     * Call this to change chat group
     * @param idGroup id group to see
     * @param limit limit records to retrieve
     */
    public void setGroupView(long idGroup, int limit){
        limitCount = limit;
        getTotalPagesCount(); //TODO
        if(this.idGroup != idGroup){
            this.idGroup = idGroup;
            addRawList(dbHelper.getMessage(idGroup,apiKey, 0, limit));
        }
    }

    private void addRawList(List<MessageRaw> list){
        try{
            lsChat.clear();
            for(MessageRaw raw : list) {
                lsChat.add(new ChatModel(1L
                        , gson.fromJson(AESUtils.decText(raw.getMessage()), MessageModel.class)
                        , raw.getFrom()
                        , raw.getDate()
                        , raw.getMine() != 0));
            }
            this.setAdapter(new CustomAdapterBubble(lsChat, context,mActivity));
            Objects.requireNonNull(this.getLayoutManager()).scrollToPosition(Objects.requireNonNull(this.getAdapter()).getItemCount()-1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void loadNextPage() {
        Log.d(TAG, "loadNextPage: " + currentPage+ "; TOTAL_PAGES: "+TOTAL_PAGES);

        List<MessageRaw> list = dbHelper.getMessage(
                idGroup
                ,apiKey
                , lsChat.size()-1
                , limitCount);

        Collections.reverse(list);

        ((CustomAdapterBubble) Objects.requireNonNull(this.getAdapter())).removeLoadingHeader();
        isLoading = false;

        try{
            for(MessageRaw raw : list) {
                ((CustomAdapterBubble) Objects.requireNonNull(getAdapter()))
                        .add(new ChatModel(1L
                                , gson.fromJson(AESUtils.decText(raw.getMessage()), MessageModel.class)
                                , raw.getFrom()
                                , raw.getDate()
                                , raw.getMine() != 0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (currentPage != TOTAL_PAGES)
            ((CustomAdapterBubble) Objects.requireNonNull(this.getAdapter())).addLoadingHeader();
        else isLastPage = true;
    }

    public void deleteHistory() {
        dbHelper.deleteHistory();
    }
}
