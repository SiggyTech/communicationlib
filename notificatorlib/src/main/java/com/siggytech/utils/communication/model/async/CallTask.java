package com.siggytech.utils.communication.model.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.siggytech.utils.communication.model.MessageModel;

import static com.siggytech.utils.communication.util.FileUtil.fileToBase64;

/**
 * @author Kusses.
 */

public class CallTask extends AsyncTask<Object, Void,TaskMessage> {
    private static final String TAG = CallTask.class.getSimpleName();

    private Context context;
    private AsyncTaskCompleteListener<TaskMessage> listener;
    private ProgressDialog progressDialog;

    public CallTask(Context context, AsyncTaskCompleteListener<TaskMessage> listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
        progressDialog=new ProgressDialog(context);
        progressDialog.setMessage("Wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }


    @Override
    protected TaskMessage doInBackground(Object... objects) {
        TaskMessage message = new TaskMessage();
        try {
            MessageModel messageModel = (MessageModel) objects[0];
            messageModel.setMessage(fileToBase64(messageModel.getFile()));
            message.setMessage("OK");
            message.setMessageModel(messageModel);
            return message;
        }catch (Exception e){
            message.setError(true);
            message.setMessage("Error: "+e.getMessage());
            message.setException(e);
            return message;
        }
    }

    @Override
    protected void onPostExecute(TaskMessage message) {
        super.onPostExecute(message);
        message.getMessageModel().setFile(null);
        listener.onTaskCompleted(message);
        progressDialog.dismiss();
    }
}
