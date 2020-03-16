package com.zhw.skynet.core;

public enum MsgType {
    MT_CONSUMER_SH,
    MT_PROVIDER_SH,
    MT_SH_RESP,
    MT_HEARTBEAT_REQ,
    MT_HEARTBEAT_RESP,
    MT_REQUEST,
    MT_RESPONSE;

    public static MsgType from(int val) {
        if (val > MsgType.values().length) {
            return null;
        }
        return MsgType.values()[val - 1];
    }

    public byte val() {
        return (byte) (this.ordinal() + 1);
    }
}