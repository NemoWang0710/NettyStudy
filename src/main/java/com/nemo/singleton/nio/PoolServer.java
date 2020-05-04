package com.nemo.singleton.nio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PoolServer {

    ExecutorService pool = Executors.newFixedThreadPool(50);

    private Selector selector;

    public static void main(String[] args) throws IOException {
        PoolServer server = new PoolServer();
        server.initServer(8000);
        server.listen();
    }

    public void initServer(int port) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        serverSocketChannel.configureBlocking(false);

        serverSocketChannel.socket().bind(new InetSocketAddress(port));

        this.selector = Selector.open();

        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器启动成功");
    }

    public void listen() throws IOException {
        while (true){
            selector.select();
            Iterator<SelectionKey> ite = this.selector.selectedKeys().iterator();
            while (ite.hasNext()){
                SelectionKey key = (SelectionKey)ite.next();
                ite.remove();
                if(key.isAcceptable()){
                    ServerSocketChannel server = (ServerSocketChannel)key.channel();
                    SocketChannel channel = server.accept();
                    channel.configureBlocking(false);
                    channel.register(this.selector,SelectionKey.OP_READ);
                }else if(key.isReadable()){
                    key.interestOps(key.interestOps() & (SelectionKey.OP_READ));
                    pool.execute(new ThreadHandleChannel(key));
                }
            }
        }
    }


    class ThreadHandleChannel extends Thread{

        private SelectionKey key;
        ThreadHandleChannel(SelectionKey key) {this.key = key;}
        @Override
        public void run() {
            SocketChannel channel = (SocketChannel)key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try {
                int size = 0;
                while ((size = channel.read(buffer)) > 0){
                    buffer.flip();
                    baos.write(buffer.array(),0,size);
                    buffer.clear();
                }
                baos.close();

                byte[] content = baos.toByteArray();
                ByteBuffer writeBuf = ByteBuffer.allocate(content.length);

                writeBuf.put(content);
                writeBuf.flip();
                channel.write(writeBuf);
                if(size == -1){
                    channel.close();
                }else{
                    key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                    key.selector().wakeup();
                }
            }catch (Exception e){
                System.out.println(e.getMessage());
            }


        }
    }
}
