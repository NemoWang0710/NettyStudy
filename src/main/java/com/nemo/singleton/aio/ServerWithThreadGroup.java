package com.nemo.singleton.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AIO的多线程模型
 */
public class ServerWithThreadGroup {
    public static void main(String[] args) throws IOException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        AsynchronousChannelGroup threadGroup = AsynchronousChannelGroup.withCachedThreadPool(executorService,1);
        final AsynchronousServerSocketChannel serverSocketChannel = AsynchronousServerSocketChannel.open(threadGroup)
                .bind(new InetSocketAddress(8888));

        serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
            @Override
            // completed 当客户端有连接上来并且连接成功之后
            public void completed(AsynchronousSocketChannel client, Object attachment) {
                serverSocketChannel.accept(null,this);
                try{
                    System.out.println(client.getRemoteAddress());
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    // 这里往通道上再下一个钩子，目的就是等到通道上有读写数据的时候，操作系统帮我来执行这段代码
                    client.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        // completed 当读写数据成功之后...
                        public void completed(Integer result, ByteBuffer attachment) {
                            attachment.flip();
                            System.out.println(new String(attachment.array(),0,result));
                            client.write(ByteBuffer.wrap("HelloClient".getBytes()));
                        }

                        @Override
                        // failed 当读写数据失败之后...
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            exc.printStackTrace();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {

            }
        });
    }
}
