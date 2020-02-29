package com.siggytech.utils.communication;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import static com.siggytech.utils.communication.ChatControl.SELECT_FILE;
import static com.siggytech.utils.communication.Utils.getDateName;

/**
 * @author Kussess
 * @since 2020-02-28
 */
public class UtilActivity extends AppCompatActivity {
    private static final String TAG = UtilActivity.class.getSimpleName();
    private static final int ACTIVITY_START_CAMARA_APP = 0;
    private String mImageFileLocation = "";
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_util);

        if(getWindow()!=null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // Make us non-modal, so that others can receive touch events.
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
            // ...but notify us that it happened.
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        }

        LinearLayout lnCamera = findViewById(R.id.lnCamera);
        LinearLayout lnPhotoVideo = findViewById(R.id.lnPhotoVideo);
        LinearLayout lnCancel = findViewById(R.id.lnCancel);

        lnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto(v);
            }
        });

        lnPhotoVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooser = new Intent(Intent.ACTION_GET_CONTENT);
                Uri uri = Uri.parse(Environment.getDownloadCacheDirectory().getPath());
                chooser.addCategory(Intent.CATEGORY_OPENABLE);
                chooser.setDataAndType(uri, "*/*");
                try {
                    startActivityForResult(chooser, SELECT_FILE);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(context, "Por favor instale un gestor de archivos (File Manager).",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        lnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        createFolder();

    }

    /**
     * Este metodo está sobreescrito dado que es requerido por uno de los fragments que esta
     * implementando este activity
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "Entro al onActivityResult del Main el codigo " + requestCode);
        if(requestCode == ACTIVITY_START_CAMARA_APP && resultCode == RESULT_OK){
            File imgFile = new File(mImageFileLocation);

        }else if(requestCode == ACTIVITY_START_CAMARA_APP && resultCode == RESULT_CANCELED){
            File imgFile = new File(mImageFileLocation);

        }else if(requestCode == SELECT_FILE && data!=null){
            String selectedImagePath = FilePath.getPath(getApplicationContext(), data.getData());
           /* FileModel fileModel = null;

            if(selectedImagePath!=null){
                fileModel = setFileMetaData(selectedImagePath);
            }

            if(fileModel!=null && fileModel.getPath()!=null) {
                showDialogDoc(fileModel);
            }else{
                Toast.makeText(getActivity(),"Error al adjuntar documento.",Toast.LENGTH_SHORT).show();
            }*/
        }
    }

    /**
     * Metodo que se encarga de tomar la fotografia
     * @param view vista
     * */
    public void takePhoto(View view){

        Intent callCamaraApplicationIntent = new Intent();
        callCamaraApplicationIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = null;
        try {
            photoFile = createImageFile();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            callCamaraApplicationIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", photoFile));
        }else{
            callCamaraApplicationIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        }

        startActivityForResult(callCamaraApplicationIntent, ACTIVITY_START_CAMARA_APP);
    }

    /**
     * Metodo que se encarga de crear la fotografia en formato jpg en la carpeta SAG_IMAGES
     * @return fotografía en formato File
     * @throws IOException
     * */
    private File createImageFile() throws IOException{
        String imageFileName;
        File storageDirectory = new File(Conf.ROOT_PATH);

        imageFileName = getDateName();

        File image = File.createTempFile(imageFileName, ".jpg", storageDirectory);
        mImageFileLocation = image.getAbsolutePath();

        return image;
    }


    /**
     * Creates a content folder for files.
     * */
    private void createFolder(){


        File directory = new File(Conf.ROOT_PATH);

        try {
            if (directory.mkdirs()) {
                Log.v(TAG, "Directory created");
            } else {
                Log.v(TAG, "Directory is not created");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
