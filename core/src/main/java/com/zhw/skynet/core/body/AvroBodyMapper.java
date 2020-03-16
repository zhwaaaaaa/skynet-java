package com.zhw.skynet.core.body;

import com.zhw.skynet.core.BodyMapper;
import com.zhw.skynet.core.DecodeException;
import com.zhw.skynet.core.protocol.CodecException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.avro.reflect.ReflectDatumWriter;

import java.io.IOException;
import java.lang.reflect.Type;

public class AvroBodyMapper<T> implements BodyMapper<T> {
    private ReflectDatumWriter<T> writer;
    private ReflectDatumReader<T> reader;
    private Schema schema;

    public AvroBodyMapper(Class<T> type) {
        this((Type) type);
    }

    public AvroBodyMapper(Type type) {
        schema = ReflectData.get().getSchema(type);
        writer = new ReflectDatumWriter<>(schema);
        reader = new ReflectDatumReader<>(schema);
    }


    @Override
    public String typeDesc() {
        return schema.toString(false);
    }

    @Override
    public int writeTo(T data, ByteBuf out, int writeOpts) throws CodecException {
        try {
            ByteBufOutputStream os = new ByteBufOutputStream(out);
            writer.write(data, EncoderFactory.get().directBinaryEncoder(os, null));
            return os.writtenBytes();
        } catch (IOException e) {
            throw new CodecException(e);
        }
    }

    @Override
    public T read(ByteBuf in, int readOpts) throws DecodeException {
        try {
            BinaryDecoder decoder = DecoderFactory.get()
                    .directBinaryDecoder(new ByteBufInputStream(in), null);
            return reader.read(null, decoder);
        } catch (IOException e) {
            throw new DecodeException(e);
        }
    }
}
