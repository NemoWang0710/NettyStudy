package com.nemo.singleton.bio;

import java.io.IOException;
import java.net.Socket;

public class Client {

    public static void main(String[] args) throws IOException {
        Socket s = new Socket("localhost",8888);
        s.getOutputStream().write("HelloServer".getBytes());

        System.out.println("write over , waiting for msg back ...");
        byte[] bytes = new byte[1024];
        int len = s.getInputStream().read(bytes);
        System.out.println(new String(bytes,0,len));
        s.close();

    }
}