package com.zhw.skynet.core.protocol;

public final class ResponseMessage extends Message {
    private int responseCode;

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
}
