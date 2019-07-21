package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import util.ByteUtil;
import util.UDPConstants;

public class ClientSearcher {
	
	public final static int LISTEN_PORT=UDPConstants.PORT_CLIENT_RESPONSE;
	
	public static ServerInfo searchServer(int timeout) {
		CountDownLatch receiveLatch=new CountDownLatch(1);
		Searcher searcher=null;
		try {
			searcher=listen(receiveLatch);
			sendBroadcast();
			receiveLatch.await(timeout,TimeUnit.MILLISECONDS);
		}catch (Exception e) {
			//e.printStackTrace();
		}
		if(searcher==null) {
			return null;
		}
		List<ServerInfo> infos=searcher.getDeviceAndClose();
		if(infos.size()>0){
			return infos.get(0);
		}
		return null;
	}
	
	public static Searcher listen(CountDownLatch receiveLatch)throws InterruptedException {
		CountDownLatch startLatch=new CountDownLatch(1);
		Searcher searcher=new Searcher(LISTEN_PORT, startLatch,receiveLatch);
		searcher.start();
		startLatch.await();
		return searcher;
	}
	
	public static void sendBroadcast()throws IOException {
		DatagramSocket socket=new DatagramSocket(20000);
		byte[] buffer=new byte[128];
		ByteBuffer byteBuffer=ByteBuffer.wrap(buffer);
		byteBuffer.put(UDPConstants.HEADER);
		byteBuffer.putShort((short) 1);
		byteBuffer.putInt(UDPConstants.PORT_CLIENT_RESPONSE);
		DatagramPacket requestPacket=new DatagramPacket(byteBuffer.array(), byteBuffer.position()+1);
		requestPacket.setAddress(InetAddress.getByName("255.255.255.255"));
		requestPacket.setPort(UDPConstants.PORT_SERVER);
		//发送
		socket.send(requestPacket);
		socket.close();
	}
	
	public static class Searcher extends Thread{
		private int port;
		private CountDownLatch startLatch,receiveLatch;
		private List<ServerInfo> devices;
		private boolean done;
		private DatagramSocket socket;
		private final byte[] buffer=new byte[128];
		private int minLen=UDPConstants.HEADER.length+2+4;
		
		public Searcher(int port,CountDownLatch startLatch,CountDownLatch receiveLatch) {
			done=false;
			devices=new ArrayList<>();
			this.port=port;
			this.startLatch=startLatch;
			this.receiveLatch=receiveLatch;
		}
		
		@Override
		public void run() {
			try {
				socket=new DatagramSocket(port);
				startLatch.countDown();
				while(!done) {
					DatagramPacket packet=new DatagramPacket(buffer, buffer.length);
					System.out.println("waiting");
					socket.receive(packet);
					byte[] data=packet.getData();
					int dataLen=packet.getLength();
					boolean isValid=(dataLen>=minLen)&&
							(ByteUtil.startWith(data, UDPConstants.HEADER));
					if(!isValid) {
						continue;
					}
					ByteBuffer byteBuffer=ByteBuffer.wrap(data, UDPConstants.HEADER.length, dataLen);
					int cmd=byteBuffer.getShort();
					int tcpPort=byteBuffer.getInt();
					String sn=new String(data,minLen,dataLen-minLen);
					ServerInfo info=new ServerInfo(packet.getAddress().getHostAddress(), sn, tcpPort);
					devices.add(info);
					receiveLatch.countDown();
				}
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				close();
			}
		}
		
		private void close() {
			if(socket!=null) {
				socket.close();
				socket=null;
			}
		}
		
		public void exit() {
			done=true;
			close();
		}
		
		public List<ServerInfo> getDeviceAndClose() {
			exit();
			return devices;
		}
	}
}
