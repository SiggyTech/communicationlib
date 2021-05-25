package com.siggytech.utils.communication.presentation.register;

import android.content.Context;

import com.siggytech.utils.communication.model.RegisterModel;
import com.siggytech.utils.communication.model.async.ApiService;
import com.siggytech.utils.communication.util.FileUtil;

public class Siggy {

    public static final String DEVICE_TOKEN = "deviceToken.txt";

    public static String register(String apiKey, String username, Context context) throws Exception{
        return ApiService.registerDevice(new RegisterModel(apiKey,username),context);
    }

    public static boolean isRegister(){
        if(FileUtil.fileExists("", DEVICE_TOKEN)){
            String deviceToken = getDeviceToken();
            return deviceToken!=null && !"".equalsIgnoreCase(deviceToken.trim());
        }
        else
            return false;
    }

    public static String getDeviceToken(){
        return FileUtil.readFile(DEVICE_TOKEN,"");
    }


}
