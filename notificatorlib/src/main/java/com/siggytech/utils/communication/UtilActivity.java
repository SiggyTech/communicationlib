package com.siggytech.utils.communication;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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

import java.io.File;

import static com.siggytech.utils.communication.ChatControl.SELECT_FILE;
import static com.siggytech.utils.communication.Utils.GetDateName;

/**
 * @author K.Kusses
 * @since 2020-02-28
 */
public class UtilActivity extends AppCompatActivity {
    private static final String TAG = UtilActivity.class.getSimpleName();
    private static final int ACTIVITY_START_CAMARA_APP = 0;
    Uri outputFileUri;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_util);

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
            Intent chooser = new Intent(Intent.ACTION_GET_CONTENT);
            Uri uri = Uri.parse(Environment.getDownloadCacheDirectory().getPath());
            chooser.addCategory(Intent.CATEGORY_OPENABLE);
            chooser.setDataAndType(uri, "*/*");
            try {
                startActivityForResult(chooser, SELECT_FILE);
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(context, "Please install app to open it.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        lnCancel.setOnClickListener(v -> finish());


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ACTIVITY_START_CAMARA_APP && resultCode == RESULT_OK){
            ContentResolver cr = getContentResolver();
            getContentResolver().notifyChange(outputFileUri, null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setPathToFile(getImageFilePath(outputFileUri), Utils.MESSAGE_TYPE.PHOTO);
            }else{
                setPathToFile(outputFileUri.getPath(), Utils.MESSAGE_TYPE.PHOTO);
            }
            finish();
        }else if(requestCode == ACTIVITY_START_CAMARA_APP && resultCode == RESULT_CANCELED){
            finish();
        }else if(requestCode == SELECT_FILE && data!=null){
            String selectedImagePath = FilePath.getPath(getApplicationContext(), data.getData());
            if(selectedImagePath!=null) {
                File filex = new File(selectedImagePath);
                if (filex.exists()) Log.d(TAG, "EXISTS");

                switch (selectedImagePath.substring(selectedImagePath.lastIndexOf(".") + 1).toUpperCase()) {
                    case "JPG":
                        setPathToFile(FilePath.getPath(getApplicationContext(), data.getData()), Utils.MESSAGE_TYPE.PHOTO);
                        break;
                    case "JPEG":
                        setPathToFile(FilePath.getPath(getApplicationContext(), data.getData()), Utils.MESSAGE_TYPE.PHOTO);
                        break;
                    case "BMP":
                        setPathToFile(FilePath.getPath(getApplicationContext(), data.getData()), Utils.MESSAGE_TYPE.PHOTO);
                    case "TIFF":
                    case "PNG":
                        setPathToFile(FilePath.getPath(getApplicationContext(), data.getData()), Utils.MESSAGE_TYPE.PHOTO);
                        break;
                    case "3GP":
                        setPathToFile(FilePath.getPath(getApplicationContext(), data.getData()), Utils.MESSAGE_TYPE.VIDEO);
                        break;
                    case "MP4":
                        setPathToFile(FilePath.getPath(getApplicationContext(), data.getData()), Utils.MESSAGE_TYPE.VIDEO);
                        break;
                    case "MPEG":
                        setPathToFile(FilePath.getPath(getApplicationContext(), data.getData()), Utils.MESSAGE_TYPE.VIDEO);
                        break;
                    default:
                        setPathToFile(FilePath.getPath(getApplicationContext(), data.getData()), Utils.MESSAGE_TYPE.FILE);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ContentValues values = new ContentValues(1);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
            outputFileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            captureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            startActivityForResult(captureIntent, ACTIVITY_START_CAMARA_APP);
        } else {
            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File file = new File(Environment.getExternalStorageDirectory(), GetDateName()+".jpg");
            outputFileUri = Uri.fromFile(file);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            startActivityForResult(captureIntent, ACTIVITY_START_CAMARA_APP);
        }
    }


    public String getImageFilePath(Uri uri) {
        String path = null, image_id = null;

        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            image_id = cursor.getString(0);
            image_id = image_id.substring(image_id.lastIndexOf(":") + 1);
            cursor.close();
        }

        cursor = getContentResolver().query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", new String[]{image_id}, null);
        if (cursor!=null) {
            cursor.moveToFirst();
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        }
        return path;
    }


    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("pickFile", true);
        editor.commit();
    }


    private void setPathToFile(String abolutePath, String type){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("pickFile", false);  //set as finish this actitivy
        editor.putString("pathToFile", abolutePath);
        editor.putString("fileType", type);
        editor.commit();
    }
}
