package com.siggytech.utils.communication.model.async;

import com.siggytech.utils.communication.model.GroupRequestModel;
import com.siggytech.utils.communication.model.PairRegisterModel;
import com.siggytech.utils.communication.model.QueueRequestModel;

public class ApiManager {
    private final ApiService service;
    private TaskMessage taskMessage = null;

    public ApiManager(){
        service = new ApiService();
    }

    public TaskMessage getGroups(String imei, String apiKey) {
        Thread t = new Thread(() -> taskMessage = service.getGroups(imei, apiKey));

        t.start();
        try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }

        return taskMessage;

    }

    public TaskMessage setFirebaseToken(PairRegisterModel model) {
        Thread t = new Thread(() -> taskMessage = service.setFirebaseToken(model));

        t.start();
        try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }

        return taskMessage;
    }

    public TaskMessage getChatQueue(QueueRequestModel model) {
        Thread t = new Thread(() -> taskMessage = service.getChatQueue(model));

        t.start();
        try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }

        return taskMessage;
    }

    public TaskMessage getPttGroups(GroupRequestModel model) {
        Thread t = new Thread(() -> taskMessage = service.getPttGroups(model));

        t.start();
        try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }

        return taskMessage;
    }
}
