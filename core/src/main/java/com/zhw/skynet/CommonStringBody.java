package com.zhw.skynet;

import com.zhw.skynet.common.TypeReference;
import com.zhw.skynet.core.AbstractBody;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public class CommonStringBody extends AbstractBody {
    public static final Charset charset = Charset.forName("utf-8");
    private String sequence;
    private int times;

    public CommonStringBody(String sequence, int times) {
        this.sequence = sequence;
        this.times = times;
    }

    public CommonStringBody(String sequence) {
        this.sequence = sequence;
    }

    @Override
    public int writeTo(ByteBuf buf) {
        for (int i = 0; i < times; i++) {
            buf.writeCharSequence(sequence, charset);
        }
        return times * sequence.length();
    }

    @Override
    public <T> T read(TypeReference<T> type) {
        return null;
    }

}
