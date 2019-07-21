package core;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * IO参数类，对ByteBuffer进行封装，防止ByteBuffer无节制申请内存，
 * 同时封装ByteBuffer的操作，简化开发
 * @author Administrator
 *
 */
public class IoArgs {
	
	private int limit=5;
	private byte[] data=new byte[256];
	private ByteBuffer buffer=ByteBuffer.wrap(data);
	
	/**
	 * 从bytes中读取数据
	 * @param bytes
	 * @param offset
	 * @return
	 */
	public int readForm(byte[] bytes,int offset) {
		int size=Math.min(bytes.length-offset, buffer.remaining());
		buffer.put(bytes,offset,size);
		return size;
	}
	
	/**
	 * 写入数据到bytes中
	 * @param bytes
	 * @param offset
	 * @return
	 */
	public int writeTo(byte[] bytes,int offset) {
		int len=bytes.length-offset;
		int len2=buffer.remaining();
		int size=Math.min(bytes.length-offset, buffer.remaining());
		buffer.get(bytes,offset,size);
		return size;
	}
	
	/**
	 * 从SocketChannel中读取数据
	 * @param channel
	 * @return
	 * @throws IOException
	 */
	public int readForm(SocketChannel channel)throws IOException {
		startWriting();
		int bytesProduced=0;
		while(buffer.hasRemaining()) {
			int len=channel.read(buffer);
			if(len<0) {
				throw new EOFException();
			}
			bytesProduced+=len;
		}
		finishWriting();
		return bytesProduced;
	}
	
	/**
	 * 向SocketChannel中写入数据
	 * @param channel
	 * @return
	 * @throws IOException
	 */
	public int writeTo(SocketChannel channel)throws IOException {
		int bytesProduced=0;
		while(buffer.hasRemaining()) {
			int len=channel.write(buffer);
			if(len<0) {
				throw new EOFException();
			}
			bytesProduced+=len;
		}
		System.out.println("byte len :"+bytesProduced);
		return bytesProduced;
	}
	
//	/**
//	 * 读取ByteBuffer中的数据并丢弃换行符
//	 * @return
//	 */
//	public String bufferString() {
//		return new String(buffer.array(), 0, buffer.position()-1);
//	}
	
	public void startWriting() {
		buffer.clear();
		buffer.limit(limit);
	}
	
	public void finishWriting() {
		buffer.flip();
	}
	
	/**
	 * 设置单次写操作所能容纳的空间
	 * @param limit
	 */
	public void setLimit(int limit) {
		this.limit=limit;
	}
	
	public interface IoArgsEventListener{
		void onStart(IoArgs args);
		
		void onCompleted(IoArgs args);
	}

	public void writeLength(int total) {
		buffer.putInt(total);
	}
	
	public int readLength() {
		return buffer.getInt();
	}

	public int capacity() {
		return buffer.capacity();
	}
	
	
}
