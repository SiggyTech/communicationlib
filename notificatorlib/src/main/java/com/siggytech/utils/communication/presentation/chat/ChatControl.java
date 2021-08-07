package com.siggytech.utils.communication.presentation.chat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.siggytech.utils.communication.presentation.common.CallBack;
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
import java.util.Objects;

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
    public String userName;

    private Gson gson;
    private CallBack callBack;

    public ChatControl(Context context, String API_KEY, String userName, Lifecycle lifecycle, CallBack callBack, HeaderListener headerListener){
        super(context);

        Conf.API_KEY = API_KEY;

        FileUtil.createFolder(Conf.ROOT_FOLDER,"");

        Conf.DEVICE_TOKEN = Siggy.getDeviceToken();

        this.userName = userName;
        this.gson = new Gson();
        this.callBack = callBack;

        MessengerHelper.setChatObserver(new ChatObserver(this));
        lifecycle.addObserver(MessengerHelper.getChatObserver());

        init(headerListener);
    }

    private void init(HeaderListener headerListener) {
        List<GroupModel> groupList = new ArrayList<>();
        groupList.add(new GroupModel(9999, "Every Group"));
        MessengerHelper.setGroupList(groupList);

        initLayout(headerListener);
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
                        getGroups();
                    }).start();

                });
    }


    private void getGroups(){
        new ApiAsyncTask(this).execute(ApiEnum.GET_GROUPS,Conf.DEVICE_TOKEN, Conf.API_KEY);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void initLayout(HeaderListener headerListener){
        inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        mBinding = DataBindingUtil.inflate(inflater,R.layout.chat_root,null,false);

        MessengerHelper.setChatListView(new ChatListView(
                getContext(),
                MessengerHelper.getGroupList().get(0).idGroup,
                headerListener));

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

                                if (audioFile.length() > 0)
                                    MessengerHelper.getChatListView().sendMessage(
                                            userName,
                                            AESUtils.encText(gson.toJson(messageModel)),
                                            AESUtils.encText(getContext().getString(R.string.audio_message)),
                                            Utils.MESSAGE_TYPE.AUDIO,
                                            MessengerHelper.getGroupList().get(MessengerHelper.getIndexGroup()).idGroup);

                            }
                            ar.setDuration(0);
                            timeSwapBuff = 0L;

                            boolean deleted = deleteFile(audioFile);
                            if (!deleted) Utils.traces(TAG+" CAN'T DELETE AUDIO FILE!");

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
                getContext().startActivity(new Intent(getContext(), AttachMenuActivity.class));

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
        Utils.traces(TAG+" clearPersistentVariables");
        isPickingFile  = true; //to start timer looking for a file to send.
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = settings.edit();
        settings.edit().remove("pickFile").commit();
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
            e.printStackTrace();
        }
        return deleted;
    }

    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if(isPickingFile) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
                boolean pickFile = settings.getBoolean("pickFile", true);
                boolean deleteFile = true;
                if(!pickFile){
                    try {
                        String fileType = settings.getString("fileType", Utils.MESSAGE_TYPE.PHOTO);

                        File file = new File(MessengerHelper.getLastUri().getPath());
                        if(!file.exists())
                            file = new File(FilePath.getPath(getContext(), MessengerHelper.getLastUri()));

                        if(file.exists()) {
                            MessageModel messageModel = new MessageModel();
                            if (Utils.MESSAGE_TYPE.PHOTO.equals(fileType)) {
                                messageModel.setMessage(fileToBase64(compressImage(getContext(),MessengerHelper.getLastUri())));
                            }
                            messageModel.setType(fileType);
                            messageModel.setFrom(userName);

                            if (Utils.MESSAGE_TYPE.VIDEO.equals(fileType)) {
                                String fileName = getDateName() + "." + getFileExt(file.getName());
                                compressVideo(file.getAbsolutePath(),fileName,messageModel);
                                deleteFile = false;
                            } else {
                                MessengerHelper.getChatListView().sendMessage(
                                        userName,
                                        AESUtils.encText(gson.toJson(messageModel)),
                                        AESUtils.encText(getContext().getString(R.string.image_message)),
                                        fileType,
                                        MessengerHelper.getGroupList().get(MessengerHelper.getIndexGroup()).idGroup);
                            }

                            isPickingFile = false;
                            if (deleteFile && MessengerHelper.getLastUri().getPath().contains(Conf.ROOT_FOLDER)) {
                                boolean deleted = deleteFile(file);
                                if (!deleted) Utils.traces(TAG+" CAN'T DELETE FILE!");
                            }
                        }else{
                            isPickingFile = false;
                            Toast.makeText(getContext(),"File doesn't exist: "+MessengerHelper.getLastUri().getPath(),Toast.LENGTH_LONG).show();
                        }
                    } catch(Exception e) {
                        Utils.traces("Pick file ex: "+Utils.exceptionToString(e));
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
            mBinding.tvAudioText.setText(String.format(Locale.US,"%02d:%02d", mins, secs));
            customHandler.postDelayed(this, 0);
        }

    };


    private void compressVideo(String filePath, String fileName, MessageModel messageModel){
        VideoCompress.compressVideoLow(filePath, filePath, new VideoCompress.CompressListener() {
            private ProgressDialog progressDialog;

            @Override
            public void onStart() {
                try {
                    progressDialog = new ProgressDialog(getContext());
                    progressDialog.setMessage(getContext().getString(R.string.wait));
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                }catch (Exception e){
                    Utils.traces("compressVideo onStart "+Utils.exceptionToString(e));
                }
            }

            @Override
            public void onSuccess() {
                File file = FileUtil.getFile(Conf.ROOT_FOLDER,fileName);
                messageModel.setFile(file);
                MessengerHelper.getChatListView().callToBase64(messageModel);
                progressDialog.dismiss();
            }

            @Override
            public void onFail() {
                if(progressDialog!=null && progressDialog.isShowing())
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
        if(MessengerHelper.getChatListView()!=null)
            MessengerHelper.getChatListView().deleteHistory();
    }

    public void onDestroy(){
        try{
            if(MessengerHelper.getChatListView()!=null)
                MessengerHelper.getChatListView().onDestroy();
            clearInstances();
        }catch (Exception e){
            Utils.traces("onDestroy ChatControl ex: "+Utils.exceptionToString(e));
        }
    }

    public void onResume(){
        if(MessengerHelper.getChatListView()!=null)
            MessengerHelper.getChatListView().onResume();
    }

    public void onStop(){
        if(MessengerHelper.getChatListView()!=null)
            MessengerHelper.getChatListView().onStop();
    }

    public void setHeaderListener(HeaderListener listener){
        if(MessengerHelper.getChatListView()!=null)
            MessengerHelper.getChatListView().setHeaderListener(listener);
    }

    /**
     * Use this method to create a new instance of chat observer, do not forget null this
     * on destroy event by your self. Is normal used when you have more than one activity
     * @param destroy if destroy instances of chat control.
     * @return chat observer
     */
    public ChatObserver getNewChatObserver(boolean destroy){
        return new ChatObserver(this, destroy);
    }

    /**
     * This method returns the main chat Observer
     * @return Main Chat Observer
     */
    public ChatObserver getMainChatObserver(){
        return MessengerHelper.getChatObserver();
    }

    private void clearInstances() {
        ar = null;
        timerHandler = null;
        fileName = null;
        userName = null;
        gson = null;
        callBack = null;
        inflater = null;
        MessengerHelper.clearChatObserver();
    }

    public void onNewIntent(Bundle extras) {
        if(extras != null && extras.containsKey(MESSAGE_GROUP))
            setGroupView(Long.parseLong(Objects.requireNonNull(extras.getString(MESSAGE_GROUP))), 10);
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



    public static class ChatObserver implements LifecycleObserver{

        private final ChatControl chatControl;
        private final Boolean destroy;

        public ChatObserver(ChatControl chatControl) {
            this.chatControl = chatControl;
            this.destroy = true;
        }

        public ChatObserver(ChatControl chatControl, boolean destroy) {
            this.chatControl = chatControl;
            this.destroy = destroy;
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        public void onResume(){
            if(MessengerHelper.getChatListView()!=null)
                MessengerHelper.getChatListView().setLifecycleEvent(Lifecycle.Event.ON_RESUME);
            Utils.traces("chat onResume de Lifecycle ["+destroy+"]");
            chatControl.onResume();

        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        public void onPause(){
            if(MessengerHelper.getChatListView()!=null)
                MessengerHelper.getChatListView().setLifecycleEvent(Lifecycle.Event.ON_PAUSE);
            Utils.traces("chat onPause de Lifecycle ["+destroy+"]");
        }


        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        public void onStop(){
            if(MessengerHelper.getChatListView()!=null)
                MessengerHelper.getChatListView().setLifecycleEvent(Lifecycle.Event.ON_STOP);
            Utils.traces("chat onStop de Lifecycle ["+destroy+"]");
            chatControl.onStop();
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void onDestroy(){
            Utils.traces("chat onDestroy de Lifecycle ["+destroy+"]");
            if(destroy) {
                if (MessengerHelper.getChatListView() != null)
                    MessengerHelper.getChatListView().setLifecycleEvent(Lifecycle.Event.ON_DESTROY);
                chatControl.onDestroy();
            }
        }

    }
}

