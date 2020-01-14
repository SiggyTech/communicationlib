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
import android.widget.LinearLayout;
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
    public String userName;
    public int idGroup;
    private Context context;
    ChatListView abc;


    public ChatControl(Context context, int idGroup, String API_KEY, String nameClient, String userName){
        super(context);

        this.context = context;
        this.idGroup = idGroup;
        this.api_key = API_KEY;
        this.name = nameClient;
        this.imei = getIMEINumber();
        this.userName = userName;
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

        ViewGroup.LayoutParams root_LayoutParams =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        root_LayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        root_LayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        //root.setPadding((int) getResources().getDimension(R.dimen.activity_horizontal_margin), (int) getResources().getDimension(R.dimen.activity_vertical_margin), 0, (int) getResources().getDimension(R.dimen.activity_vertical_margin));
        //tools:context="com.example.androidtest.MainActivity"    //not support
        this.setLayoutParams(root_LayoutParams);

        RelativeLayout rl = new RelativeLayout(context);
        abc = new ChatListView(context, idGroup, api_key, name);
        abc.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        RelativeLayout.LayoutParams abc_LayoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        //abc.setId(@id/abc);

        abc_LayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        abc_LayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        abc_LayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        abc_LayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        rl.addView(abc);
        this.addView(rl);
        rl.setLayoutParams(abc_LayoutParams);
        rl.setId(generateViewId());

        mOutEditText = new EditText(context);
        mOutEditText.setId(generateViewId());
        mSendButton = new Button(context);
        mSendButton.setId(generateViewId());
        mSendButton.setText("Enviar");

        RelativeLayout.LayoutParams mOutEditTextParams = new LayoutParams(300,LayoutParams.WRAP_CONTENT);
        mOutEditTextParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        mOutEditTextParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);//.addRule(RelativeLayout.END_OF, rl.getId());
        mOutEditText.setLayoutParams(mOutEditTextParams);

        RelativeLayout.LayoutParams mSendButtonParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        mSendButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mSendButtonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        mSendButton.setLayoutParams(mSendButtonParams);

        this.addView(mOutEditText);
        this.addView(mSendButton);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abc.sendMessage(userName, mOutEditText.getText().toString());
                mOutEditText.setText("");
            }
        });


    }
}
