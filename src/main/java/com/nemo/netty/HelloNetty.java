package com.nemo.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;

public class HelloNetty {

    public static void main(String[] args) {
        new NettyServer(8888).serverStart();
    }

    static class NettyServer{
        int port = 8888;
        public NettyServer(int port){
            this.port = port;
        }

        public void serverStart(){
            // Nio的 selector
            EventLoopGroup booGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            ServerBootstrap b = new ServerBootstrap();

            b.group(booGroup,workerGroup)
                    // 建立完通道是什么类型的
                    .channel(NioServerSocketChannel.class)
                    // 当我们每一个客户端连接上来之后，给一个监听器。在通道上
                    // 一旦这个通道初始化了，我就在这个通道上添加对这个通道的处理器
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 在通道上建立 监听器
                            ch.pipeline().addLast(new Handler());
                        }
                    });
            try{
                ChannelFuture f = b.bind(port).sync();
                f.channel().closeFuture().sync();
            }catch (InterruptedException e){
                e.printStackTrace();
            }finally {
                workerGroup.shutdownGracefully();
                booGroup.shutdownGracefully();
            }
        }
    }

    static class Handler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("Server : channel read");
            ByteBuf buf = (ByteBuf) msg;
            System.out.println(buf.toString(CharsetUtil.UTF_8));
            ctx.writeAndFlush(msg);
            ctx.close();
        }

        // 如果出现异常
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
