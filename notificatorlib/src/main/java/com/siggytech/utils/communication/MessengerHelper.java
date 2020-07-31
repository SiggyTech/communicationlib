package com.siggytech.utils.communication;

public class MessengerHelper {

    private static MessengerHelper instance;

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
}
