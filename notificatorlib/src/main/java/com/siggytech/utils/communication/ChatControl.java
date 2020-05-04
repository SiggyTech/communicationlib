package com.siggytech.utils.communication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.siggytech.utils.communication.audio.AudioRecorder;

import java.io.File;

import static android.content.Context.TELEPHONY_SERVICE;
import static com.siggytech.utils.communication.Utils.FileToBase64;
import static com.siggytech.utils.communication.Utils.GetDateName;
import static com.siggytech.utils.communication.Utils.GetStringDate;

/**
 * @author SIGGI Tech
 */
public class ChatControl extends RelativeLayout {
    private static final String TAG = ChatControl.class.getSimpleName();
    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_WRITE = 2;
    public static final int SELECT_FILE = 100;
    private ListView mConversationView;
    private EditText mOutEditText;
    private EditText mServerAddress;
    private LinearLayout mSendButton;
    private LinearLayout mAddFile;
    private LinearLayout mAudio;
    private TextView mAudioText;
    private AudioRecorder ar;
    private boolean isPickingFile = false;
    Handler timerHandler = new Handler();

    private String filePath;
    public String imei;
    public String name;
    public String api_key;
    public String userName;
    public int idGroup;
    private final Context context;
    private ChatListView chatListView;

    private String packageName;
    private int resIcon;
    private Activity mActivity;
    private Gson gson;


    public ChatControl(Context context, int idGroup, String API_KEY, String nameClient, String userName,
                        String packageName, int resIcon, Activity activity){
        super(context);
        this.context = context;
        this.idGroup = idGroup;
        this.api_key = API_KEY;
        this.name = nameClient;
        this.imei = getIMEINumber();
        this.userName = userName;

        this.packageName = packageName;
        this.resIcon = resIcon;
        this.mActivity = activity;
        this.gson = new Gson();
        initLayout(context);
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

    @SuppressLint("ClickableViewAccessibility")
    public void initLayout(final Context context) {
        int idContent = Utils.GenerateViewId();

        ViewGroup.LayoutParams root_LayoutParams =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        root_LayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        root_LayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        this.setLayoutParams(root_LayoutParams);

        RelativeLayout rl = new RelativeLayout(context);

        chatListView = new ChatListView(
                context,
                mActivity,
                idGroup,
                api_key,
                name,
                packageName,
                resIcon);

        chatListView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        RelativeLayout.LayoutParams abc_LayoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        abc_LayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        abc_LayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        abc_LayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        abc_LayoutParams.addRule(ABOVE,idContent);
        rl.addView(chatListView);
        this.addView(rl);
        rl.setLayoutParams(abc_LayoutParams);
        rl.setId(Utils.GenerateViewId());

        mOutEditText = new EditText(context);
        mOutEditText.setId(Utils.GenerateViewId());
        mOutEditText.setMaxLines(5);
        mOutEditText.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        mOutEditText.setBackgroundResource(R.drawable.gradientbg);

        mSendButton = new LinearLayout(context);
        mSendButton.setId(Utils.GenerateViewId());
        mSendButton.setGravity(Gravity.CENTER);
        mSendButton.setPadding(10,10,10,15);
        if(!Conf.CHAT_BASIC) mSendButton.setVisibility(GONE);

        ImageView iv = new ImageView(context);
        iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_send_24dp));
        iv.setColorFilter(Conf.CHAT_COLOR_COMPONENTS);
        mSendButton.addView(iv);

        mAudio = new LinearLayout(context);
        mAudio.setPadding(10,10,10,15);
        mAudio.setId(Utils.GenerateViewId());
        mAudio.setGravity(Gravity.CENTER);

        ImageView ivMic = new ImageView(context);
        ivMic.setImageDrawable(getResources().getDrawable(R.drawable.ic_mic_none_24dp));
        ivMic.setColorFilter(Conf.CHAT_COLOR_COMPONENTS);
        mAudio.addView(ivMic);

        mAudioText = new TextView(context);
        mAudioText.setText("00:00");
        mAudioText.setTextColor(getResources().getColor(R.color.bt_dark_gray));
        mAudioText.setTextSize(TypedValue.COMPLEX_UNIT_SP,17);
        mAudioText.setPadding(20,0,0,0);
        mAudioText.setVisibility(GONE);

        mAddFile = new LinearLayout(context);
        mAddFile.setPadding(20,10,10,15);
        mAddFile.setId(Utils.GenerateViewId());
        mAddFile.setGravity(Gravity.CENTER);

        final ImageView ivAdd = new ImageView(context);
        ivAdd.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_24dp));
        ivAdd.setColorFilter(Conf.CHAT_COLOR_COMPONENTS);
        mAddFile.addView(ivAdd);

        final ImageView ivMic2 = new ImageView(context);
        ivMic2.setImageDrawable(getResources().getDrawable(R.drawable.ic_mic_none_gray_24dp));
        ivMic2.setVisibility(GONE);
        mAddFile.addView(ivMic2);

        RelativeLayout.LayoutParams mContentParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        mContentParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        LinearLayout lnContent = new LinearLayout(context);
        lnContent.setId(idContent);
        lnContent.setLayoutParams(mContentParams);
        lnContent.setBackgroundColor(getResources().getColor(R.color.light_grey));
        lnContent.setPadding(10,10,10,20);

        LinearLayout lnSend = getLnContentSum(6);
        lnSend.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout lnH0 = getLnWeight(1);
        lnH0.setVerticalGravity(Gravity.CENTER|Gravity.BOTTOM);
        LinearLayout lnH1 = getLnWeight(Conf.CHAT_BASIC?5:4);
        lnH1.setVerticalGravity(Gravity.CENTER);
        LinearLayout lnH2 = getLnWeight(1);
        lnH2.setGravity(Gravity.CENTER|Gravity.BOTTOM);

        lnH0.addView(mAddFile);

        if(!Conf.SEND_FILES){
            lnH0.setVisibility(View.GONE);
        }

        lnH1.addView(mOutEditText);
        lnH1.addView(mAudioText);
        lnH2.addView(mSendButton);
        if(!Conf.CHAT_BASIC) {
            lnH2.addView(mAudio);
            lnSend.addView(lnH0);
        }
        lnSend.addView(lnH1);
        lnSend.addView(lnH2);
        lnContent.addView(lnSend);

        this.addView(lnContent);

        if(!Conf.CHAT_BASIC) {
            TextWatcher excludeTW;
            excludeTW = new TextWatcher(){
                @Override
                public void afterTextChanged(Editable s) {}
                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before,
                                          int count) {
                    if (count>0) {
                        mAudio.setVisibility(GONE);
                        mSendButton.setVisibility(VISIBLE);
                    } else {
                        mAudio.setVisibility(VISIBLE);
                        mSendButton.setVisibility(GONE);
                    }
                }
            };
            mOutEditText.addTextChangedListener(excludeTW);

            mAudio.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        ChatControl.this.setFocusable(true);
                        ChatControl.this.requestFocus();

                        ivAdd.setVisibility(GONE);
                        ivMic2.setVisibility(VISIBLE);
                        mOutEditText.setVisibility(GONE);
                        mAudioText.setVisibility(VISIBLE);

                        filePath = Conf.ROOT_PATH + GetDateName() + ".3gp";
                        ar = new AudioRecorder(filePath);

                        audioRecording(true);

                        return true;
                    }
                    case MotionEvent.ACTION_UP: {
                        try {
                            audioRecording(false);
                            mAudioText.setText("00:00");

                            File audioFile = new File(filePath);
                            if(ar.getDuration() > 1) {
                                MessageModel messageModel = new MessageModel();
                                messageModel.setType(Utils.MESSAGE_TYPE.AUDIO);
                                messageModel.setMessage(FileToBase64(audioFile));
                                messageModel.setDuration(ar.getDuration());

                                if (audioFile != null && audioFile.length() > 0)
                                    chatListView.sendMessage(userName, AESUtils.encText(gson.toJson(messageModel)), GetStringDate(), Utils.MESSAGE_TYPE.AUDIO);

                            }
                            ar.setDuration(0);
                            timeSwapBuff = 0L;

                            boolean deleted = deleteFile(audioFile);
                            if (!deleted) Log.d(TAG, "CAN'T DELETE FILE!!!");


                            ivAdd.setVisibility(VISIBLE);
                            ivMic2.setVisibility(GONE);
                            mOutEditText.setVisibility(VISIBLE);
                            mAudioText.setVisibility(GONE);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        return true;
                    }
                }

                return false;
            });
        }

        mSendButton.setOnClickListener(view -> {
            try {
                if(!"".equals(mOutEditText.getText().toString().trim())) {

                    MessageModel messageModel = new MessageModel();
                    messageModel.setType(Utils.MESSAGE_TYPE.MESSAGE);
                    messageModel.setMessage(mOutEditText.getText().toString());
                    chatListView.sendMessage(userName, AESUtils.encText(gson.toJson(messageModel)), GetStringDate(), Utils.MESSAGE_TYPE.MESSAGE);
                    mOutEditText.setText("");
                }
            } catch(Exception e){e.printStackTrace();}
        });

        mAddFile.setOnClickListener(view -> {
            try {
                mActivity.startActivity(new Intent(context, UtilActivity.class));

                clearPersistentVariables();

                timerHandler.postDelayed(timerRunnable,0);
            } catch(Exception e){
                e.printStackTrace();
            }
        });
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

    private LinearLayout getLnContentSum(float weight){
        LinearLayout lnContent2 = new LinearLayout(context);
        lnContent2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        lnContent2.setOrientation(LinearLayout.HORIZONTAL);
        lnContent2.setWeightSum(weight);
        return  lnContent2;
    }

    private LinearLayout getLnWeight(float weight){
        LinearLayout ln = new LinearLayout(context);
        ln.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,weight));

        return ln;
    }

    public EditText getmOutEditText() {
        return mOutEditText;
    }

    public LinearLayout getmSendButton() {
        return mSendButton;
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
                        String pathToFile = settings.getString("pathToFile", "");
                        File file = new File(pathToFile);
                        if(!file.exists()) file = new File(Utils.getRealPathFromURI(context, Uri.parse(pathToFile)));

                        if(file.exists()) {
                            MessageModel messageModel = new MessageModel();
                            if (Utils.MESSAGE_TYPE.PHOTO.equals(fileType)) {
                                messageModel.setMessage(FileToBase64(Utils.CompressImage(pathToFile)));
                            }
                            messageModel.setType(fileType);
                            messageModel.setFrom(userName);
                            messageModel.setDate(GetStringDate());

                            if (Utils.MESSAGE_TYPE.VIDEO.equals(fileType)) {
                                deleteFile = false;
                                messageModel.setFile(file);
                                chatListView.callToBase64(messageModel);
                            } else {
                                chatListView.sendMessage(userName, AESUtils.encText(gson.toJson(messageModel)), GetStringDate(), fileType);
                            }

                            isPickingFile = false;
                            if (deleteFile && pathToFile.contains("SIGGI")) {
                                boolean deleted = deleteFile(file);
                                if (!deleted) Log.d(TAG, "CAN'T DELETE FILE!");
                            }
                        }else{
                            isPickingFile = false;
                            Toast.makeText(context,"CAN'T FIND FILE: "+pathToFile,Toast.LENGTH_LONG).show();
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                else
                    timerHandler.postDelayed(timerRunnable,1000);
            }

        }
    };

    private long startHTime = 0L;
    private Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startHTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;

            ar.setDuration((int)(updatedTime / 1000));
            if (mAudioText != null)
                mAudioText.setText(String.format("%02d:%02d", mins, secs));
            customHandler.postDelayed(this, 0);
        }

    };

}

