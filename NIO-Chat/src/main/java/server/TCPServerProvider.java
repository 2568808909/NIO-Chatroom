package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TCPServerProvider implements ClientHandlerCallBack{
	
	private static Provider PROVIDER=null;
	private int port;
	private List<ClientHandler> clientHandlers=new ArrayList<>();
	private Selector selector;
	private ServerSocketChannel server;
	
	public TCPServerProvider(int port) {
		this.port=port;
	}
	
	public void stop() {
		if(PROVIDER!=null) {
			PROVIDER.exit();
			PROVIDER=null;
			System.out.println("TCPServer end.");
		}
		for (ClientHandler clientHandler : clientHandlers) {
			clientHandler.exit();
		}
	}
	
	public boolean start() {
		System.out.println("TCPServer start.");
		stop();
		try {
			//开启选择器
			selector=Selector.open();
			//开启通道
			server=ServerSocketChannel.open();
			//设置为非阻塞
			server.configureBlocking(false);
			//绑定本地端口
			server.socket().bind(new InetSocketAddress(port));
			//注册客户端连接到客户端的监听
			server.register(selector, SelectionKey.OP_ACCEPT);
			Provider provider=new Provider();
			provider.start();
			PROVIDER=provider;
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public class Provider extends Thread{
		private boolean done;
		
		public Provider()throws IOException {
			done=false;
		}
		
		public void run() {
			do {
				try {
					if(selector.select()==0) {
						if(done) {
							break;
						}
					}
					Iterator<SelectionKey> iterator=selector.selectedKeys().iterator();
					while(iterator.hasNext()) {
						if(done) {
							break;
						}
						SelectionKey key=iterator.next();
						if(key.isAcceptable()) {
							ServerSocketChannel serverSocketChannel=(ServerSocketChannel)key.channel();
							SocketChannel socketChannel=serverSocketChannel.accept();
							System.out.println("accepted.");
							try {
								ClientHandler clientHandler=new ClientHandler(socketChannel,TCPServerProvider.this);
								clientHandlers.add(clientHandler);
							}catch (Exception e) {
								e.printStackTrace();
								System.out.println("客户端连接异常"+e.getMessage());
							}
						}
					}
				}catch (Exception e) {
					continue;
				}
			}while(!done);
		}
		
		public void close() {
			if(server!=null) {
				try {
					server.close();
					server=null;
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		public void exit() {
			close();
			done=true;
		}
	}

	public void boardcast(String str) {
		for (ClientHandler clientHandler : clientHandlers) {
			clientHandler.send(str);;
		}
	}

	@Override
	public void onCloseNotify(ClientHandler handler) {
		clientHandlers.remove(handler);
		
	}

	@Override
	public void onReadNotify(ClientHandler handler,String msg) {
		for (ClientHandler clientHandler : clientHandlers) {
			if(clientHandler.equals(handler)) {
				continue;
			}
			clientHandler.send(msg);
		}
	}
	
	
}
