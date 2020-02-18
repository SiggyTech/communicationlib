package com.siggytech.utils.communication;

import android.content.Context;
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
import android.os.Build;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.StateSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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

    AudioTrack at;

    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_WRITE = 2;
    private int sampleRate = 44100 ;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    private boolean status = true;
    public AudioRecord recorder;
    private static final int READ_PHONE_STATE = 0;
    private int idGroup;


    private UDPSocket udpSocket;
    private String API_KEY;
    private boolean flagListen = true;

    private String TAG = "PTTButton";
    private String buttonName;

    private OkHttpClient client;
    private String name = "";


    public PTTButton(Context context, int idGroup, String API_KEY, String nameClient) {
        super(context);
        this.context = context;
        this.idGroup = idGroup;
        this.API_KEY = API_KEY;
        this.name = nameClient;

        initView();


    }
    CountDownTimer timer;
    boolean canTalk = true;
    /*CountDownTimer timer2 = new CountDownTimer(3000, 1000) {
        public void onTick(long millisUntilFinished) {
            //here you can have your logic to set text to edittext
        }
        public void onFinish() {
            flagListen = true;
        }
    };*/


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
                return true;
            case MotionEvent.ACTION_UP:
                System.out.println("Button released");
                status = false;
                recorder.release();
                //subscribeThread.resume();
                break;
        }
        return true;
    }

    public class MyRunnable implements Runnable {
        public String message;
        public MyRunnable(String parameter) {
            this.message = parameter;
        }
        public void run() {
        }
    }
    public void startStreaming() {
        String message = "{ \"name\": \"" + this.name + "\",\"imei\": "+ this.getIMEINumber() +", \"api_key\": \"" + this.API_KEY + "\",\"idgroup\": "+ this.idGroup +" }";

        Thread streamThread = new Thread(new MyRunnable(message) {

            @Override
            public void run() {
                try {

                    DatagramSocket socket = new DatagramSocket();
                    Log.d("VS", "Socket Created");

                    Log.d("VS", "Socket Created");

                    DatagramPacket packet;

                    InetAddress destination = InetAddress.getByName(Conf.SERVER_IP);

                    packet = new DatagramPacket(this.message.getBytes(), this.message.getBytes().length, destination, Conf.SERVER_PORT);
                    socket.send(packet);



                    minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
                    byte[] buffer = new byte[minBufSize];

                    Log.d("VS","Buffer created of size " + minBufSize);


                    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize*10);
                    Log.d("VS", "Recorder initialized");

                    recorder.startRecording();

                    while(status) {
                        minBufSize = recorder.read(buffer, 0, buffer.length);
                        packet = new DatagramPacket(buffer, buffer.length, destination, Conf.SERVER_PORT);
                        socket.send(packet);
                        System.out.println("MinBufferSize: " +minBufSize);
                    }
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
                        canTalk = false;
                        buttonName = getText().toString();
                        setText("Sending...");
                        return true;
                    }

                    case MotionEvent.ACTION_UP:
                    {
                        Log.d("log", "onTouch: release");
                        status = false;
                        recorder.release();
                        blockTouch();
                        timer = new CountDownTimer(3000, 100) {
                            public void onTick(long millisUntilFinished) {
                                //here you can have your logic to set text to edittext
                            }
                            public void onFinish() {
                                canTalk = true;
                                setText(buttonName);
                                unblockTouch();
                            }
                        }.start();

                        break;
                    }
                }

                return false;
            }
        });
    }

    private void webSocketConnection(){

        WebSocketListener webSocketListenerCoinPrice;
        OkHttpClient clientCoinPrice = new OkHttpClient();

        String url = "ws://" + Conf.SERVER_IP + ":" + Conf.SERVER_WS_PORT + "?imei=" + this.getIMEINumber() + "&groupId=" + this.idGroup + "&API_KEY="+ this.API_KEY +"&clientName=" + this.name;
        //Log.e(TAG, url);

        Request requestCoinPrice = new Request.Builder().url(url).build();

        //OLD: Request requestCoinPrice = new Request.Builder().url("ws://" + Conf.SERVER_IP + ":" + Conf.SERVER_WS_PORT).build();

        webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                /*webSocket.send("{\n" +
                        "    \"type\": \"subscribe\",\n" +
                        "    \"channels\": [{ \"name\": \"ticker\", \"product_ids\": [\"product\"] }]\n" +
                        "}");*/
                //Log.e(TAG, "onOpen");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                //Log.e(TAG, "MESSAGE String: " + text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {


                //Log.e(TAG, "MESSAGE bytes: " + bytes.hex());
                try
                {

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
                // Log.e(TAG, "CLOSE: " + code + " " + reason);
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

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        client = new OkHttpClient();
        try {
            webSocketConnection();
        }
        catch(Exception ex){
            Log.e(TAG, "error en webSocketConnection: " + ex.getMessage());
        }

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
                        canTalk = false;
                        buttonName = getText().toString();
                        setText("Sending...");
                        return true;
                    }

                    case MotionEvent.ACTION_UP:
                    {
                        Log.d("log", "onTouch: release");
                        status = false;
                        recorder.release();
                        blockTouch();
                        timer = new CountDownTimer(3000, 100) {
                            public void onTick(long millisUntilFinished) {
                                //here you can have your logic to set text to edittext
                            }
                            public void onFinish() {
                                canTalk = true;
                                setText(buttonName);
                                unblockTouch();
                            }
                        }.start();

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


    private void PlayShortAudioFileViaAudioTrack(byte[] byteData) throws IOException {
        if (at!=null) {
            at.write(byteData, 0, byteData.length);
            at.play();
        }
        else
            Log.d("TCAudio", "audio track is not initialised ");
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
