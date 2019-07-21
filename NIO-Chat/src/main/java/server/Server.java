package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import core.IoContext;
import impl.IoSelectorProvider;
import util.TCPConstants;

public class Server {
	public static void main(String[] args)throws IOException {
		System.out.println("Server start.");
		IoContext.setup().ioProvider(new IoSelectorProvider()).start();
		UDPServerProvider.start(TCPConstants.PORT_SERVER);
		TCPServerProvider tcpServerProvider=new TCPServerProvider(TCPConstants.PORT_SERVER);
		tcpServerProvider.start();
		BufferedReader reader=new BufferedReader(new InputStreamReader(System.in, "utf-8"));
		String str;
		do {
			str=reader.readLine();
			tcpServerProvider.boardcast(str);
		}while(!"00bye00".equals(str));
		tcpServerProvider.stop();
		UDPServerProvider.stop();
		System.out.println("Server end.");
		IoContext.close();
	}
	
	
}
