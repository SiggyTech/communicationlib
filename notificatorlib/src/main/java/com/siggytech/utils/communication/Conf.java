package com.siggytech.utils.communication;

import android.graphics.Color;
import android.os.Environment;

public class Conf {

    public static String SERVER_IP = "192.168.1.148";//cloud

    public static int SERVER_PORT = 7778;
    public static int SERVER_WS_PORT = 8080;
    public static int SERVER_MSG_PORT = 8081;
    public static int TOKEN_PORT = 8082;
    public static int SERVER_CHAT_PORT = 3000;
    public static int SERVER_CHAT_PORT_IN = 3001;
    public static int SERVER_IMAGE_PORT = 3002;
    public static int DATE_FORMAT = 0; //1: "yyyy-MM-dd HH:mm:ss" 2:"dd-MM-yyyy HH:mm:ss"
    public static String LOCAL_USER = "Me";
    public static int CHAT_COLOR_FROM = Color.BLACK;
    public static int CHAT_COLOR_TEXT = Color.BLACK;
    public static int CHAT_COLOR_DATE = Color.DKGRAY;
    public static int CHAT_COLOR_COMPONENTS = Color.parseColor("#2155C0");
    public static boolean CHAT_DARK_MODE = false;
    public static boolean CHAT_BASIC = false;
    public static String ROOT_PATH = Environment.getExternalStorageDirectory()+"/SIGGI/";
    public static boolean SEND_FILES = true;
    public static String HTTP = "http://";
    public static int NOTIFICATION_ID_FOREGROUND_SERVICE = 8466503;
    public static String FOREGROUND_CHANNEL_ID = "foreground_channel_id_56";
    public static int COMM_NOTIFICATION_FOREGROUND_ID = 8577309;
    public static String COMM_NOTIFICATION_CHANNEL_ID = "com.siggy.websocketservice";
    public static String COMM_NOTIFICATION_CHANNEL_NAME = "Communication Background Service";
    public static String COMM_NOTIFICATION_CONTENT_TITLE = "App is running in background";
    public static boolean ENABLE_LOG_TRACE = false;


}