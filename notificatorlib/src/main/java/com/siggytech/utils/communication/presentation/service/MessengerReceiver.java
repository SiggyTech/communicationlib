package com.siggytech.utils.communication.presentation.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.siggytech.utils.communication.presentation.MessengerHelper;
import com.siggytech.utils.communication.util.Utils;

import static com.siggytech.utils.communication.presentation.service.MyFirebaseMessagingService.NEW_MESSAGE;


public class MessengerReceiver extends BroadcastReceiver {

    public static final String MESSAGE_CHAT = "messageChat";
    public static final String MESSAGE_CHAT_ID = "messageChatID";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.hasExtra(MESSAGE_CHAT)){
            if(MessengerHelper.getChatListView()!=null)
                MessengerHelper.getChatListView().onMessageReceiver(intent.getStringExtra(MESSAGE_CHAT),intent.getLongExtra(MESSAGE_CHAT_ID,0L));
            else  Utils.traces("MessengerReceiver onReceive HAS MESSAGE_CHAT BUT MessengerHelper.getChatListView() IS NULL");
        }else if(intent.hasExtra(WebSocketPTTService.MESSAGE_PTT)){
            try {
               if(MessengerHelper.getPttButton()!=null)
                    MessengerHelper.getPttButton().PlayShortAudioFileViaAudioTrack(intent.getByteArrayExtra(WebSocketPTTService.MESSAGE_PTT));
               else Utils.traces("MessengerReceiver onReceive HAS MESSAGE_PTT BUT MessengerHelper.getPttButton IS NULL");
            } catch (Exception e) {
                Utils.traces("MessengerReceiver onReceive. "+Utils.exceptionToString(e));
            }
        }else if(intent.hasExtra(NEW_MESSAGE)){
            Utils.traces("MessengerReceiver NEW_MESSAGE");
            if(MessengerHelper.getChatListView()!=null)
                MessengerHelper.getChatListView().setGroupView(intent.getIntExtra(NEW_MESSAGE,0),10);
            else  Utils.traces("MessengerReceiver onReceive new message chatListView is null");
        }

    }
}