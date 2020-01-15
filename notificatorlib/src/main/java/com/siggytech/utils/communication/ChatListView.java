package com.siggytech.utils.communication;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ListView;

import org.json.JSONObject;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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

    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

            if(newMessage) {
                try{
                    newMessage = false;
                    lsChat.add(new ChatModel(1L, AESUtils.decrypt(messageText), from, dateTime)); //TODO agregar fecha a la caja de texto
                    SetAdapter();
                }
                catch (Exception e){e.printStackTrace();}

            }

            //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            //StrictMode.setThreadPolicy(policy);

            //String url = "ws://" + Conf.SERVER_IP + ":" + Conf.SERVER_CHAT_PORT + "?imei=" + imei + "&groupId=" + idGroup + "&API_KEY="+ API_KEY +"&clientName=" + name;

            //Socket socket = Socket.Builder.with(url).build().connect();
            //socket.onEvent(Socket.EVENT_OPEN, socketOpenListener);
            //socket.onEvent(Socket.EVENT_RECONNECT_ATTEMPT, .....);
            //socket.onEvent(Socket.EVENT_CLOSED, .....);
            //socket.onEventResponse("Some event", socketPairListener);
            //socket.send( "Some Event", "{\n" +
            //        "    \"type\": \"subscribe\",\n" +
            //        "    \"channels\": [{ \"name\": \"ticker\", \"product_ids\": [\"product\"] }]\n" +
            //        "}");
            //socket.sendOnOpen("Some event", "{\n" +
            //        "    \"type\": \"subscribe\",\n" +
            //        "    \"channels\": [{ \"name\": \"ticker\", \"product_ids\": [\"product\"] }]\n" +
            //        "}");

            timerHandler.postDelayed(timerRunnable,100);
        }
    };

    public void sendMessage(String from, String text, String dateTime){
        try{
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        String url = "ws://" + Conf.SERVER_IP + ":" + Conf.SERVER_CHAT_PORT + "?imei=" + imei + "&groupId=" + idGroup + "&API_KEY="+ API_KEY +"&clientName=" + name;

        Socket socket = Socket.Builder.with(url).build().connect();
        socket.sendOnOpen("Message", "{\n" +
                "    \"from\": \"" + from +  "\",\n" +
                "    \"text\": \"" + text +  "\", \n" +
                "    \"dateTime\": \"" + dateTime +  "\" \n" +
                "}");

        lsChat.add(new ChatModel(1L, AESUtils.decrypt(text), Conf.LOCAL_USER, dateTime)); //TODO agregar fecha a la caja de texto y from
        SetAdapter();
        }
        catch(Exception e){e.printStackTrace();}
    }
    public ChatListView (Context context, int idGroup, String API_KEY, String nameClient){
        super(context);
        this.context = context;
        this.idGroup = idGroup;
        this.API_KEY = API_KEY;
        this.name = nameClient;

        imei = getIMEINumber();

        timerHandler.postDelayed(timerRunnable,0);

        try {
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
                webSocket.send("{\n" +
                        "    \"type\": \"subscribe\",\n" +
                        "    \"channels\": [{ \"name\": \"ticker\", \"product_ids\": [\"product\"] }]\n" +
                        "}");
                Log.e(TAG, "onOpen");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.e(TAG, "MESSAGE String: " + text); //here comes the message
                try {
                    JSONObject jObject = new JSONObject(text);
                    from = new JSONObject(jObject.getString("data")).getString("from");
                    messageText = new JSONObject(jObject.getString("data")).getString("text");
                    dateTime = new JSONObject(jObject.getString("data")).getString("dateTime");
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
