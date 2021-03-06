package com.nemo.singleton.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

public class Client {

    public static void main(String[] args) {

    }

    private void clientStart(){
        EventLoopGroup workers = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(workers)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel ch) throws Exception {
                        System.out.println("channel initialized:");
                        ch.pipeline().addLast(new ClientHandler());
                    }
                });
        try{
            System.out.println("start ti connect ...");
            ChannelFuture f = b.connect("127.0.0.1",8888).sync();
            f.channel().closeFuture().sync();
        }catch (InterruptedException e){
            e.printStackTrace();
        }finally {
            workers.shutdownGracefully();
        }
    }

    class ClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channel is activited.");
            final ChannelFuture f = ctx.writeAndFlush(Unpooled.copiedBuffer("HelloNetty".getBytes()));
            f.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    System.out.println("msg send");
                }
            });
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            try{
                ByteBuf buf = (ByteBuf)msg;
                System.out.println(buf.toString());
            }finally {
                ReferenceCountUtil.release(msg);
            }
        }
    }
}
