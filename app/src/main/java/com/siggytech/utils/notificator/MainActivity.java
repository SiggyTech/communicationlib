package com.siggytech.utils.notificator;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.siggytech.utils.communication.model.GroupModel;
import com.siggytech.utils.communication.model.async.TaskMessage;
import com.siggytech.utils.communication.presentation.chat.ChatControl;
import com.siggytech.utils.communication.presentation.chat.HeaderListener;
import com.siggytech.utils.communication.presentation.common.CallBack;
import com.siggytech.utils.communication.presentation.ptt.PTTButton;
import com.siggytech.utils.communication.presentation.register.Siggy;
import com.siggytech.utils.communication.presentation.register.SiggyRegisterAsync;
import com.siggytech.utils.communication.presentation.register.SiggyRegisterListener;
import com.siggytech.utils.communication.util.Conf;
import com.siggytech.utils.notificator.databinding.ActivityMainBinding;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements LifecycleOwner {

    PTTButton pttButton;
    ChatControl ch;

    String API_KEY = "HGDJLGOPQJZGMIPEHBSJ";
    String username = "";

    boolean isChat = true;
    ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_main);


        setSupportActionBar(mBinding.toolbar);

        //You need paste this line, is very important
        Conf.APPLICATION_ID = BuildConfig.APPLICATION_ID;

        Conf.SERVER_IP = "35.247.219.199"; //Set dedicated IP server.
        Conf.CHAT_BASIC = false;
        Conf.ENABLE_LOG_TRACE = true;  //Only for debug

        Conf.SERVER_PORT = 9006;
        Conf.SERVER_WS_PORT = 9005;
        Conf.TOKEN_PORT = 9004;

        username = Build.MODEL;

        //check permissions
        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO};

            if (!hasPermissions(this, PERMISSIONS))
                ActivityCompat.requestPermissions(this, PERMISSIONS, 112 );
            else
                initSiggy();
        }
        else initSiggy();

    }


    /**
     * You must call this initial method
     */
    private void initSiggy(){
        if(Siggy.isRegister()) startSiggy();
        else registerSiggy();

        //This call need be called after chat init
        onNewIntent(getIntent());
    }


    private void registerSiggy(){

        new SiggyRegisterAsync(
                MainActivity.this,
                new SiggyRegisterListener() {
                    @Override
                    public void onPreExecute() {
                        //Do your stuff
                    }

                    @Override
                    public void onPostExecute(Object result) {
                        String message;

                        // Registration failed?
                        if (result instanceof Exception) {

                            // Display error in alert
                            message = ((Exception) result).getMessage();
                        }
                        else {
                            message = "Siggy device token: " + result.toString() + "\n\n";
                            startSiggy();
                        }

                        // Registration succeeded, display an alert with the device token
                        new android.app.AlertDialog.Builder(MainActivity.this)
                                .setTitle("Siggy")
                                .setMessage(message)
                                .setPositiveButton(android.R.string.ok, null)
                                .show();
                    }
                }
        ).execute(API_KEY,username);
    }

    private void startSiggy(){
        if(isChat)
            addChatListView();
        else
            addPTTButton();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e("KUSSE","DOWN Key code: "+keyCode);
        if(keyCode == 142) {
            if (pttButton != null)
                return pttButton.onKeyDown(keyCode, event);
        }
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.e("KUSSE","UP Key code: "+keyCode);
        if(keyCode == 142) {
            if(pttButton!=null) pttButton.onKeyUp(keyCode,event);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initSiggy();
        } else {
            Toast.makeText(MainActivity.this, "Missing implement.", Toast.LENGTH_LONG).show();
        }
    }


    private void addPTTButton(){
        pttButton = new PTTButton(
                this,
                API_KEY,
                username,
                PTTButton.AudioQuality.LOW,
                false,
                new CallBack() {
                    @Override
                    public void onPreExecute() {
                        Objects.requireNonNull(getSupportActionBar()).setSubtitle(getString(R.string.connecting));
                    }

                    @Override
                    public void onReady(TaskMessage result) {
                        Objects.requireNonNull(getSupportActionBar()).setTitle(result.getMessage());
                        Objects.requireNonNull(getSupportActionBar()).setSubtitle(getString(R.string.connected));
                    }
                },
                getLifecycle());

        CoordinatorLayout coordinatorLayout = new CoordinatorLayout(this);
        coordinatorLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        coordinatorLayout.addView(pttButton);
        mBinding.frame.addView(coordinatorLayout);

        Snackbar.make(mBinding.frame,username,Snackbar.LENGTH_INDEFINITE).setAction("Search", v -> {
            List<GroupModel> list = pttButton.getGroupList();
            if(!list.isEmpty()) {
                StringBuilder groups = new StringBuilder();
                for(GroupModel g : list){
                    groups.append(" ").append(g.idGroup);
                }
                Snackbar.make(mBinding.frame,groups.toString(),Snackbar.LENGTH_INDEFINITE).show();
            }

        }).show();
    }


    @Override
    public void onNewIntent(Intent intent){
        if(ch!=null) ch.onNewIntent(intent.getExtras());
        super.onNewIntent(intent);
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void addChatListView() {
        Conf.DATE_FORMAT = 2; //dd-mm-yyyy hh24:mm:ss
        Conf.LOCAL_USER = "Yo"; //user name to show in my device. Default: Me
        Conf.CHAT_DARK_MODE = false;


        ch = new ChatControl(
                this,
                API_KEY,
                username,
                getLifecycle(),
                new CallBack() {
                    @Override
                    public void onPreExecute() {
                        runOnUiThread(() -> Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.connecting)));
                    }

                    @Override
                    public void onReady(TaskMessage result) {
                        runOnUiThread(() ->  Objects.requireNonNull(getSupportActionBar()).setTitle(result.getMessage()));

                    }
                },
                new HeaderListener() {
                    @Override
                    public void onTitleChanged(String result) {
                        Objects.requireNonNull(getSupportActionBar()).setTitle(result);
                    }

                    @Override
                    public void onSubtitleChanged(String result) {
                        Objects.requireNonNull(getSupportActionBar()).setSubtitle(result);;
                    }
                });

        mBinding.frame.addView(ch);

        if(false) ch.deleteHistory();
    }

    /**
     * For change de view to a specific group
     * @param idGroup id group to switch
     */
    private void changeGroupView(long idGroup){

        if(isChat && ch!=null)
            ch.setGroupView(idGroup, 10);
        else if(!isChat && pttButton!=null)
            pttButton.setGroup(idGroup);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_help,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(R.id.menuGroup == item.getItemId()){
            changeGroup();
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeGroup(){
        try{
            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);

            TextInputLayout tilHelper = dialog.findViewById(R.id.tilHelper);
            AutoCompleteTextView autoHelper = dialog.findViewById(R.id.autoHelper);

            ArrayAdapter<GroupModel> adapter =
                    new ArrayAdapter<>(MainActivity.this,
                            R.layout.dropdown_menu_popup_item,
                            isChat?ch.getGroupList():pttButton.getGroupList());

            autoHelper.setAdapter(adapter);

            autoHelper.setOnItemClickListener((parent, view, position, id) -> {
                tilHelper.setErrorEnabled(false);
                GroupModel group = (GroupModel) parent.getItemAtPosition(position);
                if (group != null) {
                    changeGroupView(group.idGroup);
                    Objects.requireNonNull(getSupportActionBar()).setTitle(""+group.idGroup);
                    dialog.dismiss();
                }else{
                    tilHelper.setErrorEnabled(true);
                    tilHelper.setError("null group");
                }
            });

            dialog.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
