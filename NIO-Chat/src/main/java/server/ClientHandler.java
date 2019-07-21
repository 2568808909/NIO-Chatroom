package server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import core.CloseUtils;
import core.Connector;
import util.CloseUtil;

public class ClientHandler extends Connector{

	private ClientHandlerCallBack clientHandlerCallBack;
	private final String clientInfo;
	
	
	public ClientHandler(SocketChannel socketChannel,ClientHandlerCallBack clientHandlerCallBack) throws IOException{
		this.clientHandlerCallBack=clientHandlerCallBack;
		this.clientInfo=socketChannel.getRemoteAddress().toString();
		setup(socketChannel);
		System.out.println("新客户端连接: "+clientInfo);
	}	

	public void exit() {
		CloseUtils.close(this);
	}
	
	@Override
	public void onChannelClose(SocketChannel socketChannel) {
		super.onChannelClose(socketChannel);
		exitBySelf();
	}
	
	@Override
	protected void onReceiveNewMessage(String str) {
		super.onReceiveNewMessage(str);
		clientHandlerCallBack.onReadNotify(this, str);
	}
	

	public void exitBySelf() {
		exit();
		clientHandlerCallBack.onCloseNotify(this);
	}
}