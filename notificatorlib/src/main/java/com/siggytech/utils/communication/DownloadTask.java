package com.siggytech.utils.communication;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.siggytech.view.MyImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask {
    private static final String TAG = DownloadTask.class.getSimpleName();
    private Context context;
    private String downloadUrl = "", downloadFileName = "";
    private MyImage mImage;

    public DownloadTask(Context context, String downloadUrl, MyImage imageView) {
        this.context = context;
        this.downloadUrl = downloadUrl;
        this.mImage = imageView;
        this.downloadFileName = downloadUrl.substring(downloadUrl.lastIndexOf( '/' ),downloadUrl.length());//Create file name by picking download file name from URL

        //Start Downloading Task
        new DownloadingTask().execute();
    }

    private class DownloadingTask extends AsyncTask<Void, Void, Void> {
        File fileStorage = null;
        File outputFile = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mImage.getProgressBar().show();
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if (outputFile != null) {
                    //success
                    mImage.setRoundImage(BitmapFactory.decodeFile(outputFile.getPath()));

                    mImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                Uri uri = Uri.parse(outputFile.getPath());
                                Intent intent = new Intent(context, TransitionToActivity.class);
                                intent.putExtra("ImageUri", uri.toString());
                                context.startActivity(intent);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                    mImage.getProgressBar().hide();
                } else {
                    //fail
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                        }
                    }, 3000);
                    Toast.makeText(context, "Fail", Toast.LENGTH_SHORT).show();
                    if(outputFile.exists()){
                        outputFile.delete();
                    }
                    mImage.getProgressBar().hide();
                    //TODO esto cambiarlo por una x y ademas debe ser con round
                    mImage.getImageView().setImageDrawable(new ColorDrawable(Color.RED));
                }
            } catch (Exception e) {
                e.printStackTrace();

                //Change button text if exception occurs

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                    }
                }, 3000);
                Log.e(TAG, "Download Failed with Exception - " + e.getLocalizedMessage());
                if(outputFile!=null && outputFile.exists()){
                    outputFile.delete();
                }
            }
            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                URL url = new URL(downloadUrl);//Create Download URl
                HttpURLConnection c = (HttpURLConnection) url.openConnection();//Open Url Connection
                c.setRequestMethod("GET");//Set Request Method to "GET" since we are grtting data
                c.connect();//connect the URL Connection

                //If Connection response is not OK then show Logs
                if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Server returned HTTP " + c.getResponseCode()
                            + " " + c.getResponseMessage());

                }
                
                fileStorage = new File(Conf.ROOT_PATH);
                
                //If File is not present create directory
                if (!fileStorage.exists()) {
                    fileStorage.mkdir();
                    Log.e(TAG, "Directory Created.");
                }

                outputFile = new File(fileStorage, downloadFileName);//Create Output file in Main File

                //Create New File if not present
                if (!outputFile.exists()) {
                    outputFile.createNewFile();
                    Log.e(TAG, "File Created");
                }

                FileOutputStream fos = new FileOutputStream(outputFile);//Get OutputStream for NewFile Location

                InputStream is = c.getInputStream();//Get InputStream for connection

                byte[] buffer = new byte[1024];//Set buffer type
                int len1 = 0;//init length
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);//Write new file
                }

                //Close all connection after doing task
                fos.close();
                is.close();

            } catch (Exception e) {
                //Read exception if something went wrong
                e.printStackTrace();
                outputFile = null;
                Log.e(TAG, "Download Error Exception " + e.getMessage());
            }

            return null;
        }
    }
}
