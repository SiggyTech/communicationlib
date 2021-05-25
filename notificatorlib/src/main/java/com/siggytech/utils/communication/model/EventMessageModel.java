package com.siggytech.utils.communication.model;

import androidx.annotation.NonNull;

import com.siggytech.utils.communication.util.Utils;

public class EventMessageModel {
    private String event;
    private DataMessageModel data;
    private long timeMark;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public DataMessageModel getData() {
        return data;
    }

    public void setData(DataMessageModel data) {
        this.data = data;
    }

    public long getTimeMark() {
        return timeMark;
    }

    public void setTimeMark(long timeMark) {
        this.timeMark = timeMark;
    }

    @NonNull
    @Override
    public String toString() {
        return Utils.getGson().toJson(this);
    }
}
