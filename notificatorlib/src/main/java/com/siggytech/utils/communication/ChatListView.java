package com.siggytech.utils.communication;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ListView;

import org.json.JSONObject;

import java.util.ArrayList;
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
    private String name;
    private boolean newMessage = false;
    private String messageText;
    private String from;
    private String dateTime;

    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

            if(newMessage) {
                lsChat.add(new ChatModel(1L, messageText, from)); //TODO agregar fecha a la caja de texto
                SetAdapter();
                newMessage = false;
            }
            timerHandler.postDelayed(timerRunnable,1000);
        }
    };

    public ChatListView (Context context, int idGroup, String API_KEY, String nameClient){
        super(context);
        this.context = context;
        this.idGroup = idGroup;
        this.API_KEY = API_KEY;
        this.name = nameClient;

        customAdapterBubble = new CustomAdapterBubble(lsChat, context);
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
                    from = jObject.getString("from");
                    messageText = jObject.getString("messageText");
                    dateTime = jObject.getString("dateTime");
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
