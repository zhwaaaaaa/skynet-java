package com.zhw.skynet.core;

import java.io.InputStream;
import java.io.OutputStream;

public interface BodyMapper<T> {

    String typeDesc();

    int writeTo(Object data, OutputStream out) throws EncodeException;

    T read(InputStream in) throws DecodeException;
}
