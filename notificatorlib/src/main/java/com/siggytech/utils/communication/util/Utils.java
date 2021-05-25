package com.siggytech.utils.communication.util;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.view.View;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.util.concurrent.atomic.AtomicInteger;

import static com.siggytech.utils.communication.util.Conf.ENABLE_LOG_TRACE;
import static com.siggytech.utils.communication.util.DateUtil.getCurrentDate;

public class Utils {
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public static class MESSAGE_TYPE{
        public static final String MESSAGE = "Message";
        public static final String AUDIO = "audio";
        public static final String PHOTO = "photo";
        public static final String VIDEO = "video";
        public static final String FILE = "file";
    }

    @SuppressLint("NewApi")
    public static int generateViewId() {
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

    public static Gson getGson(){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return  f.getAnnotation(Expose.class)!=null;
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        });
        gsonBuilder.disableHtmlEscaping();
        return gsonBuilder.serializeNulls().create();
    }



    public static String exceptionToString(Exception e){
        if(e!=null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Excepción: ");
            sb.append(e.getMessage());
            sb.append("\n");
            StackTraceElement[] array = e.getStackTrace();
            if (array != null) {
                for (StackTraceElement element : array) {
                    if (element.getClassName() != null) {
                        sb.append("Class name: ");
                        sb.append(element.getClassName());
                        sb.append("\n");
                    }
                    if (element.getFileName() != null) {
                        sb.append("File name: ");
                        sb.append(element.getFileName());
                        sb.append("\n");
                    }
                    if (element.getMethodName() != null) {
                        sb.append("Method name: ");
                        sb.append(element.getMethodName());
                        sb.append("\n");
                    }
                    sb.append("Line number: ");
                    sb.append(element.getLineNumber());
                    sb.append("\n");
                    break;
                }
            }
            return sb.toString();
        }else return "Exception is null";
    }


    public static boolean isServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isConnect(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    public static void traces(String text){
        if(ENABLE_LOG_TRACE){
            if(!FileUtil.fileExists("",Conf.LOG_FILE_NAME)){
                FileUtil.saveFile(Conf.LOG_FILE_NAME,"",getCurrentDate()+": "+text);
            }else{
                FileUtil.writeInFile(FileUtil.getFile("",Conf.LOG_FILE_NAME),getCurrentDate()+": "+text);
            }
        }
    }

}