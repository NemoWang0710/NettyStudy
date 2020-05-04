package com.nemo.singleton.bio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Blocking Io 阻塞式IO
 */
public class Server {

    /**
     * throws IOException 这个处理是不对的。
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        ServerSocket ss = new ServerSocket();
        ss.bind(new InetSocketAddress("localhost",8888));
        /**
         * 上边的这两行代码, 等于说在服务器端创建了一个大的面板，能够让客户端连接上来
         */
        while(true){
            Socket s = ss.accept();  // 这个方法是一个阻塞方法。一直阻塞到有客户端连接上来
            /**
             * s 这个s就是和客户端连接的一个通道
             */


            /**
             * 下边为什么要单独启动一个线程来处理呢 ? 为什么不直接处理呢
             * 原因:  如果处理的时间比较长，while就不会在执行了，那么accept就执行不了，那就表示下一个连接就连接不上了
             */
            new Thread( () -> {
                 handle(s);
            }).start();
        }
    }

    static void handle(Socket s) {
        try{
            byte[] bytes = new byte[1024];

            /**
             * block IO 客户端和服务器端建立了通道之后，这个通道是单向的，并不是双向的
             *  1,首先要从 InputStream中读数据
             *  2,然后再通过outPutStream写数据
             *  就是把数据从client端读过来，然后再给他写回去
             */
            int len = s.getInputStream().read(bytes);

            s.getOutputStream().write(bytes,0,len);
            s.getOutputStream().flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
