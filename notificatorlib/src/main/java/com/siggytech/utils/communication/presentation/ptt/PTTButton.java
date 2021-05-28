package com.siggytech.utils.communication.presentation.ptt;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.siggytech.utils.communication.R;
import com.siggytech.utils.communication.model.GroupModel;
import com.siggytech.utils.communication.model.GroupRequestModel;
import com.siggytech.utils.communication.model.PairRegisterModel;
import com.siggytech.utils.communication.model.async.ApiAsyncTask;
import com.siggytech.utils.communication.model.async.ApiEnum;
import com.siggytech.utils.communication.model.async.ApiListener;
import com.siggytech.utils.communication.model.async.ApiManager;
import com.siggytech.utils.communication.model.async.TaskMessage;
import com.siggytech.utils.communication.presentation.MessengerHelper;
import com.siggytech.utils.communication.presentation.chat.CustomButtonAnimation;
import com.siggytech.utils.communication.presentation.chat.StrokeGradientDrawable;
import com.siggytech.utils.communication.presentation.register.Siggy;
import com.siggytech.utils.communication.presentation.service.WebSocketPTTService;
import com.siggytech.utils.communication.util.Conf;
import com.siggytech.utils.communication.util.FileUtil;
import com.siggytech.utils.communication.util.Utils;
import com.siggytech.utils.communication.util.vad.VoiceRecorder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static android.media.AudioRecord.RECORDSTATE_RECORDING;
import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

//TODO import com.konovalov.vad.VadConfig;

/**
 *
 * @author Siggy Technologies
 */
public class PTTButton extends AppCompatButton implements View.OnTouchListener, VoiceRecorder.Listener, ApiListener<TaskMessage> {
    public static final String TOKEN_RELEASED_ERROR = "tokenReleasedError";
    private final String TAG = PTTButton.class.getSimpleName();
    private static final String TOKEN_TAKEN = "token taked";
    public static final String TOKEN_RELEASED = "token released";

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
    private String buttonName;
    private String sendingText = "";


    private int groupIndex = 0;


    AudioTrack at;

    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_WRITE = 2;
    private int sampleRate = 44100 ;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    public AudioRecord recorder;
    CountDownTimer timer;

    private String deviceToken;
    private String API_KEY;
    private String name;

    private final Context context;
    private String username;

    //TODO private VadConfig.SampleRate DEFAULT_SAMPLE_RATE = VadConfig.SampleRate.SAMPLE_RATE_16K;
    //TODO private VadConfig.FrameSize DEFAULT_FRAME_SIZE = VadConfig.FrameSize.FRAME_SIZE_160;
    //TODO private VadConfig.Mode DEFAULT_MODE = VadConfig.Mode.VERY_AGGRESSIVE;
    private int DEFAULT_SILENCE_DURATION = 500;
    private int DEFAULT_VOICE_DURATION = 500;

    //TODO private VoiceRecorder vadRecorder;
    //TODO private VadConfig config;
    private boolean isTalking = false;
    private boolean voiceDetectionActivated;

    private final CallBack callBack;

    public PTTButton(Context context, String API_KEY, String nameClient, String username, int quality, boolean voiceDetection, CallBack callBack) {
        super(context);
        this.context = context;
        this.API_KEY = API_KEY;
        this.name = nameClient;
        this.username = username;
        this.voiceDetectionActivated = voiceDetection;
        this.deviceToken = Siggy.getDeviceToken();
        this.callBack = callBack;

        FileUtil.createFolder(Conf.ROOT_FOLDER,"");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        switch (quality){
            case AudioQuality.HIGH:
                sampleRate = 44100 ;
                channelConfig = AudioFormat.CHANNEL_IN_MONO;
                audioFormat = AudioFormat.ENCODING_PCM_16BIT;
                minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
                break;
            case AudioQuality.MEDIUM:
                sampleRate = 8000; //44100, 22050, 11025, 8000
                channelConfig = AudioFormat.CHANNEL_IN_MONO;
                audioFormat = AudioFormat.ENCODING_PCM_16BIT;
                minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
                break;
            case AudioQuality.LOW:
                sampleRate = 8000; //44100, 22050, 11025, 8000
                channelConfig = AudioFormat.CHANNEL_IN_MONO;
                audioFormat = AudioFormat.ENCODING_PCM_8BIT;
                minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
                break;
        }

        setTokenPair();

        init();

        if(voiceDetection){
            setVadRecorder();
            //TODO vadRecorder.start();
        }
    }

    private void init() {
        List<GroupModel> groupList = new ArrayList<>();
        groupList.add(new GroupModel(9999, "Every Group"));
        MessengerHelper.setPttGroupList(groupList);

        initView();

        getGroups();

        MessengerHelper.setPttButton(this);
    }

    private void setTokenPair() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    new Thread(() -> {
                        ApiManager apiManager = new ApiManager();
                        TaskMessage message = apiManager.setFirebaseTokenPtt(new PairRegisterModel(Siggy.getDeviceToken(),task.getResult(), username));
                        Utils.traces("Firebase Token: "+task.getResult());
                        Log.e(TAG,"On Pair register: "+message.getMessage());
                    }).start();

                });
    }


    public void startVoiceActivation(){
        setVadRecorder();
        //TODO vadRecorder.start();
    }
    public void stopVoiceActivation(){
        //TODO vadRecorder.stop();
    }

    public void setVadRecorder(){
        /**TODO  config = VadConfig.newBuilder()
                .setSampleRate(DEFAULT_SAMPLE_RATE)
                .setFrameSize(DEFAULT_FRAME_SIZE)
                .setMode(DEFAULT_MODE)
                .setSilenceDurationMillis(DEFAULT_SILENCE_DURATION)
                .setVoiceDurationMillis(DEFAULT_VOICE_DURATION)
                .build();
          vadRecorder = new VoiceRecorder(this, config);
         **/
    }


    private void getGroups(){
        new ApiAsyncTask(this).execute(ApiEnum.GET_PTT_GROUPS,new GroupRequestModel(deviceToken,API_KEY));
    }

    private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) { releaseTokenState();
        }
    };

    /**
     * You must override onResume() at you activity and call this method.
     */
    public void onResume(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(CONNECTIVITY_ACTION);
        context.registerReceiver(mNetworkReceiver, filter);
    }

    /**
     * You must override onPause() at you activity and call this method.
     */
    public void onPause(){
        if(mNetworkReceiver!=null)
            context.unregisterReceiver(mNetworkReceiver);
    }

    /**
     * Starts recording
     * @return returns true if has internet connection.
     */
    public boolean startTalking(){

        if(Utils.isConnect(context)) {
            setPressed(true);
            if (requestToken()) {
                startStreaming();
                buttonName = getText().toString();
                setText(sendingText);

                MediaPlayer mp = MediaPlayer.create(context, R.raw.out);
                mp.start();
            } else {
                MediaPlayer mp = MediaPlayer.create(context, R.raw.busy);
                mp.start();
                setPressed(false);
                releaseTokenState();
                isTalking = false;
            }
        }else return false;
        return true;
    }

    /**
     * Stops recording
     */
    public boolean stopTalking(){
        if(isRecording()) {
            try {
                isTalking = false;
                recorder.release();
                blockTouch();
                leaveToken();



                MediaPlayer mp = MediaPlayer.create(context, R.raw.in);
                mp.start();
                timer = new CountDownTimer(3000, 100) {
                    public void onTick(long millisUntilFinished) {
                        //here you can have your logic to set text to edittext
                    }
                    public void onFinish() {
                        setText(buttonName);
                        unblockTouch();
                        if(isVoiceDetectionActivated()){
                            startVoiceActivation();
                        }
                    }
                }.start();
            } catch (Exception e) {
                Log.e("log", "stopTalking: " + e.getMessage()); //here new line
            }
            setPressed(false);
            return true;
        }else return false;
    }

    /**
     * Checks recording state.
     * @return true if recording
     */
    public boolean isRecording(){
        return (recorder!=null && recorder.getState() == AudioRecord.STATE_INITIALIZED &&
                recorder.getRecordingState() == RECORDSTATE_RECORDING);
    }

    /**
     * Checks release token state
     */
    private void releaseTokenState() {
        //checks if token is taken
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences!=null && preferences.getBoolean(TOKEN_RELEASED_ERROR, false)) {
            leaveToken();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                startTalking();
                return true;
            }

            case MotionEvent.ACTION_UP: {
                stopTalking();
                break;
            }
        }
        return false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mHeight == 0 && mWidth == 0 && w != 0 && h != 0) {
            mHeight = getHeight();
            mWidth = getWidth();
        }
    }

    /**
     * Releases the recorder if it is recording when the button is not pressed
     */
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        for(int x : getBackground().getState())
            if(x != android.R.attr.state_pressed)
                if(isRecording())
                    stopTalking();
    }

    @Override
    public void onPreExecute() {
        callBack.onPreExecute();
    }

    @Override
    public void onPostExecute(TaskMessage result) {
        callBack.onReady(result);
    }

    @Override
    public void onCancelled(TaskMessage result) {
        callBack.onReady(result);
    }

    public void setGroup(long idGroup) {
        try{
            int pos = 0;
            for(GroupModel g : MessengerHelper.getGroupList()){
                if(g.idGroup == idGroup){
                    break;
                }
                pos++;
            }

            this.groupIndex =pos;
        }catch (Exception e){
            Utils.traces("setGroup PTT ex: "+Utils.exceptionToString(e));
        }
    }


    public class MyRunnable implements Runnable {
        public String message;
        public MyRunnable(String parameter) {
            this.message = parameter;
        }
        public void run() {
        }
    }

    private void startStreaming() {

        float[] tempFloatBuffer = new float[3];

        if(isVoiceDetectionActivated()){
            stopVoiceActivation();
        }

        String message = "{ \"name\": \"" + this.name + "\",\"imei\": \""+ Siggy.getDeviceToken() +"\", \"api_key\": \"" + this.API_KEY + "\",\"idgroup\": \""+ MessengerHelper.getPttGroupList().get(groupIndex).idGroup +"\" }";

        Thread streamThread = new Thread(new MyRunnable(message) {

            @Override
            public void run() {
                try {

                    int noiseAux = 0;
                    DatagramSocket socket = new DatagramSocket();
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

                    while(isRecording()) {

                        int tempIndex           = 0;
                        minBufSize = recorder.read(buffer, 0, buffer.length);
                        packet = new DatagramPacket(buffer, buffer.length, destination, Conf.SERVER_PORT);
                        socket.send(packet);
                        //System.out.println("MinBufferSize: " +minBufSize);
                        float totalAbsValue = 0.0f;
                        short sample        = 0;

                        if(isVoiceDetectionActivated()) {
                            // Analyze Sound.
                            for (int i = 0; i < buffer.length; i += 2) {
                                sample = (short) ((buffer[i]) | buffer[i + 1] << 8);
                                totalAbsValue += Math.abs(sample) / (minBufSize / 2);
                            }

                            // Analyze temp buffer.
                            tempFloatBuffer[tempIndex % 3] = totalAbsValue;
                            float temp = 0.0f;
                            for (int i = 0; i < 3; ++i)
                                temp += tempFloatBuffer[i];

                            //TODO research for low quality
                            if ((temp >= 0 && temp <= 350)) {
                                tempIndex++;
                                noiseAux++;
                                if (noiseAux > 50) {//number of packages of noise to stop communication
                                    Log.i("TAG", "no voice detected");
                                    //call stop talking
                                    ((Activity) context).runOnUiThread(() -> stopTalking());
                                    return;
                                }

                                continue;
                            }

                            if (temp > 350) {
                                noiseAux = 0;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("VS", "IOException");
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

        });
        streamThread.start();
    }

    /**
     * This is to know if voice detected is enabled or not
     * @return voice detection state
     */
    public boolean isVoiceDetectionActivated() {
        return voiceDetectionActivated;
    }

    /**
     * This is to enable or disable voice detection
     * @param voiceDetectionActivated voice detection
     */
    public void setVoiceDetectionActivated(boolean voiceDetectionActivated) {
        this.voiceDetectionActivated = voiceDetectionActivated;
    }

    public List<GroupModel> getGroupList(){
        return MessengerHelper.getPttGroupList();
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
                .listener(() -> finalizeAnimation(params));
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
        setOnTouchListener((v, event) -> true);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void unblockTouch() {
        this.getBackground().setColorFilter(null);
        this.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    startTalking();
                    return true;
                }

                case MotionEvent.ACTION_UP: {
                    stopTalking();
                    break;
                }
            }
            return false;
        });
    }

    /**
     * Request token for talking
     * @return true if granted otherwise false.
     */
    private boolean requestToken(){
        HttpClient httpClient = new DefaultHttpClient();
        String url = "http://" + Conf.SERVER_IP + ":" + Conf.TOKEN_PORT + "/gettoken?imei=" + deviceToken + "&groupId=" + MessengerHelper.getPttGroupList().get(groupIndex).idGroup + "&API_KEY="+ API_KEY +"&clientName=" + name + "&username=" + username;

        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<>();

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity respEntity = response.getEntity();

            if (respEntity != null) {
                String content =  EntityUtils.toString(respEntity);
                Log.e(TAG, content);
                return TOKEN_TAKEN.equals(content);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * release token
     */
    private void leaveToken(){
        if(Utils.isConnect(context)) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                String url = "http://" + Conf.SERVER_IP + ":" + Conf.TOKEN_PORT + "/releasetoken?imei=" + deviceToken + "&groupId=" + MessengerHelper.getPttGroupList().get(groupIndex).idGroup + "&API_KEY=" + API_KEY + "&clientName=" + name + "&username=" + username;

                HttpPost httpPost = new HttpPost(url);
                List<NameValuePair> params = new ArrayList<>();

                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                HttpResponse response = httpClient.execute(httpPost);
                HttpEntity respEntity = response.getEntity();

                if (respEntity != null) {
                    if (TOKEN_RELEASED.equals(EntityUtils.toString(respEntity))) {
                        PreferenceManager.getDefaultSharedPreferences(context)
                                .edit().putBoolean(TOKEN_RELEASED_ERROR, false).apply();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                PreferenceManager.getDefaultSharedPreferences(context)
                        .edit().putBoolean(TOKEN_RELEASED_ERROR, true).apply();
            }
        }else{
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit().putBoolean(TOKEN_RELEASED_ERROR, true).apply();
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initView() {

        if(!Utils.isServiceRunning(WebSocketPTTService.class,context)){
            Intent i = new Intent(context, WebSocketPTTService.class);
            i.putExtra("username",username);
            i.putExtra("name",name);
            i.putExtra("idGroup",MessengerHelper.getPttGroupList().get(groupIndex).idGroup);
            i.putExtra("imei",Siggy.getDeviceToken());
            i.putExtra("apiKey",API_KEY);

            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                context.startService(i);
            else
                context.startForegroundService(i);

        }else{
            Utils.traces("PTTButton WebSocketPTTService already exists");
        }

        int intSize = android.media.AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                audioFormat);

        at = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                audioFormat, intSize, AudioTrack.MODE_STREAM);

       this.setOnTouchListener((v, event) -> {
           switch (event.getAction()) {
               case MotionEvent.ACTION_DOWN: {
                   startTalking();
                   return true;
               }
               case MotionEvent.ACTION_UP: {
                   stopTalking();
                   break;
               }
           }
           return false;
       });

        mPadding = new Padding();
        mPadding.left = getPaddingLeft();
        mPadding.right = getPaddingRight();
        mPadding.top = getPaddingTop();
        mPadding.bottom = getPaddingBottom();

        setBackgroundCompat(ResourcesCompat.getDrawable(getResources(),R.drawable.ptt_selector,null));
        setWidth(100);
        setHeight(100);

    }


   public void PlayShortAudioFileViaAudioTrack(byte[] byteData) throws IOException {
        if (at!=null) {
            at.write(byteData, 0, byteData.length);
            at.play();
        }
        else Log.d("TCAudio", "audio track is not initialised ");
    }

    private StrokeGradientDrawable createDrawable(int color, int cornerRadius, int strokeWidth) {
        StrokeGradientDrawable drawable = new StrokeGradientDrawable(new GradientDrawable());
        drawable.getGradientDrawable().setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        drawable.setCornerRadius(cornerRadius);
        drawable.setStrokeColor(color);
        drawable.setStrokeWidth(strokeWidth);
        return drawable;
    }

    @SuppressWarnings("deprecation")
    public void setBackgroundCompat(@Nullable Drawable drawable) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
            setBackgroundDrawable(drawable);
        } else {
            setBackground(drawable);
        }
    }

    public void setIcon(@DrawableRes final int icon) {
        // post is necessary, to make sure getWidth() doesn't return 0
        post(() -> {
            Drawable drawable = ResourcesCompat.getDrawable(getResources(),icon,null);
            int padding = (getWidth() / 2) - (drawable.getIntrinsicWidth() / 2);
            setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
            setPadding(padding, 0, 0, 0);
        });
    }

    public void setIconLeft(@DrawableRes int icon) {
        setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
    }

    /**
     * Method that change the text of the button when is sending data
     * @param sendingText sending text
     */
    public void setSendingText(String sendingText) {
        this.sendingText = sendingText;
    }
    @Override
    public void onSpeechDetected() {
        System.out.println("Speech detected");
        if(!isTalking){
            isTalking = true;
            ((Activity)context).runOnUiThread(this::startTalking);
        }
    }

    @Override
    public void onNoiseDetected() {
        if(isTalking) {
            isTalking = false;
            ((Activity) context).runOnUiThread(this::stopTalking);
        }
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

    public final class AudioQuality {
        public static final int HIGH = 1;
        public static final int MEDIUM = 2;
        public static final int LOW = 3;

    }

    public interface CallBack{
        void onPreExecute();
        void onReady(TaskMessage result);
    }

}

