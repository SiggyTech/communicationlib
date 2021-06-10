package com.siggytech.utils.communication.presentation.common;

import com.siggytech.utils.communication.model.async.TaskMessage;

public interface CallBack {
    void onPreExecute();
    void onReady(TaskMessage result);
}
