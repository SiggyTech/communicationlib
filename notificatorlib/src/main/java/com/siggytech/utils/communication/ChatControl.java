package com.siggytech.utils.communication;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
    private CountDownTimer t;
    private boolean isPickingFile = false;
    Handler timerHandler = new Handler();


    private ArrayAdapter<String> mConversationArrayAdapter;

    private int cnt;
    private String filePath;
    public String imei;
    public String name;
    public String api_key;
    public String userName;
    public int idGroup;
    private final Context context;
    private ChatListView abc;
    private String messageTittle;
    private String messageText;
    private String packageName;
    private int resIcon;
    private String notificationMessage;
    private Activity mActivity;
    private Gson gson;

    public ChatControl(Context context, int idGroup, String API_KEY, String nameClient, String userName,
                       String messageTittle, String messageText, String packageName, int resIcon, String notificationMessage, Activity activity){
        super(context);
        this.context = context;
        this.idGroup = idGroup;
        this.api_key = API_KEY;
        this.name = nameClient;
        this.imei = getIMEINumber();
        this.userName = userName;
        this.messageTittle = messageTittle;
        this.messageText = messageText;
        this.packageName = packageName;
        this.resIcon = resIcon;
        this.notificationMessage = notificationMessage;
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

        abc = new ChatListView(context, idGroup,
                api_key,
                name,
                messageTittle,
                messageText,
                packageName,
                resIcon);

        abc.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        abc.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        RelativeLayout.LayoutParams abc_LayoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        abc_LayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        abc_LayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        abc_LayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        abc_LayoutParams.addRule(ABOVE,idContent);
        rl.addView(abc);
        this.addView(rl);
        rl.setLayoutParams(abc_LayoutParams);
        rl.setId(Utils.GenerateViewId());

        mOutEditText = new EditText(context);
        mOutEditText.setId(Utils.GenerateViewId());
        mOutEditText.setMaxLines(5);
        mOutEditText.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        mOutEditText.setBackgroundResource(R.drawable.gradientbg);

        mSendButton = new LinearLayout(context);
        mSendButton.setLayoutParams(new LayoutParams(100,100));
        mSendButton.setId(Utils.GenerateViewId());
        mSendButton.setBackgroundResource(R.drawable.send_selector);
        mSendButton.setGravity(Gravity.CENTER);
        if(!Conf.CHAT_BASIC) mSendButton.setVisibility(GONE);

        ImageView iv = new ImageView(context);
        iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_send_24dp));
        mSendButton.addView(iv);

        mAudio = new LinearLayout(context);
        mAudio.setLayoutParams(new LayoutParams(130,130));
        mAudio.setId(Utils.GenerateViewId());
        mAudio.setGravity(Gravity.CENTER);

        ImageView ivMic = new ImageView(context);
        ivMic.setImageDrawable(getResources().getDrawable(R.drawable.ic_mic_none_24dp));
        mAudio.addView(ivMic);

        mAudioText = new TextView(context);
        mAudioText.setText("00:00");
        mAudioText.setTextColor(getResources().getColor(R.color.bt_dark_gray));
        mAudioText.setTextSize(TypedValue.COMPLEX_UNIT_SP,17);
        mAudioText.setPadding(20,0,0,0);
        mAudioText.setVisibility(GONE);

        mAddFile = new LinearLayout(context);
        mAddFile.setLayoutParams(new LayoutParams(120,120));
        mAddFile.setId(Utils.GenerateViewId());
        mAddFile.setGravity(Gravity.CENTER);

        final ImageView ivAdd = new ImageView(context);
        ivAdd.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_24dp));
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
        lnContent.setPadding(10,10,10,10);

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

            t = new CountDownTimer( Long.MAX_VALUE , 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    cnt++;
                    int seconds = cnt;
                    int minutes = seconds / 60;
                    seconds     = seconds % 60;
                    mAudioText.setText(String.format("%02d:%02d", minutes, seconds));
                }
                @Override
                public void onFinish() {}
            };

            mAudio.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
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
                                if(cnt > 1) {
                                    MessageModel messageModel = new MessageModel();
                                    messageModel.setType(Utils.MESSAGE_TYPE.AUDIO);
                                    messageModel.setMessage(FileToBase64(audioFile));
                                    messageModel.setDuration(cnt);

                                    if (audioFile != null && audioFile.length() > 0)
                                        abc.sendMessage(userName, AESUtils.encrypt(gson.toJson(messageModel)), GetStringDate(), Utils.MESSAGE_TYPE.AUDIO);

                                }
                                cnt = 0;

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
                }
            });
        }

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if(!"".equals(mOutEditText.getText().toString().trim())) {

                        MessageModel messageModel = new MessageModel();
                        messageModel.setType(Utils.MESSAGE_TYPE.MESSAGE);
                        messageModel.setMessage(mOutEditText.getText().toString());
                        abc.sendMessage(userName, AESUtils.encrypt(gson.toJson(messageModel)), GetStringDate(), Utils.MESSAGE_TYPE.MESSAGE);
                        mOutEditText.setText("");
                    }
                } catch(Exception e){e.printStackTrace();}
            }
        });

        mAddFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mActivity.startActivity(new Intent(context, UtilActivity.class));

                    clearPersistentVariables();

                    timerHandler.postDelayed(timerRunnable,0);

                    //
                } catch(Exception e){
                    e.printStackTrace();
                }
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
                cnt=0;
                t.start();
            } catch (Exception e) {
                Log.e("Exception in start", "" + e);
            }
        }else{
            try {
                ar.stop();
                t.cancel();
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

                if(!pickFile){
                    try {
                        String pathToFile = settings.getString("pathToFile", "");
                        File file = new File(pathToFile);
                        MessageModel messageModel = new MessageModel();
                        messageModel.setMessage(FileToBase64(Utils.getCompressedImageFile(file, context)));
                        messageModel.setType(Utils.MESSAGE_TYPE.PHOTO);
                        abc.sendMessage(userName, AESUtils.encrypt(gson.toJson(messageModel)), GetStringDate(), settings.getString("fileType", Utils.MESSAGE_TYPE.PHOTO));
                        isPickingFile = false;
                        boolean deleted = deleteFile(file);
                        if(!deleted) Log.d(TAG,"CAN'T DELETE FILE!!!");
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                else
                    timerHandler.postDelayed(timerRunnable,1000);
            }

        }
    };


}

