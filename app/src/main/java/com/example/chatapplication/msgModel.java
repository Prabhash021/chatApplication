package com.example.chatapplication;

public class msgModel {
    private String userId;
    private String msg;
    private String time;

    public msgModel(String userId, String msg, String time) {
        this.userId = userId;
        this.msg = msg;
        this.time = time;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
