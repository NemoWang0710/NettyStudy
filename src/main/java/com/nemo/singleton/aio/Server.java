package com.nemo.singleton.aio;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * AIO的单线程模型
 *
 */
public class Server {
    public static void main(String[] args) throws IOException, InterruptedException {
        final AsynchronousServerSocketChannel serverSocketChannel = AsynchronousServerSocketChannel.open()
                .bind(new InetSocketAddress(8888));

        /**
         * 代码思路:主线程写好一段代码将这段代码钩到操作系统内核里边，等到有连接进来的时候，你帮我执行这段代码。
         *          等到连接好了之后，我又下一个钩，将钩下到通道上边，等到通道有读写的时候 帮我执行第二个钩子
         */
        // 这里用到了 观察者设计模式
        serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
            @Override
            // completed 已经连接上来了。
            public void completed(final AsynchronousSocketChannel client, Object attachment) {
                serverSocketChannel.accept(null,this);
                try {
                    System.out.println(client.getRemoteAddress());
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    client.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(Integer result, ByteBuffer attachment) {
                            attachment.flip();
                            System.out.println(new String(attachment.array(),0,result));
                            client.write(ByteBuffer.wrap("HelloClient".getBytes()));
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            exc.printStackTrace();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            // failed 是失败了
            public void failed(Throwable exc, Object attachment) {
                exc.printStackTrace();
            }
        });

        while (true){
            Thread.sleep(1000);
        }
    }
}
