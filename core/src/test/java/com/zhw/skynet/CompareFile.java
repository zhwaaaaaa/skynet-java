package com.zhw.skynet;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CompareFile {
    public static void main(String[] args) throws IOException {
        /*byte[] users = Files.readAllBytes(Paths.get("users.avro"));
        byte[] received = Files.readAllBytes(Paths.get("recev.avro"));
        byte[] s1 = new byte[65459];
        byte[] s2 = new byte[65520];
        byte[] s3 = new byte[39659];
        System.arraycopy(users, 0, s1, 0, s1.length);
        Files.write(Paths.get("u.1.avro"), s1);
        System.arraycopy(users, s1.length, s2, 0, s2.length);
        Files.write(Paths.get("u.2.avro"), s2);
        System.arraycopy(users, s1.length + s2.length, s3, 0, s3.length);
        Files.write(Paths.get("u.3.avro"), s3);

        System.arraycopy(received, 0, s1, 0, s1.length);
        Files.write(Paths.get("r.1.avro"), s1);
        System.arraycopy(received, s1.length, s2, 0, s2.length);
        Files.write(Paths.get("r.2.avro"), s2);
        System.arraycopy(received, s1.length + s2.length, s3, 0, s3.length);
        Files.write(Paths.get("r.3.avro"), s3);*/

        byte[] users = Files.readAllBytes(Paths.get("u.2.avro"));
        byte[] received = Files.readAllBytes(Paths.get("r.2.avro"));
        System.out.println(users.length);
        System.out.println(users.length == received.length);
        for (int i = 0; i < 1000; i++) {
            System.out.printf("%7d %4d %4d\n", i, users[i], received[i]);
        }
    }
}
