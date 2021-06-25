package com.siggytech.utils.communication.model.async;

import android.os.AsyncTask;

public class ApiAsyncTask extends AsyncTask<Object, Void, TaskMessage> {

    ApiListener<TaskMessage> listener;

    public ApiAsyncTask(ApiListener<TaskMessage> listener){
        this.listener = listener;
    }

    @Override
    protected TaskMessage doInBackground(Object... objects) {
        try {
            ApiManager manager = new ApiManager();
            ApiEnum apiEnum = (ApiEnum) objects[0];
            switch (apiEnum){
                case GET_GROUPS:
                    return ApiEnum.GET_GROUPS.callRest(manager,objects[1],objects[2]);
                case GET_CHAT_QUEUE:
                    return ApiEnum.GET_CHAT_QUEUE.callRest(manager,objects[1]);
                case GET_PTT_GROUPS:
                    return ApiEnum.GET_PTT_GROUPS.callRest(manager,objects[1]);
                case REQUEST_TOKEN:
                    return ApiEnum.REQUEST_TOKEN.callRest(manager,objects[1],objects[2]);
                case LEAVE_TOKEN:
                    return ApiEnum.LEAVE_TOKEN.callRest(manager,objects[1],objects[2]);
                default:
                    TaskMessage taskMessage = new TaskMessage();
                    taskMessage.setError(true);
                    taskMessage.setMessage("Api Method Not Found.");
                    return taskMessage;
            }
        }catch (Exception e){
            TaskMessage taskMessage = new TaskMessage();
            taskMessage.setError(true);
            taskMessage.setMessage("Error: "+e.getMessage());
            taskMessage.setException(e);
            return taskMessage;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        listener.onPreExecute();
    }

    @Override
    protected void onPostExecute(TaskMessage taskMessage) {
        super.onPostExecute(taskMessage);
        listener.onPostExecute(taskMessage);
    }


    @Override
    protected void onCancelled(TaskMessage taskMessage) {
        super.onCancelled(taskMessage);
        listener.onCancelled(taskMessage);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
