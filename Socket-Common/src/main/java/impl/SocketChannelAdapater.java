package impl;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import core.CloseUtils;
import core.IoArgs;
import core.IoArgs.IoArgsEventListener;
import core.IoProvider;
import core.Receiver;
import core.Sender;

public class SocketChannelAdapater implements Receiver,Sender,Cloneable{
	
	private final AtomicBoolean isClose=new AtomicBoolean(false);
	private final SocketChannel socketChannel;
	private final IoProvider ioProvider;
	private final OnChannelStatusChangedListener listener;
	
	private IoArgs.IoArgsEventListener inputEventListener;
	private IoArgs.IoArgsEventListener outputEventListener;
	
	private IoArgs receiveArgsTemp;
	
	@Override
	public void setReceiveListener(IoArgsEventListener listener) {
		inputEventListener=listener;
	}

	@Override
	public boolean receiveAsync(IoArgs args) throws IOException {
		if(isClose.get()) {
			System.out.println("Current channel is close");
			throw new IOException();
		}
		receiveArgsTemp=args;
		return ioProvider.registerInput(socketChannel, inputCallback);
	}
	
	private IoProvider.HandleInputCallback inputCallback=new IoProvider.HandleInputCallback() {
		@Override
		protected void canProviderInput() {
			if(isClose.get()) {
				return;
			}
			IoArgs args=receiveArgsTemp;
			IoArgs.IoArgsEventListener receiveIoEventListener=SocketChannelAdapater.this.inputEventListener;
			//回调开始操作
			receiveIoEventListener.onStart(args);
			//具体的读取操作
			try {
				if(args.readForm(socketChannel)>0) {
					//完成读取，进行回调
					receiveIoEventListener.onCompleted(args);
				}else {
					throw new IOException("Cannot read any data!");
				}
			}catch (Exception e) {
				e.printStackTrace();
				CloseUtils.close(SocketChannelAdapater.this);
			}
		}
	};
	
	private IoProvider.HandleOutputCallback outputCallback=new IoProvider.HandleOutputCallback() {
		
		@Override
		protected void canProviderOutput() {
			if(isClose.get()) {
				return;
			}
			IoArgs args=getAttach();
			IoArgs.IoArgsEventListener listener=outputEventListener;
			
			listener.onStart(args);
			
			try {
				if(args.writeTo(socketChannel)>0) {
					//完成读取，进行回调
					listener.onCompleted(args);
				}else {
					throw new IOException("Cannot write any data!");
				}
			}catch (Exception e) {
				e.printStackTrace();
				CloseUtils.close(SocketChannelAdapater.this);
			}
		}
	};
	
	public SocketChannelAdapater(
			SocketChannel channel,
			IoProvider ioProvider,
			OnChannelStatusChangedListener listener) throws IOException{
		this.socketChannel=channel;
		this.ioProvider=ioProvider;
		this.listener=listener;
		channel.configureBlocking(false);
	}
	
	@Override
	public void close() throws IOException {
		//CAS,比较主存中的isClose值是否为false，是则更新为true，更新成功返回true
		if(isClose.compareAndSet(false, true)) {
			//解除注册与回调
			ioProvider.unRegisterInput(socketChannel);
			ioProvider.unRegisterOutput(socketChannel);
			//关闭当前channel
			CloseUtils.close(socketChannel);
		}
	}

	@Override
	public boolean sendAsync(IoArgs args, IoArgsEventListener listener) throws IOException {
		if(isClose.get()) {
			System.out.println("Current channel is close");
			throw new IOException();
		}	
		outputEventListener=listener;
		//将当前发送数据附加到回调当中
		outputCallback.setAttach(args);
		
		return ioProvider.registerOutput(socketChannel, outputCallback);
	}

	public interface OnChannelStatusChangedListener{
		void onChannelClose(SocketChannel socketChannel);
	}

	
}
