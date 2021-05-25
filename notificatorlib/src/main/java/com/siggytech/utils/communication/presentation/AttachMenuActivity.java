package com.siggytech.utils.communication.presentation;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.siggytech.utils.communication.R;
import com.siggytech.utils.communication.util.Conf;
import com.siggytech.utils.communication.util.FilePath;
import com.siggytech.utils.communication.util.Utils;

import java.io.File;

import static com.siggytech.utils.communication.presentation.chat.ChatControl.SELECT_FILE;
import static com.siggytech.utils.communication.util.DateUtil.getDateName;

/**
 * @author K.Kusses
 * @since 2020-02-28
 */
public class AttachMenuActivity extends AppCompatActivity {
    private static final String TAG = AttachMenuActivity.class.getSimpleName();
    private static final int ACTIVITY_START_CAMARA_APP = 0;

    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_attach_menu);

        System.out.println("Package name: " + getApplicationContext().getPackageName());

        if(getWindow()!=null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // Make us non-modal, so that others can receive touch events.
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
            // ...but notify us that it happened.
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        }

        ImageView ivCamera = findViewById(R.id.ivCamera);
        ivCamera.setColorFilter(Conf.CHAT_COLOR_COMPONENTS);
        ImageView ivPhotoVideo = findViewById(R.id.ivPhotoVideo);
        ivPhotoVideo.setColorFilter(Conf.CHAT_COLOR_COMPONENTS);
        TextView tvCancel =  findViewById(R.id.tvCancel);
        tvCancel.setTextColor(Conf.CHAT_COLOR_COMPONENTS);

        LinearLayout lnCamera = findViewById(R.id.lnCamera);
        LinearLayout lnPhotoVideo = findViewById(R.id.lnPhotoVideo);
        LinearLayout lnCancel = findViewById(R.id.lnCancel);

        lnCamera.setOnClickListener(this::takePhoto);

        lnPhotoVideo.setOnClickListener(v -> {
            Intent chooser;
          if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                chooser = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            else chooser = new Intent(Intent.ACTION_GET_CONTENT);
            chooser.setType("image/*, video/*");
            if (chooser.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(Intent.createChooser(chooser, "Select File"), SELECT_FILE);
            }
        });

        lnCancel.setOnClickListener(v -> finish());

        if(Conf.CHAT_DARK_MODE) {
            CardView cardOptions = findViewById(R.id.cardOptions);
            cardOptions.setCardBackgroundColor(getResources().getColor(R.color.primaryLightColorDark));
            CardView cardCancel = findViewById(R.id.cardCancel);
            cardCancel.setCardBackgroundColor(getResources().getColor(R.color.primaryLightColorDark));
            TextView tvPhotoVideoTitle = findViewById(R.id.tvPhotoVideoTitle);
            tvPhotoVideoTitle.setTextColor(getResources().getColor(R.color.textColorDark));
            TextView tvCameraTitle = findViewById(R.id.tvCameraTitle);
            tvCameraTitle.setTextColor(getResources().getColor(R.color.textColorDark));
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ACTIVITY_START_CAMARA_APP && resultCode == RESULT_OK){
            getContentResolver().notifyChange(MessengerHelper.getLastUri(), null);
            setPathToFile( Utils.MESSAGE_TYPE.PHOTO);
            finish();
        }else if(requestCode == ACTIVITY_START_CAMARA_APP && resultCode == RESULT_CANCELED){
            finish();
        }else if(requestCode == SELECT_FILE && data!=null){
            MessengerHelper.setLastUri(data.getData());

            String selectedImagePath = FilePath.getPath(getApplicationContext(), data.getData());

            if(selectedImagePath!=null) {
                File filex = new File(selectedImagePath);
                if (filex.exists()) Log.d(TAG, "EXISTS");

                switch (selectedImagePath.substring(selectedImagePath.lastIndexOf(".") + 1).toUpperCase()) {
                    case "JPG":
                    case "JPEG":
                    case "BMP":
                    case "TIFF":
                    case "PNG":
                        setPathToFile(Utils.MESSAGE_TYPE.PHOTO);
                        break;
                    case "3GP":
                    case "MP4":
                    case "MPEG":
                        setPathToFile(Utils.MESSAGE_TYPE.VIDEO);
                        break;
                    default:
                        setPathToFile(Utils.MESSAGE_TYPE.FILE);
                        break;
                }
            }else{
                Toast.makeText(context,getString(R.string.not_supported),Toast.LENGTH_LONG).show();
            }
            finish();
        }
    }

    /**
     * Takes pic
     * @param view view
     * */
    public void takePhoto(View view){
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ContentValues values = new ContentValues(1);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
            MessengerHelper.setLastUri(getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values));
            captureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            File file = new File(Environment.getExternalStorageDirectory(), getDateName()+".jpg");
            MessengerHelper.setLastUri(Uri.fromFile(file));
        }
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, MessengerHelper.getLastUri());
        startActivityForResult(captureIntent, ACTIVITY_START_CAMARA_APP);
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("pickFile", true);
        editor.commit();
    }


    private void setPathToFile(String type){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("pickFile", false);  //set as finish this actitivy
        editor.putString("fileType", type);
        editor.commit();
    }
}
