package com.siggytech.utils.communication.model.async;

public interface ApiListener<T> {

    void onPreExecute();

    void onPostExecute(T result);

    void onCancelled(T result);
}
