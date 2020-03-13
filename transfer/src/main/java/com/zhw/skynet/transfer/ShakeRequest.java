package com.zhw.skynet.transfer;

import java.util.List;

public class ShakeRequest {
    private int code;
    private int len;
    private List<ServiceDesc> desc;

    public ShakeRequest(int code, int len, List<ServiceDesc> desc) {
        this.code = code;
        this.len = len;
        this.desc = desc;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public List<ServiceDesc> getDesc() {
        return desc;
    }

    public void setDesc(List<ServiceDesc> desc) {
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
