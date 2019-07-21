package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import core.IoContext;
import impl.IoSelectorProvider;

public class Client {
	
	public final static int LISTEN_PORT=30000;
	
	public static void main(String[] args) throws IOException, InterruptedException{
		IoContext.setup().ioProvider(new IoSelectorProvider()).start();
		ServerInfo info=ClientSearcher.searchServer(10000);
		System.out.println("Server :"+info);
		if(info!=null) {
			TCPClient client=null;
			try {
				client=TCPClient.startWith(info);
				if(client==null) {
					return;
				}
				write(client);
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				if(client!=null) {
					client.exit();
				}
			}
		}
		IoContext.close();
	}

	private static void write(TCPClient client)throws IOException {
		InputStream inputStream=System.in;
		BufferedReader input=new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
		do {
			String str=input.readLine();
			client.send(str);
			if("00bye00".equalsIgnoreCase(str)) {
				break;
			}
		}while(true);
		
	}
	
	
}
