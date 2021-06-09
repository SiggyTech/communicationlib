package com.siggytech.utils.communication.util;

import android.graphics.Color;

public class Conf {

    public static String SERVER_IP;

    public static int SERVER_PORT = 9006;
    //public static int SERVER_PORT = 7778;
    //public static int SERVER_WS_PORT = 8080;
    public static int SERVER_WS_PORT = 9005;

    public static int TOKEN_PORT = 9004;
    //public static int TOKEN_PORT = 8082;

//    public static int SERVER_CHAT_PORT = 3000;//UDP
    public static int SERVER_CHAT_PORT = 9002;//UDP

    //    public static int SERVER_CHAT_PORT_IN = 3001;//UDP
    public static int SERVER_CHAT_PORT_IN = 9001;//UDP

    //public static int SERVER_IMAGE_PORT = 3002;//TCP
    public static int SERVER_IMAGE_PORT = 9008;//TCP

    public static final int SERVER_REGISTER_DEVICE = 9007;

    public static int DATE_FORMAT = 0; //1: "yyyy-MM-dd HH:mm:ss" 2:"dd-MM-yyyy HH:mm:ss"
    public static String LOCAL_USER = "Me";
    public static int CHAT_COLOR_FROM = Color.BLACK;
    public static int CHAT_COLOR_TEXT = Color.BLACK;
    public static int CHAT_COLOR_DATE = Color.DKGRAY;
    public static int CHAT_COLOR_COMPONENTS = Color.parseColor("#2155C0");
    public static boolean CHAT_DARK_MODE = false;
    public static boolean CHAT_BASIC = false;
    public static String ROOT_FOLDER = "siggy";
    public static String HTTP = "http://";
    public static int COMM_NOTIFICATION_FOREGROUND_ID = 8577309;
    public static String COMM_NOTIFICATION_CHANNEL_ID = "com.siggy.websocketservice";
    public static String COMM_NOTIFICATION_CHANNEL_NAME = "Communication Background Service";
    public static String COMM_NOTIFICATION_CONTENT_TITLE = "App is running in background";
    public static boolean ENABLE_LOG_TRACE = false;
    public static final String LOG_FILE_NAME = "notificatorTrace.txt";
    public static String APPLICATION_ID = "your.application.id";


}