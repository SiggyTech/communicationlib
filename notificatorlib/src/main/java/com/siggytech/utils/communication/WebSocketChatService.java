package com.siggytech.utils.communication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.siggytech.utils.communication.repo.DbHelper;
import com.siggytech.utils.communication.repo.MessageRaw;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketChatService extends Service {
    public String TAG = WebSocketChatService.class.getSimpleName();
    public static final String MESSAGE_CHAT = "messageChat";
    private OkHttpClient messengerClient;
    private WebSocket webSocket;
    private String name,imei,idGroup, apiKey;

    private DbHelper dbHelper;

    public WebSocketChatService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.traces("WebSocketChatService onCreate");

        dbHelper = new DbHelper(WebSocketChatService.this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startMyOwnForeground();
        else startForeground(Conf.COMM_NOTIFICATION_FOREGROUND_ID,new Notification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        try {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                name = extras.getString("name");
                idGroup = String.valueOf(extras.getLong("idGroup"));
                imei = extras.getString("imei");
                apiKey = extras.getString("apiKey");
                new Thread(this::messengerWebSocketConnection).start();
            }
        } catch(Exception ex) {
            Log.d("intent.getExtras", "Error: " + ex.getMessage());
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (webSocket != null) webSocket.close(1000,"onDestroy");
        }catch (Exception e){
            Utils.traces("WebSocketChatService onDestroy Ex: "+Utils.exceptionToString(e));
        }
        try {
            if (dbHelper!=null) dbHelper.close();
        }catch (Exception e){
            Utils.traces("WebSocketChatService onDestroy Ex: "+Utils.exceptionToString(e));
        }
        Utils.traces("WebSocketChatService onDestroy");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        NotificationChannel chan = new NotificationChannel(Conf.COMM_NOTIFICATION_CHANNEL_ID, Conf.COMM_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, Conf.COMM_NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_s_notification)
                .setContentTitle(Conf.COMM_NOTIFICATION_CONTENT_TITLE)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(Conf.COMM_NOTIFICATION_FOREGROUND_ID, notification);
    }

    /**
     * Manages messenger web socket connection
     */
    private void messengerWebSocketConnection(){
        try {
            messengerClient = new OkHttpClient();

            String url = "ws://" + Conf.SERVER_IP + ":" + Conf.SERVER_CHAT_PORT + "?imei=" + this.imei + "&groupId=" + this.idGroup + "&API_KEY=" + this.apiKey + "&clientName=" + this.name;
            Log.e(TAG, url);

            Request requestCoinPrice = new Request.Builder().url(url).build();

            WebSocketListener webSocketListenerMessenger = new WebSocketListener() {
                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    Utils.traces("messengerWebSocketConnection onOpen");
                }

                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    try {
                        Utils.traces("messengerWebSocketConnection onMessage");
                        MessageRaw messageRaw = new MessageRaw();
                        JSONObject jObject = new JSONObject(text);
                        messageRaw.setIdGroup(new JSONObject(jObject.getString("data")).getString("idGroupFrom"));
                        messageRaw.setFrom(new JSONObject(jObject.getString("data")).getString("from"));
                        messageRaw.setMessage(new JSONObject(jObject.getString("data")).getString("text"));
                        messageRaw.setDate(Utils.getStringDate());
                        messageRaw.setMine(0);
                        messageRaw.setUserKey(apiKey);

                        if(dbHelper==null) dbHelper = new DbHelper(WebSocketChatService.this);

                        long id = dbHelper.insertMessage(messageRaw);

                        if(id==-1){
                            Utils.traces("messengerWebSocketConnection onMessage error on bd insert");
                        }

                        Intent intent = new Intent(WebSocketChatService.this,MessengerReceiver.class);
                        intent.putExtra(MESSAGE_CHAT, text);
                        sendBroadcast(intent);
                    } catch (Exception ex) {
                        Utils.traces("messengerWebSocketConnection onMessage: "+ Utils.exceptionToString(ex));
                    }
                }

                @Override
                public void onMessage(WebSocket webSocket, ByteString bytes) {
                    try {
                        Log.e(TAG, "MESSAGE bytes: " + bytes.hex()); //
                    } catch (Exception ex) {
                        System.out.print(ex.getMessage());
                    }
                }

                @Override
                public void onClosing(WebSocket webSocket, int code, String reason) {
                    webSocket.close(1000, null);
                    webSocket.cancel();
                    Utils.traces("messengerWebSocketConnection onClosing code:"+code+" reason: "+reason);
                }

                @Override
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    Utils.traces("messengerWebSocketConnection onClosed code:"+code+" reason: "+reason);
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    messengerWebSocketConnection();
                }
            };

            webSocket = messengerClient.newWebSocket(requestCoinPrice, webSocketListenerMessenger);
            messengerClient.dispatcher().executorService().shutdown();
        }catch (Exception e){
            Utils.traces("messengerWebSocketConnection catch: "+Utils.exceptionToString(e));
        }
    }

}
