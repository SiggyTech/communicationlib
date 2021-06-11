package com.siggytech.utils.communication.presentation;

import android.net.Uri;

import androidx.lifecycle.Lifecycle;

import com.siggytech.utils.communication.model.EventMessageModel;
import com.siggytech.utils.communication.model.GroupModel;
import com.siggytech.utils.communication.presentation.chat.ChatListView;
import com.siggytech.utils.communication.presentation.ptt.PTTButton;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.WebSocket;

public class MessengerHelper {

    private static final MessengerHelper instance;

    static {
        instance = new MessengerHelper();
    }

    public static MessengerHelper getInstance() {
        return MessengerHelper.instance;
    }

   private static ChatListView chatListView;

    public static ChatListView getChatListView() {
        return chatListView;
    }

    public static void setChatListView(ChatListView chatListView) {
        MessengerHelper.chatListView = chatListView;
    }

    private static PTTButton pttButton;

    public static PTTButton getPttButton() {
        return pttButton;
    }

    public static void setPttButton(PTTButton pttButton) {
        MessengerHelper.pttButton = pttButton;
    }

    private static List<GroupModel> groupList;

    public static List<GroupModel> getGroupList(){
        return groupList;
    }

    public static void setGroupList(List<GroupModel> list) {
        groupList = list;
    }

    public static void clearGroupList() {
        groupList = null;
    }

    public static Uri lastUri;

    public static Uri getLastUri() {
        return lastUri;
    }

    public static void setLastUri(Uri lastUri) {
        MessengerHelper.lastUri = lastUri;
    }

    public static void clearLastUri() {
        lastUri = null;
    }

    private static WebSocket chatListenerSocket;

    public static WebSocket getChatListenerSocket(){
        return chatListenerSocket;
    }

    public static void setChatListenerSocket(WebSocket ws){
        chatListenerSocket = ws;
    }

    public static void clearChatSocket(){
        chatListenerSocket = null;
    }

    private static List<EventMessageModel> chatQueueList;
    public static List<EventMessageModel> getChatQueue(){
        return chatQueueList;
    }
    public static void setChatQueue(List<EventMessageModel> list){
        chatQueueList = list;
    }
    public static void clearChatQueue(){
        if(chatQueueList!=null) chatQueueList.clear();
        chatQueueList = null;
    }

    private static int groupIndex = 0;

    public static int getIndexGroup() {
        return groupIndex;
    }

    public static void setGroupIndex(int groupIndex) {
        MessengerHelper.groupIndex = groupIndex;
    }



    private static List<GroupModel> pttGroupList;
    public static List<GroupModel> getPttGroupList() {
        return pttGroupList;
    }

    public static void setPttGroupList(List<GroupModel> pttGroupList) {
        MessengerHelper.pttGroupList = pttGroupList;
    }
    public static void clearPttGroupList() {
        MessengerHelper.pttGroupList = null;
    }

    private static OkHttpClient pttClient;
    private static WebSocket socketPttListener;

    public static OkHttpClient getPttClient() {
        return pttClient;
    }

    public static void setPttClient(OkHttpClient pttClient) {
        MessengerHelper.pttClient = pttClient;
    }

    public static void clearPttClient() {
        MessengerHelper.pttClient = null;
    }

    public static WebSocket getSocketPttListener() {
        return socketPttListener;
    }

    public static void setSocketPttListener(WebSocket socketPttListener) {
        MessengerHelper.socketPttListener = socketPttListener;
    }

    public static void clearSocketPttListener() {
        MessengerHelper.socketPttListener = null;
    }

    private static Lifecycle.Event lifecycleEventPtt;

    public static Lifecycle.Event getLifecycleEventPtt() {
        return lifecycleEventPtt;
    }

    public static void setLifecycleEventPtt(Lifecycle.Event lifecycleEventPtt) {
        MessengerHelper.lifecycleEventPtt = lifecycleEventPtt;
    }

}
