package com.siggytech.utils.communication.model.async;

import com.siggytech.utils.communication.model.GroupRequestModel;
import com.siggytech.utils.communication.model.QueueRequestModel;

public enum ApiEnum {
    GET_GROUPS,
    GET_CHAT_QUEUE,
    GET_PTT_GROUPS,
    REQUEST_TOKEN,
    LEAVE_TOKEN;

    TaskMessage callRest(Object... objects) {
        ApiManager manager = (ApiManager) objects[0];
        switch (this) {
            case GET_GROUPS:
                return manager.getGroups((String) objects[1], (String) objects[2]);
            case GET_CHAT_QUEUE:
                return manager.getChatQueue((QueueRequestModel)objects[1]);
            case GET_PTT_GROUPS:
                return manager.getPttGroups((GroupRequestModel) objects[1]);
            case REQUEST_TOKEN:
                return manager.requestToken((long)objects[1],(String)objects[2]);
            case LEAVE_TOKEN:
                return manager.leaveToken((long)objects[1],(String)objects[2]);
            default:
                throw new AssertionError("Unknown operations " + this);
        }
    }
}
