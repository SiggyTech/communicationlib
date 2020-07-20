package com.siggytech.utils.communication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketService extends Service {
    public String TAG = WebSocketService.class.getSimpleName();

    private int sampleRate = 44100 ;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    MediaPlayer mediaPlayer;

    AudioTrack at;
    private WebSocketListener webSocketListener;
    private OkHttpClient okHttpClient;
    private String name,imei,idGroup, apiKey;
    
    public WebSocketService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        int intSize = android.media.AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                audioFormat);
        at = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                audioFormat, intSize, AudioTrack.MODE_STREAM);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(5,new Notification());
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.siggy.websocketservice";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_s_notification)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(5, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        try {
            Bundle extras = intent.getExtras();

            if (extras == null) {
                Log.d("Service", "null");
            } else {
                Log.d("Service", "not null");
                name = extras.getString("name");
                idGroup = String.valueOf(extras.getInt("idGroup"));
                imei = extras.getString("imei");
                apiKey = extras.getString("apiKey");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        webSocketConnection();
                    }
                }).start();

            }
        } catch(Exception ex) {
            Log.d("intent.getExtras", "Error: " + ex.getMessage());
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    private void webSocketConnection(){

        okHttpClient = new OkHttpClient();

        String url = "ws://" + Conf.SERVER_IP + ":" + Conf.SERVER_WS_PORT + "?imei=" + this.imei + "&groupId=" + this.idGroup + "&API_KEY="+ this.apiKey +"&clientName=" + this.name;
        Request requestCoinPrice = new Request.Builder().url(url).build();

        webSocketListener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.e(TAG, "onOpen");
                Log.d(TAG, "Abrio el socket del ptt");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.e(TAG, "MESSAGE String: " + text);
                //here receive the message when the token state is changed.

            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                try {
                    PlayShortAudioFileViaAudioTrack(bytes.toByteArray());
                } catch(Exception ex){
                    System.out.print(ex.getMessage());
                }
            }

            /**
             * Invoked when the remote peer has indicated that no more incoming messages will be transmitted
             */
            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000, null);
                webSocket.cancel();
                Log.d(TAG, "onClosing socket del ptt");
            }

            /**
             * Invoked when both peers have indicated that no more messages will be transmitted and
             * the connection has been successfully released. No further calls to this listener will
             * be made
             */
            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                //TODO: stuff
                Log.d(TAG, "onClosed SE CERRO socket del ptt");
            }

            /**
             * Invoked when a web socket has been closed due to an error reading from or writing to
             * the network. Both outgoing and incoming messages may have been lost. No further calls
             * to this listener will be made
             */
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                //TODO: stuff
                Log.d(TAG, "onFailure FALLO socket del ptt");
            }
        };

        okHttpClient.newWebSocket(requestCoinPrice, webSocketListener);
        okHttpClient.dispatcher().executorService().shutdown();
    }

    private void PlayShortAudioFileViaAudioTrack(byte[] byteData) throws IOException {
        if (at!=null) {
            at.write(byteData, 0, byteData.length);
            at.play();
        }
        else Log.d("TCAudio", "audio track is not initialised ");
    }

}
