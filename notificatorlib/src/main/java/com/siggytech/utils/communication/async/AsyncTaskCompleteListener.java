package com.siggytech.utils.communication.async;

/**
 * @author Kusses.
 */

public interface AsyncTaskCompleteListener<T> {
    void onTaskCompleted(T result);
}
