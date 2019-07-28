package core;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.UUID;

import box.BytesReceivePacket;
import box.FileReceivePacket;
import box.StringReceivePacket;
import box.StringSendPacket;
import impl.SocketChannelAdapater;
import impl.async.AsyncReceiveDispatcher;
import impl.async.AsyncSendDispatcher;

/**
 * 处理连接,基于SocketChannel进行连接，但是收发数据另外封装了Receiver和Sender，
 * 而不是通过SocketChannel，以模拟一个真实的连接
 *
 * @author Administrator
 */
public abstract class Connector implements Closeable, SocketChannelAdapater.OnChannelStatusChangedListener {

    protected UUID key = UUID.randomUUID();
    private SocketChannel channel;
    private Receiver receiver;
    private Sender sender;
    private SendDispatcher sendDispatcher;
    private ReceiveDispatcher receiveDispatcher;
//	private IoArgs.IoArgsEventListener echoReceiverListener=new IoArgs.IoArgsEventListener() {
//		@Override
//		public void onStart(IoArgs args) {
//			
//		}
//		@Override
//		public void onCompleted(IoArgs args) {
//			//打印读取到得数据
//			onReceiveNewMessage(args.bufferString());
//			//读取下一条数据
//			readNextMessage();
//		}
//	};

    private ReceiveDispatcher.ReceivePacketCallback receivePacketCallback = new ReceiveDispatcher.ReceivePacketCallback() {

        @Override
        public void onReceivePacketCompleted(ReceivePacket packet) {
            onReceivedNewPacket(packet);
        }

        @Override
        public ReceivePacket<?, ?> onArrivedNewPacket(byte type,long length) {
            switch (type){
                case Packet.TYPE_MEMORY_BYTES:
                    return new BytesReceivePacket(length);
                case Packet.TYPE_MEMORY_STRING:
                    return new StringReceivePacket(length);
                case Packet.TYPE_STREAM_FILE:
                    return new FileReceivePacket(length,onCreateNewReceiveFile());
                case Packet.TYPE_STREAM_DIRECT:
                    return new BytesReceivePacket(length);
                default:
                    throw new UnsupportedOperationException("UnSupport type :"+type);
            }
        }
    };

    protected abstract File onCreateNewReceiveFile();

    protected void onReceivedNewPacket(ReceivePacket packet) {
        System.out.println(key.toString() + ": receive New packet " + packet.type() + " " + packet.length());
    }


    public void setup(SocketChannel socketChannel) throws IOException {
        this.channel = socketChannel;
        IoContext context = IoContext.get();
        SocketChannelAdapater adapater = new SocketChannelAdapater(socketChannel, context.getIoProvider(), this);

        receiver = adapater;
        sender = adapater;

        sendDispatcher = new AsyncSendDispatcher(sender);
        receiveDispatcher = new AsyncReceiveDispatcher(receiver, receivePacketCallback);

        //启动接收
        receiveDispatcher.start();
//		readNextMessage();
    }

//	private void readNextMessage() {
//		if(receiver!=null) {
//			try {
//				receiver.receiveAsync(echoReceiverListener);
//			}catch (Exception e) {
//				e.printStackTrace();
//				System.out.println("接收数据异常："+e.getMessage());
//			}
//		}
//	}

    public void send(String msg) {
        SendPacket sendPacket = new StringSendPacket(msg);
        sendDispatcher.send(sendPacket);
    }

    public void send(SendPacket packet) {
        sendDispatcher.send(packet);
    }

    @Override
    public void close() throws IOException {
        receiveDispatcher.close();
        sendDispatcher.close();
        sender.close();
        receiver.close();
        channel.close();
    }

    @Override
    public void onChannelClose(SocketChannel socketChannel) {

    }

}
