package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import core.CloseUtils;
import core.Connector;

public class TCPClient extends Connector{
	
	public TCPClient(SocketChannel channel)throws IOException {
		setup(channel);
	}

	public static TCPClient startWith(ServerInfo info)throws IOException {
		SocketChannel socketChannel=SocketChannel.open();
		socketChannel.connect(new InetSocketAddress(info.getAddress(), info.getPort()));
		
		System.out.println("已发起客户端连接");
		System.out.println("客户端信息 ："+socketChannel.getLocalAddress().toString());
		System.out.println("服务端信息 :"+socketChannel.getRemoteAddress().toString());
		try {
			return new TCPClient(socketChannel);
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
	public void onChannelClose(SocketChannel socketChannel) {
		super.onChannelClose(socketChannel);
		System.out.println("连接已关闭，无法读取数据");
	}
}
