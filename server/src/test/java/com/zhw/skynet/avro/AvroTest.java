package com.zhw.skynet.avro;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.*;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class AvroTest {

    public static void main(String[] args) throws IOException {
        ReflectDatumWriter<User> datumWriter = new ReflectDatumWriter<User>(User.class);
        Schema schema = ReflectData.get().getSchema(User.class);
        datumWriter.setSchema(schema);
        FileOutputStream out = new FileOutputStream("user.avro");
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        datumWriter.write(new User("cccccccccc", Integer.MAX_VALUE, true), encoder);
        encoder.flush();
        out.close();
        File file = new File("user.avro");
        System.out.println(file.length());
        System.out.println(schema.toString(false));
        ReflectDatumReader<User> reflectDatumReader = new ReflectDatumReader<User>(schema);
        User reuse = new User();
        User read = reflectDatumReader.read(null,
                DecoderFactory.get().binaryDecoder(new FileInputStream(file), null));
        System.out.println(read);
        System.out.println(reuse == read);


    }
}
