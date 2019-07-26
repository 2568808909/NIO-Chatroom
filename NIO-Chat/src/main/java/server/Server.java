package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import core.IoContext;
import foo.Foo;
import impl.IoSelectorProvider;
import util.TCPConstants;

public class Server {
	public static void main(String[] args)throws IOException {
		File cachePath= Foo.getCacheDir("server");
		System.out.println("Server start.");
		IoContext.setup().ioProvider(new IoSelectorProvider()).start();
		UDPServerProvider.start(TCPConstants.PORT_SERVER);
		TCPServerProvider tcpServerProvider=new TCPServerProvider(TCPConstants.PORT_SERVER,cachePath);
		tcpServerProvider.start();
		BufferedReader reader=new BufferedReader(new InputStreamReader(System.in, "utf-8"));
		String str;
		do {
			str=reader.readLine();
			if("00bye00".equalsIgnoreCase(str)){
				break;
			}
			//发送字符串
			tcpServerProvider.boardcast(str);
		}while(true);
		tcpServerProvider.stop();
		UDPServerProvider.stop();
		System.out.println("Server end.");
		IoContext.close();
	}
	
	
}
