package com.siggytech.utils.communication.presentation.register;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class SiggyRegisterAsync extends AsyncTask<String, Void, Object> {

    private Context context;
    private SiggyRegisterListener registerListener;
    private String deviceToken;

    public SiggyRegisterAsync(Context context, SiggyRegisterListener registerListener) {
        this.context = context;
        this.registerListener = registerListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        registerListener.onPreExecute();
    }

    @Override
    protected Object doInBackground(String... strings) {
        try {
            // Register the device for notifications
            deviceToken = Siggy.register(strings[0],strings[1],context);

            // Registration succeeded, log token to logcat
            Log.d("Siggy", "Siggy device token: " + deviceToken);

            // Provide token to onPostExecute()
            return deviceToken;
        }
        catch (Exception exc) {
            // Registration failed, provide exception to onPostExecute()
            return exc;
        }
    }

    @Override
    protected void onPostExecute(Object result) {
        registerListener.onPostExecute(result);
        clearInstances();
    }

    private void clearInstances(){
        context = null;
        registerListener = null;
        deviceToken = null;
    }
}
