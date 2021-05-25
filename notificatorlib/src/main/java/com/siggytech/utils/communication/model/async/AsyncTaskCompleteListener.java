package com.siggytech.utils.communication.model.async;

/**
 * @author Kusses.
 */

public interface AsyncTaskCompleteListener<T> {
    void onTaskCompleted(T result);
}
