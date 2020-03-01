package com.siggytech.utils.communication;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Base64;
import android.view.View;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils {
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public class MESSAGE_TYPE{
        public static final String MESSAGE = "Message";
        public static final String AUDIO = "audio";
        public static final String PHOTO = "photo";
        public static final String VIDEO = "video";
    }

    @SuppressLint("NewApi")
    public static int GenerateViewId() {

        if (Build.VERSION.SDK_INT < 17) {
            for (;;) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF)
                    newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        } else {
            return View.generateViewId();
        }
    }

    /**
     * Method that gets a name for a picture
     * @return name of picture
     */
    public static String GetDateName(){
        Calendar calendar = Calendar.getInstance();
        return String.valueOf(calendar.getTimeInMillis());
    }

    public static String GetStringDate(){
        String strDate = "";
        try {
            SimpleDateFormat sdf;
            switch (Conf.DATE_FORMAT) {
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

            Calendar cal = Calendar.getInstance();
            strDate = sdf.format(cal.getTime());
        }catch (Exception e){
            e.printStackTrace();
        }
        return strDate;
    }

    /**
     * Converts a File to Base64
     * @param file file
     * @return base64
     */
    public static String FileToBase64(File file){
        String base64="";
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();

            base64 = ToBase64(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return base64;
    }


    public static String ToBase64(byte[] byteArray){
        return Base64.encodeToString(byteArray,Base64.NO_WRAP);
    }

}