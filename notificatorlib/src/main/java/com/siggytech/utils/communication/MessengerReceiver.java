package com.siggytech.utils.communication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class MessengerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Utils.traces("MessengerReceiver onReceive");
        if(intent.hasExtra(WebSocketChatService.MESSAGE_CHAT)){
            Utils.traces("MessengerReceiver onReceive HAS MESSAGE_CHAT");
            if(MessengerHelper.getChatListView()!=null)
                MessengerHelper.getChatListView().onMessageReceiver(intent.getStringExtra(WebSocketChatService.MESSAGE_CHAT));
            else  Utils.traces("MessengerReceiver onReceive HAS MESSAGE_CHAT BUT MessengerHelper.getChatListView() IS NULL");
        }else if(intent.hasExtra(WebSocketPTTService.MESSAGE_PTT)){
            try {
                Utils.traces("MessengerReceiver onReceive HAS MESSAGE_PTT");
               if(MessengerHelper.getPttButton()!=null)
                    MessengerHelper.getPttButton().PlayShortAudioFileViaAudioTrack(intent.getByteArrayExtra(WebSocketPTTService.MESSAGE_PTT));
               else Utils.traces("MessengerReceiver onReceive HAS MESSAGE_PTT BUT MessengerHelper.getPttButton IS NULL");
            } catch (Exception e) {
                Utils.traces("MessengerReceiver onReceive. "+Utils.exceptionToString(e));
            }
        }

    }
}