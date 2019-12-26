package com.siggytech.utils.communication;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Created by fsoto on 11/26/19.
 */

public class ChatControl extends RelativeLayout {
    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_WRITE = 2;
    private ListView mConversationView;
    private EditText mOutEditText;
    private EditText mServerAddress;
    private Button mSendButton;
    private ArrayAdapter<String> mConversationArrayAdapter;
    public String TAG = "ChatControl";

    public String imei;
    public String name;
    public String api_key;
    public String idGroup;
    private Context context;


    public ChatControl(Context context, int idGroup, String API_KEY, String nameClient){
        super(context);
        initLayout(context);
        this.context = context;
        this.idGroup = String.valueOf(idGroup);
        this.api_key = API_KEY;
        this.name = nameClient;
        this.imei = getIMEINumber();

        webSocketConnection();
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
    public void initLayout(Context context) {

        ViewGroup.LayoutParams root_LayoutParams =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        root_LayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        root_LayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        //root.setPadding((int) getResources().getDimension(R.dimen.activity_horizontal_margin), (int) getResources().getDimension(R.dimen.activity_vertical_margin), 0, (int) getResources().getDimension(R.dimen.activity_vertical_margin));
        //tools:context="com.example.androidtest.MainActivity"    //not support
        this.setLayoutParams(root_LayoutParams);

        FrameLayout abc = new FrameLayout(context);
        RelativeLayout.LayoutParams abc_LayoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        //abc.setId(@id/abc);
        abc_LayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        abc_LayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        abc_LayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        abc_LayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        this.addView(abc);
        abc.setLayoutParams(abc_LayoutParams);

        TextView textView1 = new TextView(context);
        FrameLayout.LayoutParams textView1_LayoutParams =
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        //textView1.setId(@ id/textView1);
        textView1.setText("Small Text");
        //android:textAppearance="?abc"    //not support
        abc.addView(textView1);
        textView1.setLayoutParams(textView1_LayoutParams);

    }
    private void setupChat(){
        //udpSocket = new UDPSocket(mHandler,1984);
        //udpSocket.startRecv();         // Empieza a escuchar
        // Inicialización de la interfaz

        /*
        mConversationArrayAdapter=new ArrayAdapter<String>(this, R.layout.list_item);
        mConversationView= findViewById(R.id.list_conversation);
        mConversationView.setAdapter(mConversationArrayAdapter);
        mOutEditText= findViewById(R.id.edit_text_out);
        mServerAddress = findViewById(R.id.edit_server_address);
        mServerAddress.setText(address);
        mSendButton = findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = mOutEditText.getText().toString();
                String [] addr = mServerAddress.getText().toString().split(":");
                //udpSocket.Send(message,addr[0],Integer.parseInt(addr[1]));
                //udpSocketActivity.Send(message,addr[0],Integer.parseInt(addr[1]));
            }
        });
        */
    }
    private void webSocketConnection(){

        WebSocketListener webSocketListenerCoinPrice;
        OkHttpClient clientCoinPrice = new OkHttpClient();

        String url = "ws://" + Conf.SERVER_IP + ":" + Conf.SERVER_CHAT_PORT + "?imei=" + imei + "&groupId=" + idGroup + "&API_KEY="+ api_key +"&clientName=" + name;
        Log.e(TAG, url);

        Request requestCoinPrice = new Request.Builder().url(url).build();

        webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                //webSocket.send("{ \"packageName\": \"packageName\", \"messageText\": \"messageText\", \"messageTittle\": \"messageTittle\", \"from\": \"BLUEBIRD1\" }");
                Log.e(TAG, "onOpen");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {

                try {
                    Log.e(TAG, "MESSAGE String: " + text);
                    JSONObject obj = new JSONObject(text);

                    //addNotification(obj.getString("messageTittle"), obj.getString("messageText"), obj.getString("packageName"), obj.getInt("resIcon"));
                }
                catch(Exception ex){
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

}
