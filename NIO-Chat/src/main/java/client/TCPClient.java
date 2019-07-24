package client;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import core.CloseUtils;
import core.Connector;
import foo.Foo;

public class TCPClient extends Connector{

	private final File cachePath;
	
	public TCPClient(SocketChannel channel,File cachePath)throws IOException {
		setup(channel);
		this.cachePath=cachePath;
	}

	public static TCPClient startWith(ServerInfo info,File cachePath)throws IOException {
		SocketChannel socketChannel=SocketChannel.open();
		socketChannel.connect(new InetSocketAddress(info.getAddress(), info.getPort()));
		
		System.out.println("已发起客户端连接");
		System.out.println("客户端信息 ："+socketChannel.getLocalAddress().toString());
		System.out.println("服务端信息 :"+socketChannel.getRemoteAddress().toString());
		try {
			return new TCPClient(socketChannel,cachePath);
		}catch (Exception e) {
			e.printStackTrace();
			CloseUtils.close(socketChannel); 
		}
		return null;
	}
	public void exit() {
		CloseUtils.close(this);
	}

	@Override
	protected File onCreateNewReceiveFile() {
		return Foo.createNewFile(cachePath);
	}

	@Override
	public void onChannelClose(SocketChannel socketChannel) {
		super.onChannelClose(socketChannel);
		System.out.println("连接已关闭，无法读取数据");
	}
}
