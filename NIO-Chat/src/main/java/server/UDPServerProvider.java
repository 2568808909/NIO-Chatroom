package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.UUID;

import util.ByteUtil;
import util.UDPConstants;

public class UDPServerProvider {
	
	private static Provider PROVICER;
	
	public static void start(int port) {
		stop();
		String sn=UUID.randomUUID().toString();
		Provider provider=new Provider(sn,port);
		provider.start();
		PROVICER=provider;
		System.out.println("UDPServer start.");
	}
	
	public static void stop() {
		if(PROVICER!=null) {
			PROVICER.exit();
			PROVICER=null;
			System.out.println("UDPServer end.");
		}
	}
	
	public static class Provider extends Thread{
		private String sn;
		private DatagramSocket socket;
		private int port;
		private boolean done;
		private final byte[] buffer=new byte[128];
		
		public Provider(String sn,int port) {
			done=false;
			this.port=port;
			this.sn=sn;
		}

		@Override
		public void run() {
			try {
				socket=new DatagramSocket(UDPConstants.PORT_SERVER);
				while(!done) {
					DatagramPacket requestPacket=new DatagramPacket(buffer, buffer.length);
					//接收 
					socket.receive(requestPacket);
					byte[] requestBytes=requestPacket.getData();
					int dataLen=requestPacket.getLength();
					boolean isValid=dataLen>=(UDPConstants.HEADER.length+2+4)&&ByteUtil.startWith(requestBytes,UDPConstants.HEADER);
					if(!isValid) {
						continue;
					}
					System.out.println();
					int index=UDPConstants.HEADER.length;
					short cmd=(short)((requestBytes[index++]<<8) | (requestBytes[index++] & 0xff));
					int requestPort=(requestBytes[index++]<<24) | 
							((requestBytes[index++] & 0xff)<<16) | 
							((requestBytes[index++] & 0xff)<<8) | 
							(requestBytes[index] & 0xff);
					System.out.println("index :"+index);
					System.out.println("send to : "+requestPort);
					if(cmd==1&&requestPort>0) {
						ByteBuffer byteBuffer=ByteBuffer.wrap(buffer);
						byteBuffer.put(UDPConstants.HEADER);
						byteBuffer.putShort((short) 2);
						byteBuffer.putInt(port);
						byteBuffer.put(sn.getBytes());
						int len=byteBuffer.position()+1;
						//直接在数据包中指定ip，port
						//Thread.sleep(100);
						DatagramPacket responsePacket=new DatagramPacket(
								buffer, 
								len,
								requestPacket.getAddress(),
								requestPort);
						socket.send(responsePacket);
					}
				}
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				close();
			}
		}
		
		//关闭socket
		public void close() {
			if(socket!=null) {
				socket.close();
				socket=null;
			}
		}
		
		//设置done为true，以退出循环，然后关闭socket
		public void exit() {
			done=true;
			close();
		}
	}
}
