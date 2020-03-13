package com.zhw.skynet.transfer;

import com.zhw.skynet.common.Constants;
import com.zhw.skynet.common.ServiceCount;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class ShakeHandsUtil {

    public static ShakeRequest decodeShakeHandsReq(ByteBuf buf) {
        if (buf.readableBytes() < 7) {
            return null;
        }
        int code = buf.readUnsignedByte();
        int shakePkgLen = buf.readIntLE();
        int serviceSize = buf.readUnsignedShortLE();
        ShakeRequest req = new ShakeRequest(code, shakePkgLen, new ArrayList<>(serviceSize));

        int decodeLen = 2;

        for (int i = 0; i < serviceSize; i++) {
            int servLen = buf.readByte() & 0xFF;
            if (servLen > Constants.MAX_SERVICE_LEN) {
                return null;
            }
            decodeLen += servLen + 1;
            if (decodeLen > shakePkgLen) {
                return null;
            }
            String service = buf.readCharSequence(servLen, Constants.UTF8).toString();

            int paramLen = buf.readIntLE();
            decodeLen += paramLen + 4;
            if (decodeLen > shakePkgLen) {
                return null;
            }
            String param = buf.readCharSequence(paramLen, Constants.UTF8).toString();

            int resultLen = buf.readIntLE();
            decodeLen += paramLen + 4;
            if (decodeLen > shakePkgLen) {
                return null;
            }
            String result = buf.readCharSequence(resultLen, Constants.UTF8).toString();
            ServiceDesc desc = new ServiceDesc(service, 0, param, result);
            req.getDesc().add(desc);
        }

        return req;
    }


    public static ByteBuf encodeServiceCounts(List<ServiceCount> counts, ByteBuf buf) {
        buf.writeByte(Constants.MSG_TYPE_SHAKE_RESP);
        int lenIndex = buf.writerIndex();
        buf.writeZero(4);
        int len = 2;
        for (ServiceCount count : counts) {
            buf.writeByte(count.getServiceName().length());
            buf.writeCharSequence(count.getServiceName(), Constants.UTF8);
            buf.writeByte(count.getCount());
            len += count.getServiceName().length() + 2;
        }
        buf.setIntLE(lenIndex, len);
        return buf;
    }
}
