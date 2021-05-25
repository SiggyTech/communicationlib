package com.siggytech.utils.communication.presentation.service;

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

import com.siggytech.utils.communication.R;
import com.siggytech.utils.communication.util.Conf;
import com.siggytech.utils.communication.util.Utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketPTTService extends Service {
    public String TAG = WebSocketPTTService.class.getSimpleName();
    public static final String MESSAGE_PTT = "messagePtt";
    private OkHttpClient pttClient;
    private WebSocket webSocket;
    private String name,imei,idGroup, apiKey, username;
    
    public WebSocketPTTService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startMyOwnForeground();
        else startForeground(Conf.COMM_NOTIFICATION_FOREGROUND_ID,new Notification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        try {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                username = extras.getString("username");
                name = extras.getString("name");
                idGroup = String.valueOf(extras.getLong("idGroup"));
                imei = extras.getString("imei");
                apiKey = extras.getString("apiKey");

                new Thread(this::pttWebSocketConnection).start();
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
        Utils.traces("WebSocketPTTService onDestroy");
        try {
            if (webSocket != null) webSocket.close(1000,"onDestroy");
        }catch (Exception e){
            Utils.traces("WebSocketPTTService onDestroy Ex: "+Utils.exceptionToString(e));
        }
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
     * Manages ptt web socket connection
     */
    private void pttWebSocketConnection(){
        try {
            pttClient = new OkHttpClient();

            String url = "ws://" + Conf.SERVER_IP + ":" + Conf.SERVER_WS_PORT + "?imei=" + this.imei + "&groupId=" + this.idGroup + "&API_KEY=" + this.apiKey + "&clientName=" + this.name + "&username=" + this.username;
            Request requestCoinPrice = new Request.Builder().url(url).build();

            WebSocketListener webSocketListener = new WebSocketListener() {
                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    Utils.traces("PttWebSocketConnection onOpen");
                }

                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    Log.e(TAG, "MESSAGE String: " + text);
                    //here receive the message when the token state is changed.
                }

                @Override
                public void onMessage(WebSocket webSocket, ByteString bytes) {
                    try {
                        Intent intent = new Intent(WebSocketPTTService.this, MessengerReceiver.class);
                        intent.putExtra(MESSAGE_PTT, bytes.toByteArray());
                        sendBroadcast(intent);
                    } catch (Exception ex) {
                        Utils.traces("PttWebSocketConnection onMessage: "+ Utils.exceptionToString(ex));
                    }
                }

                /**
                 * Invoked when the remote peer has indicated that no more incoming messages will be transmitted
                 */
                @Override
                public void onClosing(WebSocket webSocket, int code, String reason) {
                    webSocket.close(1000, null);
                    webSocket.cancel();
                    Utils.traces("PttWebSocketConnection onClosing code: "+code+" reason: "+reason);
                }

                /**
                 * Invoked when both peers have indicated that no more messages will be transmitted and
                 * the connection has been successfully released. No further calls to this listener will
                 * be made
                 */
                @Override
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    Utils.traces("PttWebSocketConnection onClosed code: "+code+" reason: "+reason);
                }

                /**
                 * Invoked when a web socket has been closed due to an error reading from or writing to
                 * the network. Both outgoing and incoming messages may have been lost. No further calls
                 * to this listener will be made
                 */
                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    pttWebSocketConnection();
                }
            };

            webSocket = pttClient.newWebSocket(requestCoinPrice, webSocketListener);
            pttClient.dispatcher().executorService().shutdown();
        }catch (Exception e){
            Utils.traces("PttWebSocketConnection catch: "+Utils.exceptionToString(e));
        }
    }

}
