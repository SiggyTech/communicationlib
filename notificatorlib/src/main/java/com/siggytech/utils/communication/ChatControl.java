package com.siggytech.utils.communication;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.TELEPHONY_SERVICE;


public class ChatControl extends RelativeLayout {
    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_WRITE = 2;
    private ListView mConversationView;
    private EditText mOutEditText;
    private EditText mServerAddress;
    private LinearLayout mSendButton;
    private ArrayAdapter<String> mConversationArrayAdapter;

    public String imei;
    public String name;
    public String api_key;
    public String userName;
    public int idGroup;
    private Context context;
    private ChatListView abc;
    private String messageTittle;
    private String messageText;
    private String packageName;
    private int resIcon;
    private String notificationMessage;


    public ChatControl(Context context, int idGroup, String API_KEY, String nameClient, String userName,
                       String messageTittle, String messageText, String packageName, int resIcon, String notificationMessage){
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

    public void initLayout(Context context) {
        int idContent = Utils.generateViewId();

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
        rl.setId(Utils.generateViewId());

        mOutEditText = new EditText(context);
        mOutEditText.setId(Utils.generateViewId());
        mOutEditText.setMaxLines(5);
        mOutEditText.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        mOutEditText.setBackgroundResource(R.drawable.gradientbg);

        mSendButton = new LinearLayout(context);
        mSendButton.setLayoutParams(new LayoutParams(120,120));
        mSendButton.setId(Utils.generateViewId());
        mSendButton.setBackgroundResource(R.drawable.circle_green);
        mSendButton.setGravity(Gravity.CENTER);

        ImageView iv = new ImageView(context);
        iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_send_24dp));
        mSendButton.addView(iv);

        RelativeLayout.LayoutParams mContentParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        mContentParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        LinearLayout lnContent = new LinearLayout(context);
        lnContent.setId(idContent);
        lnContent.setLayoutParams(mContentParams);
        lnContent.setBackgroundColor(getResources().getColor(R.color.light_grey));
        lnContent.setPadding(10,10,10,10);

        LinearLayout lnSend = getLnContentSum(5);
        LinearLayout lnH1 = getLnWeight(4);
        lnH1.setVerticalGravity(Gravity.CENTER);
        LinearLayout lnH2 = getLnWeight(1);
        lnH2.setGravity(Gravity.CENTER|Gravity.BOTTOM);

        lnH1.addView(mOutEditText);
        lnH2.addView(mSendButton);
        lnSend.addView(lnH1);
        lnSend.addView(lnH2);
        lnContent.addView(lnSend);

        this.addView(lnContent);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String m = AESUtils.encrypt(mOutEditText.getText().toString());
                    SimpleDateFormat sdf;
                    switch(Conf.DATE_FORMAT) {
                        case 0:
                            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                            break;
                        case 1:
                            sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
                            break;
                        default:
                            sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
                            break;
                    }

                    Date now = new Date();
                    String strDate = sdf.format(now);
                    abc.sendMessage(userName, m, strDate);
                    mOutEditText.setText("");
                }
                catch(Exception e){e.printStackTrace();}
            }
        });
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


}

