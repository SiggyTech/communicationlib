package com.siggytech.utils.notificatorlib;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.StateSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


import com.siggytech.utils.notificatorlib.greendao.NetworkConnection;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import static android.content.Context.TELEPHONY_SERVICE;

public class PTTButton extends AppCompatButton implements View.OnTouchListener {
    private Padding mPadding;
    private int mHeight;
    private int mWidth;
    private int mColor;
    private int mCornerRadius;
    private int mStrokeWidth;
    private int mStrokeColor;
    protected boolean mAnimationInProgress;
    private StrokeGradientDrawable mDrawableNormal;
    private StrokeGradientDrawable mDrawablePressed;
    private Context context;
    private static final int REQUEST = 112;
    Activity activity;
    AudioTrack at;
    public Handler mHandlerTimer;

    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_WRITE = 2;
    private int sampleRate = 44100 ;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    private boolean status = true;
    public AudioRecord recorder;
    public NetworkConnection networkConnection;
    private static final int READ_PHONE_STATE = 0;
    private int idGroup;
    WebSocketListener webSocketListenerCoinPrice;
    OkHttpClient clientCoinPrice = new OkHttpClient();
    WebSocket webSocketX;

    private UDPSocket udpSocket;
    private String API_KEY;
    private boolean flagListen = true;

    private String TAG = "PTTButton";


    private OkHttpClient client;

    boolean toServer;

    public PTTButton(Context context, Activity activity, int idGroup, String API_KEY) {
        super(context);
        this.context = context;
        this.activity = activity;
        this.idGroup = idGroup;
        this.API_KEY = API_KEY;



        initView();
    }
    public PTTButton(Context context, Activity activity, int idGroup, String API_KEY, boolean toserver, String ip, int port) {
        super(context);
        this.context = context;
        this.activity = activity;
        this.idGroup = idGroup;
        this.API_KEY = API_KEY;

        this.toServer = toserver;
        Conf.SERVER_IP = ip;
        Conf.SERVER_PORT = port;

        initView();
    }
    CountDownTimer timer = new CountDownTimer(500, 100) {
        public void onTick(long millisUntilFinished) {
            //here you can have your logic to set text to edittext
        }
        public void onFinish() {
            unblockTouch();
        }
    };
    CountDownTimer timer2 = new CountDownTimer(3000, 1000) {
        public void onTick(long millisUntilFinished) {
            //here you can have your logic to set text to edittext
        }
        public void onFinish() {
            flagListen = true;
        }
    };


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mHeight == 0 && mWidth == 0 && w != 0 && h != 0) {
            mHeight = getHeight();
            mWidth = getWidth();
        }
    }

    @Override
    public boolean onTouch(View arg0, MotionEvent arg1) {

        switch (arg1.getAction()){
            case MotionEvent.ACTION_DOWN:
                System.out.println("Button pressed");
                status = true;
                //subscribeThread.stop();
                break;
            case MotionEvent.ACTION_UP:
                System.out.println("Button released");
                status = false;
                recorder.release();
                //subscribeThread.resume();
                break;
        }
       return true;
    }

    public void startStreaming() {







        Thread streamThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    DatagramSocket socket = new DatagramSocket();
                    Log.d("VS", "Socket Created");

                    minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
                    byte[] buffer = new byte[minBufSize];

                    Log.d("VS","Buffer created of size " + minBufSize);
                    DatagramPacket packet;

                    //final InetAddress destination = InetAddress.getByName(Conf.SERVER_IP);
                    //Log.d("VS", "Address retrieved");

                    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize*10);
                    Log.d("VS", "Recorder initialized");
                    InetAddress destination = InetAddress.getByName(Conf.SERVER_IP);


                    recorder.startRecording();


                    while(status == true) {

                        //flagListen = false;

                        //reading data from MIC into buffer
                        minBufSize = recorder.read(buffer, 0, buffer.length);

                        //publishMessage(buffer);

                        if(toServer){
                            packet = new DatagramPacket(buffer, buffer.length, destination, Conf.SERVER_PORT);
                            //socket.send(packet); //TODO revisando si funciona mejor la comunicacion con websocket
                            webSocketX.send(ByteString.of(buffer));
                        }
                        else {
                            System.out.println("to server false ");
                        }
                        System.out.println("MinBufferSize: " +minBufSize);

                        /*Looper.prepare();

                        mHandlerTimer = new Handler() {
                            public void handleMessage(Message msg) {
                                timer2 = new CountDownTimer(3000, 1000) {
                                    public void onTick(long millisUntilFinished) {
                                        //here you can have your logic to set text to edittext
                                    }

                                    public void onFinish() {
                                        flagListen = true;
                                    }
                                };
                            }
                        };

                        Looper.loop();*/

                    }



                //} catch(UnknownHostException e) {
                //    Log.e("VS", "UnknownHostException");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("VS", "IOException");
                }
            }

        });
        streamThread.start();
    }


    public StrokeGradientDrawable getDrawableNormal() {
        return mDrawableNormal;
    }
    public void animation(@NonNull Params params) {
        if (!mAnimationInProgress) {
            mDrawablePressed.setColor(params.colorPressed);
            mDrawablePressed.setCornerRadius(params.cornerRadius);
            mDrawablePressed.setStrokeColor(params.strokeColor);
            mDrawablePressed.setStrokeWidth(params.strokeWidth);
            if (params.duration == 0) {
                aniBtWithoutAnimation(params);
            } else {
                aniBtWithAnimation(params);
            }
            mColor = params.color;
            mCornerRadius = params.cornerRadius;
            mStrokeWidth = params.strokeWidth;
            mStrokeColor = params.strokeColor;
        }
    }
    private void aniBtWithAnimation(@NonNull final Params params) {
        mAnimationInProgress = true;
        setText(null);
        setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        setPadding(mPadding.left, mPadding.top, mPadding.right, mPadding.bottom);
        CustomButtonAnimation.Params animationParams = CustomButtonAnimation.Params.create(this)
                .color(mColor, params.color)
                .cornerRadius(mCornerRadius, params.cornerRadius)
                .strokeWidth(mStrokeWidth, params.strokeWidth)
                .strokeColor(mStrokeColor, params.strokeColor)
                .height(getHeight(), params.height)
                .width(getWidth(), params.width)
                .duration(params.duration)
                .listener(new CustomButtonAnimation.Listener() {
                    @Override
                    public void onAnimationEnd() {
                        finalizeAnimation(params);
                    }
                });
        CustomButtonAnimation animation = new CustomButtonAnimation(animationParams);
        animation.start();
    }
    private void aniBtWithoutAnimation(@NonNull Params params) {
        mDrawableNormal.setColor(params.color);
        mDrawableNormal.setCornerRadius(params.cornerRadius);
        mDrawableNormal.setStrokeColor(params.strokeColor);
        mDrawableNormal.setStrokeWidth(params.strokeWidth);
        if(params.width != 0 && params.height !=0) {
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            layoutParams.width = params.width;
            layoutParams.height = params.height;
            setLayoutParams(layoutParams);
        }
        finalizeAnimation(params);
    }
    private void finalizeAnimation(@NonNull Params params) {
        mAnimationInProgress = false;
        if (params.icon != 0 && params.text != null) {
            setIconLeft(params.icon);
            setText(params.text);
        } else if (params.icon != 0) {
            setIcon(params.icon);
        } else if(params.text != null) {
            setText(params.text);
        }
        if (params.animationListener != null) {
            params.animationListener.onAnimationEnd();
        }
    }
    public void blockTouch() {

        this.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }
    public void unblockTouch() {
        this.getBackground().setColorFilter(null);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
    }

    private void webSocketConnection(){

        Request requestCoinPrice = new Request.Builder().url("ws://" + Conf.SERVER_IP + ":8080").build();

        webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                webSocketX = webSocket;
                /*webSocket.send("{\n" +
                        "    \"type\": \"subscribe\",\n" +
                        "    \"channels\": [{ \"name\": \"ticker\", \"product_ids\": [\"product\"] }]\n" +
                        "}");*/
                Log.e(TAG, "onOpen");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.e(TAG, "MESSAGE: " + text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {

                //if(!flagListen)
                //    return;


                Log.e(TAG, "MESSAGE: " + bytes.hex());
                try
                {
                    blockTouch();

                    timer.cancel();
                    timer = new CountDownTimer(500, 100) {
                        public void onTick(long millisUntilFinished) {
                            //here you can have your logic to set text to edittext
                        }
                        public void onFinish() {
                            unblockTouch();
                        }
                    };

                    PlayShortAudioFileViaAudioTrack(bytes.toByteArray());
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

    private void initView() {

        client = new OkHttpClient();
        webSocketConnection();


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if ( ContextCompat.checkSelfPermission( activity, android.Manifest.permission.READ_PHONE_STATE ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions(activity, new String[] {  android.Manifest.permission.READ_PHONE_STATE  },
                    READ_PHONE_STATE );
        }

        /*networkConnection = new NetworkConnection();
        networkConnection.register(Long.parseLong(getIMEINumber()), getIMEINumber(), API_KEY, 1, getIP(), Conf.LOCAL_PORT);


        if(!toServer) {
            networkConnection.getDestList(getIMEINumber(), context, 1, API_KEY);

            final String di = getIMEINumber();
            countDownTimer = new CountDownTimer(Long.MAX_VALUE, 10000) {

                // This is called after every 10 sec interval.
                public void onTick(long millisUntilFinished) {
                    networkConnection.register(Long.parseLong(di), di,API_KEY, 1, getIP(), Conf.LOCAL_PORT);

                    networkConnection.getDestList(di, context, 1, API_KEY);
                }

                public void onFinish() {
                    start();
                }
            }.start();

            setDestinationList();
        }*/
        stopService();


        udpSocket = new UDPSocket(mHandler,Conf.LOCAL_PORT);
        udpSocket.startRecv();

        int intSize = android.media.AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        at = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, intSize, AudioTrack.MODE_STREAM);


        this.setOnTouchListener(new View.OnTouchListener()
        {
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                    {
                        Log.d("log", "onTouch: push");
                        status = true;

                        startStreaming();

                        break;
                    }

                    case MotionEvent.ACTION_UP:
                    {
                        Log.d("log", "onTouch: release");
                        status = false;
                        recorder.release();
                        udpSocket.startRecv();
                        //setDestinationList();

                        break;
                    }
                }

                return false;
            }
        });

        mPadding = new Padding();
        mPadding.left = getPaddingLeft();
        mPadding.right = getPaddingRight();
        mPadding.top = getPaddingTop();
        mPadding.bottom = getPaddingBottom();
        Resources resources = getResources();
        int cornerRadius = (int) resources.getDimension(R.dimen.bt_corner_radius_2);
        int blue = resources.getColor(R.color.bt_purple);
        int blueDark = resources.getColor(R.color.bt_purple_dark);
        StateListDrawable background = new StateListDrawable();
        mDrawableNormal = createDrawable(blue, cornerRadius, 0);
        mDrawablePressed = createDrawable(blueDark, cornerRadius, 0);
        mColor = blue;
        mStrokeColor = blue;
        mCornerRadius = cornerRadius;
        background.addState(new int[]{android.R.attr.state_pressed}, mDrawablePressed.getGradientDrawable());
        background.addState(StateSet.WILD_CARD, mDrawableNormal.getGradientDrawable());
        setBackgroundCompat(background);
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

    @NonNull
    private String getIP() {

                String actualConnectedToNetwork = null;
                ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connManager != null) {
                        {
                            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                            if (mWifi.isConnected()) {
                                actualConnectedToNetwork = getWifiIp();
                            }
                        }

                }
                if (TextUtils.isEmpty(actualConnectedToNetwork)) {
                    actualConnectedToNetwork = getNetworkInterfaceIpAddress();
                }
                if (TextUtils.isEmpty(actualConnectedToNetwork)) {
                    actualConnectedToNetwork = "127.0.0.1";
                }
                return actualConnectedToNetwork;

    }

    @Nullable
    private String getWifiIp() {
        final WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null && mWifiManager.isWifiEnabled()) {
            int ip = mWifiManager.getConnectionInfo().getIpAddress();
            return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "."
                    + ((ip >> 24) & 0xFF);
        }
        return null;
    }

    @Nullable
    public String getNetworkInterfaceIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface networkInterface = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        String host = inetAddress.getHostAddress();
                        if (!TextUtils.isEmpty(host)) {
                            return host;
                        }
                    }
                }

            }
        } catch (Exception ex) {
            Log.e("IP Address", "getLocalIpAddress", ex);
        }
        return null;
    }
    private final Handler mHandler= new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case MESSAGE_WRITE:
                    byte[]writeBuf =(byte[])msg.obj;
                    String writeMessage=new String(writeBuf);
                    break;
                case MESSAGE_READ:
                    byte[]readBuf =(byte[])msg.obj;

                    String readMessage=new String(readBuf,0,msg.arg1);
                    try {
                        PlayShortAudioFileViaAudioTrack(readBuf);
                        //playMp3(readBuf);
                    }
                    catch(Exception ex){}


                    //mConversationArrayAdapter.add("Servidor"+": " +readMessage);

                    break;
            }
        }
    };
    private void PlayShortAudioFileViaAudioTrack(byte[] byteData) throws IOException
    {
        if (at!=null) {

            at.write(byteData, 0, byteData.length);
            at.play();


        }
        else
            Log.d("TCAudio", "audio track is not initialised ");
    }
    private void stopService()
    {
        Intent intent = new Intent(context, MessengerService.class);
        context.stopService(intent);


    }

    private StrokeGradientDrawable createDrawable(int color, int cornerRadius, int strokeWidth) {
        StrokeGradientDrawable drawable = new StrokeGradientDrawable(new GradientDrawable());
        //drawable.getGradientDrawable().setShape(GradientDrawable.RECTANGLE);
        drawable.getGradientDrawable().setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        drawable.setCornerRadius(cornerRadius);
        drawable.setStrokeColor(color);
        drawable.setStrokeWidth(strokeWidth);
        return drawable;
    }
    @SuppressWarnings("deprecation")
    private void setBackgroundCompat(@Nullable Drawable drawable) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
            setBackgroundDrawable(drawable);
        } else {
            setBackground(drawable);
        }
    }
    public void setIcon(@DrawableRes final int icon) {
    // post is necessary, to make sure getWidth() doesn't return 0
        post(new Runnable() {
            @Override
            public void run() {
                Drawable drawable = getResources().getDrawable(icon);
                int padding = (getWidth() / 2) - (drawable.getIntrinsicWidth() / 2);
                setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
                setPadding(padding, 0, 0, 0);
            }
        });
    }
    public void setIconLeft(@DrawableRes int icon) {
        setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
    }
    //private BlockingDeque<String> queue = new LinkedBlockingDeque<String>();


    private class Padding {
        public int left;
        public int right;
        public int top;
        public int bottom;
    }
    public static class Params {
        private int cornerRadius;
        private int width;
        private int height;
        private int color;
        private int colorPressed;
        private int duration;
        private int icon;
        private int strokeWidth;
        private int strokeColor;
        private String text;
        private CustomButtonAnimation.Listener animationListener;
        private Params() {
        }
        public static Params create() {
            return new Params();
        }
        public Params text(@NonNull String text) {
            this.text = text;
            return this;
        }
        public Params icon(@DrawableRes int icon) {
            this.icon = icon;
            return this;
        }
        public Params cornerRadius(int cornerRadius) {
            this.cornerRadius = cornerRadius;
            return this;
        }
        public Params width(int width) {
            this.width = width;
            return this;
        }
        public Params height(int height) {
            this.height = height;
            return this;
        }
        public Params color(int color) {
            this.color = color;
            return this;
        }
        public Params colorPressed(int colorPressed) {
            this.colorPressed = colorPressed;
            return this;
        }
        public Params duration(int duration) {
            this.duration = duration;
            return this;
        }
        public Params strokeWidth(int strokeWidth) {
            this.strokeWidth = strokeWidth;
            return this;
        }
        public Params strokeColor(int strokeColor) {
            this.strokeColor = strokeColor;
            return this;
        }
        public Params animationListener(CustomButtonAnimation.Listener animationListener) {
            this.animationListener = animationListener;
            return this;
        }


    }
}
