package com.siggytech.utils.communication.presentation.chat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.siggytech.utils.communication.R;
import com.siggytech.utils.communication.databinding.ChatRootBinding;
import com.siggytech.utils.communication.model.GroupModel;
import com.siggytech.utils.communication.model.MessageModel;
import com.siggytech.utils.communication.model.PairRegisterModel;
import com.siggytech.utils.communication.model.async.ApiAsyncTask;
import com.siggytech.utils.communication.model.async.ApiEnum;
import com.siggytech.utils.communication.model.async.ApiListener;
import com.siggytech.utils.communication.model.async.ApiManager;
import com.siggytech.utils.communication.model.async.TaskMessage;
import com.siggytech.utils.communication.presentation.AttachMenuActivity;
import com.siggytech.utils.communication.presentation.MessengerHelper;
import com.siggytech.utils.communication.presentation.register.Siggy;
import com.siggytech.utils.communication.util.AESUtils;
import com.siggytech.utils.communication.util.Conf;
import com.siggytech.utils.communication.util.FilePath;
import com.siggytech.utils.communication.util.FileUtil;
import com.siggytech.utils.communication.util.Utils;
import com.siggytech.utils.communication.util.audio.AudioRecorder;
import com.siggytech.utils.communication.util.videocompress.VideoCompress;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.siggytech.utils.communication.presentation.service.MyFirebaseMessagingService.MESSAGE_GROUP;
import static com.siggytech.utils.communication.util.DateUtil.getDateName;
import static com.siggytech.utils.communication.util.FileUtil.fileToBase64;
import static com.siggytech.utils.communication.util.FileUtil.getFileExt;
import static com.siggytech.utils.communication.util.ImageUtil.compressImage;

/**
 * @author SIGGI Tech
 */
@SuppressLint("ViewConstructor")
public class ChatControl extends FrameLayout implements ApiListener<TaskMessage> {
    private static final String TAG = ChatControl.class.getSimpleName();
    public static final int SELECT_FILE = 100;

    private ChatRootBinding mBinding;
    private LayoutInflater inflater;
    private AudioRecorder ar;
    private boolean isPickingFile = false;
    Handler timerHandler = new Handler();

    private String fileName;
    public String deviceToken;
    public String api_key;
    public String userName;

    private final Context context;
    private Gson gson;

    private CallBack callBack;



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public ChatControl(Context context, String API_KEY, String userName, Lifecycle lifecycle, CallBack callBack){
        super(context);

        FileUtil.createFolder(Conf.ROOT_FOLDER,"");

        this.api_key = API_KEY;
        this.deviceToken = Siggy.getDeviceToken();
        this.userName = userName;
        this.context = context;
        this.gson = new Gson();
        this.callBack = callBack;

        lifecycle.addObserver(new ChatObserver(this));

        init();
    }

    private void init() {
        List<GroupModel> groupList = new ArrayList<>();
        groupList.add(new GroupModel(9999, "Every Group"));
        MessengerHelper.setGroupList(groupList);

        initLayout(context);
        getGroups();
        setTokenPair();
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
                        TaskMessage message = apiManager.setFirebaseToken(new PairRegisterModel(Siggy.getDeviceToken(),task.getResult()));
                        Utils.traces("Firebase Token: "+task.getResult());
                        Log.e(TAG,"On Pair register: "+message.getMessage());
                    }).start();

                });
    }


    private void getGroups(){
        new ApiAsyncTask(this).execute(ApiEnum.GET_GROUPS,deviceToken,api_key);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void initLayout(final Context context){
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        mBinding = DataBindingUtil.inflate(inflater,R.layout.chat_root,null,false);

        //TODO change header to chatListView

        MessengerHelper.setChatListView(new ChatListView(
                context,
                MessengerHelper.getGroupList().get(MessengerHelper.getIndexGroup()).idGroup,
                api_key,
                deviceToken));

        mBinding.frame.addView(MessengerHelper.getChatListView());

        if(Conf.CHAT_DARK_MODE){
            mBinding.etOutBox.setBackgroundResource(R.drawable.box_dark_bg);
            mBinding.etOutBox.setTextColor(getResources().getColor(R.color.textColorDark));
        }else
            mBinding.etOutBox.setBackgroundResource(R.drawable.gradientbg);

        mBinding.ivSend.setColorFilter(Conf.CHAT_COLOR_COMPONENTS);
        if(!Conf.CHAT_BASIC) mBinding.ivSend.setVisibility(GONE);

        mBinding.ivMic.setColorFilter(Conf.CHAT_COLOR_COMPONENTS);
        mBinding.ivAdd.setColorFilter(Conf.CHAT_COLOR_COMPONENTS);

        if(Conf.CHAT_DARK_MODE){
            mBinding.clControls.setBackgroundColor(getResources().getColor(R.color.primaryColorDark));
        }else {
            mBinding.clControls.setBackgroundColor(getResources().getColor(R.color.light_grey));
        }

        if(!Conf.CHAT_BASIC) {
            TextWatcher excludeTW;
            excludeTW = new TextWatcher(){
                @Override
                public void afterTextChanged(Editable s) {
                    if(mBinding.etOutBox.getText().toString().length()>0){
                        mBinding.ivMic.setVisibility(GONE);
                        mBinding.ivSend.setVisibility(VISIBLE);
                    }
                }
                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before,
                                          int count) {
                    if (count>0) {
                        mBinding.ivMic.setVisibility(GONE);
                        mBinding.ivSend.setVisibility(VISIBLE);
                    } else {
                        mBinding.ivMic.setVisibility(VISIBLE);
                        mBinding.ivSend.setVisibility(GONE);
                    }
                }
            };
            mBinding.etOutBox.addTextChangedListener(excludeTW);

            mBinding.ivMic.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        ChatControl.this.setFocusable(true);
                        ChatControl.this.requestFocus();

                        mBinding.ivAdd.setVisibility(GONE);
                        mBinding.etOutBox.setVisibility(GONE);
                        mBinding.tvAudioText.setVisibility(VISIBLE);

                        fileName = getDateName();
                        ar = new AudioRecorder(fileName);

                        audioRecording(true);

                        return true;
                    }
                    case MotionEvent.ACTION_UP: {
                        try {
                            audioRecording(false);
                            mBinding.tvAudioText.setText(R.string.time_zero);

                            File audioFile = FileUtil.getFile(Conf.ROOT_FOLDER,fileName+".3gp");
                            if(ar.getDuration() > 1) {
                                MessageModel messageModel = new MessageModel();
                                messageModel.setType(Utils.MESSAGE_TYPE.AUDIO);
                                messageModel.setMessage(fileToBase64(audioFile));
                                messageModel.setDuration(ar.getDuration());

                                if (audioFile != null && audioFile.length() > 0)
                                    MessengerHelper.getChatListView().sendMessage(
                                            userName,
                                            AESUtils.encText(gson.toJson(messageModel)),
                                            AESUtils.encText(context.getString(R.string.audio_message)),
                                            Utils.MESSAGE_TYPE.AUDIO,
                                            MessengerHelper.getGroupList().get(MessengerHelper.getIndexGroup()).idGroup);

                            }
                            ar.setDuration(0);
                            timeSwapBuff = 0L;

                            boolean deleted = deleteFile(audioFile);
                            if (!deleted) Log.d(TAG, "CAN'T DELETE FILE!");


                            mBinding.ivAdd.setVisibility(VISIBLE);
                            mBinding.etOutBox.setVisibility(VISIBLE);
                            mBinding.tvAudioText.setVisibility(GONE);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        return true;
                    }
                }
                return false;
            });
        }

        mBinding.ivSend.setOnClickListener(view -> {
            try {
                if(!"".equals(mBinding.etOutBox.getText().toString().trim())) {
                    MessageModel messageModel = new MessageModel();
                    messageModel.setType(Utils.MESSAGE_TYPE.MESSAGE);
                    messageModel.setMessage(mBinding.etOutBox.getText().toString());
                    MessengerHelper.getChatListView().sendMessage(
                            userName,
                            AESUtils.encText(gson.toJson(messageModel)),
                            AESUtils.encText(getMsgPart(mBinding.etOutBox.getText().toString())),
                            Utils.MESSAGE_TYPE.MESSAGE,
                            MessengerHelper.getGroupList().get(MessengerHelper.getIndexGroup()).idGroup);
                    mBinding.etOutBox.setText("");
                }
            } catch(Exception e){e.printStackTrace();}
        });

        mBinding.ivAdd.setOnClickListener(view -> {
            try {
                context.startActivity(new Intent(context, AttachMenuActivity.class));

                clearPersistentVariables();

                timerHandler.postDelayed(timerRunnable,0);
            } catch(Exception e){
                e.printStackTrace();
            }
        });

        this.addView(mBinding.getRoot());
    }

    private String getMsgPart(String text) {
        if(text.length()>30){
            return text.substring(30)+"...";
        }else return text;
    }


    public List<GroupModel> getGroupList(){
        return MessengerHelper.getGroupList();
    }

    private void clearPersistentVariables(){
        isPickingFile  = true; //to start timer looking for a file to send.
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        settings.edit().remove("pickFile").commit();
        settings.edit().remove("pathToFile").commit();
        settings.edit().remove("fileType").commit();
        editor.commit();
    }

    /**
     * Metodo que inicia o para una grabacion.
     * @param start si true signafica que inicia grabacion, de lo contrario la detiene
     */
    private void audioRecording(boolean start){
        if(start){
            try {
                ar.start();
                startHTime = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread, 0);
            } catch (Exception e) {
                Log.e("Exception in start", "" + e);
            }
        }else{
            try {
                ar.stop();
                timeSwapBuff += timeInMilliseconds;
                customHandler.removeCallbacks(updateTimerThread);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Deletes file
     *
     * @param file file
     * @return true if was deleted otherwise false
     */
    private boolean deleteFile(File file){
        boolean deleted = false;
        try{
            if(file!=null){
                deleted = file.delete();
            }
        }catch (Exception e){
            deleted = false;
            e.printStackTrace();
        }
        return deleted;
    }

    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if(isPickingFile) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                boolean pickFile = settings.getBoolean("pickFile", true);
                boolean deleteFile = true;
                if(!pickFile){
                    try {
                        String fileType = settings.getString("fileType", Utils.MESSAGE_TYPE.PHOTO);

                        File file = new File(MessengerHelper.getLastUri().getPath());
                        if(!file.exists())
                            file = new File(FilePath.getPath(context, MessengerHelper.getLastUri()));

                        if(file.exists()) {
                            MessageModel messageModel = new MessageModel();
                            if (Utils.MESSAGE_TYPE.PHOTO.equals(fileType)) {
                                messageModel.setMessage(fileToBase64(compressImage(context,MessengerHelper.getLastUri())));
                            }
                            messageModel.setType(fileType);
                            messageModel.setFrom(userName);

                            if (Utils.MESSAGE_TYPE.VIDEO.equals(fileType)) {
                                String destPath = Conf.ROOT_FOLDER + getDateName() + getFileExt(file.getName());
                                compressVideo(file.getAbsolutePath(),destPath,messageModel);
                                deleteFile = false;
                            } else {
                                MessengerHelper.getChatListView().sendMessage(
                                        userName,
                                        AESUtils.encText(gson.toJson(messageModel)),
                                        AESUtils.encText(context.getString(R.string.image_message)),
                                        fileType,
                                        MessengerHelper.getGroupList().get(MessengerHelper.getIndexGroup()).idGroup);
                            }

                            isPickingFile = false;
                            if (deleteFile && MessengerHelper.getLastUri().getPath().contains("SIGGI")) {
                                boolean deleted = deleteFile(file);
                                if (!deleted) Log.d(TAG, "CAN'T DELETE FILE!");
                            }
                        }else{
                            isPickingFile = false;
                            Toast.makeText(context,"CAN'T FIND FILE: "+MessengerHelper.getLastUri().getPath(),Toast.LENGTH_LONG).show();
                        }
                    } catch(Exception e) {
                        Utils.traces("Pick file ex: "+(e!=null?e.getMessage():"null"));
                    }
                }
                else
                    timerHandler.postDelayed(timerRunnable,1000);
            }

        }
    };

    private long startHTime = 0L;
    private final Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    private final Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startHTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;

            ar.setDuration((int)(updatedTime / 1000));
            if (mBinding.tvAudioText != null)
                mBinding.tvAudioText.setText(String.format(Locale.US,"%02d:%02d", mins, secs));
            customHandler.postDelayed(this, 0);
        }

    };


    private void compressVideo(String filePath, String destPath, MessageModel messageModel){
        VideoCompress.compressVideoLow(filePath, destPath, new VideoCompress.CompressListener() {
            private ProgressDialog progressDialog;

            @Override
            public void onStart() {
                progressDialog=new ProgressDialog(context);
                progressDialog.setMessage(getContext().getString(R.string.wait));
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
            }

            @Override
            public void onSuccess() {
                File file = new File(destPath);
                messageModel.setFile(file);
                MessengerHelper.getChatListView().callToBase64(messageModel);
                progressDialog.dismiss();
            }

            @Override
            public void onFail() {
                progressDialog.dismiss();
            }

            @Override
            public void onProgress(float percent) {
                Log.d(ChatControl.class.getSimpleName(),"VIDEO COMPRESS: "+ percent);
            }
        });
    }

    /**
     * Set this for changing the group chat
     * @param idGroup id group
     * @param limit limit message rescue
     */
    public void setGroupView(long idGroup, int limit){
        if(MessengerHelper.getChatListView()!=null)
            MessengerHelper.getChatListView().setGroupView(idGroup,limit);
        else Utils.traces("Chat Control setGroupView MessengerHelper.getChatListView() is null");
    }

    public void deleteHistory(){
        MessengerHelper.getChatListView().deleteHistory();
    }

    /**
     * You need override onDestroy method and call it.
     */
    public void onDestroy(){
        try{
            MessengerHelper.getChatListView().onDestroy();
            clearInstances();
        }catch (Exception e){
            Utils.traces("onDestroy ChatControl ex: "+Utils.exceptionToString(e));
        }
    }

    public void onResume(){
        MessengerHelper.getChatListView().onResume();
    }

    private void clearInstances() {
        ar = null;
        timerHandler = null;
        fileName = null;
        deviceToken = null;
        api_key = null;
        userName = null;
        gson = null;
        callBack = null;
        inflater = null;
    }

    public void onNewIntent(Bundle extras) {
        if(extras != null && extras.containsKey(MESSAGE_GROUP))
            setGroupView(Long.parseLong(extras.getString(MESSAGE_GROUP)), 10);
    }



    @Override
    public void onPreExecute() { callBack.onPreExecute(); }

    @Override
    public void onPostExecute(TaskMessage result) {
        callBack.onReady(result);
    }

    @Override
    public void onCancelled(TaskMessage result) {
        callBack.onReady(result);
    }


    public interface CallBack{
        void onPreExecute();
        void onReady(TaskMessage result);
    }


    public static class ChatObserver implements LifecycleObserver{

        private final ChatControl chatControl;

        public ChatObserver(ChatControl chatControl) {
            this.chatControl = chatControl;
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        public void onResume(){
            if(MessengerHelper.getChatListView()!=null)
            MessengerHelper.getChatListView().setLifecycleEvent(Lifecycle.Event.ON_RESUME);
            Utils.traces("onResume de Lifecycle");
            chatControl.onResume();

        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        public void onPause(){
            if(MessengerHelper.getChatListView()!=null)
            MessengerHelper.getChatListView().setLifecycleEvent(Lifecycle.Event.ON_PAUSE);
            Utils.traces("onPause de Lifecycle");
        }


        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        public void onStop(){
            if(MessengerHelper.getChatListView()!=null)
            MessengerHelper.getChatListView().setLifecycleEvent(Lifecycle.Event.ON_STOP);
            Utils.traces("onStop de Lifecycle");
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void onDestroy(){
            if(MessengerHelper.getChatListView()!=null)
            MessengerHelper.getChatListView().setLifecycleEvent(Lifecycle.Event.ON_DESTROY);
            Utils.traces("onDestroy de Lifecycle");
            chatControl.onDestroy();
        }

    }
}

