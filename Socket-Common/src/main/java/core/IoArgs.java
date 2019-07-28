package core;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

/**
 * IO参数类，对ByteBuffer进行封装，防止ByteBuffer无节制申请内存， 同时封装ByteBuffer的操作，简化开发
 *
 * @author Administrator
 */
public class IoArgs {

    private int limit = 256;
    private ByteBuffer buffer = ByteBuffer.allocate(limit);

    /**
     * 读取数据
     */
    public int readForm(ReadableByteChannel channel) throws IOException {
        //startWriting();
        int bytesProduced = 0;
        while (buffer.hasRemaining()) {
            int len = channel.read(buffer);
            if (len < 0) {
                throw new EOFException();
            }
            bytesProduced += len;
        }
        //finishWriting();
        return bytesProduced;
    }

    /**
     * 写入数据
     */
    public int writeTo(WritableByteChannel channel) throws IOException {
        int bytesProduced = 0;
        while (buffer.hasRemaining()) {
            int len = channel.write(buffer);
            if (len < 0) {
                throw new EOFException();
            }
            bytesProduced += len;
        }
        return bytesProduced;
    }

    /**
     * 从SocketChannel中读取数据
     */
    public int readForm(SocketChannel channel) throws IOException {
        startWriting();
        int bytesProduced = 0;
        while (buffer.hasRemaining()) {
            int len = channel.read(buffer);
            if (len < 0) {
                throw new EOFException();
            }
            bytesProduced += len;
        }
        finishWriting();
        return bytesProduced;
    }

    /**
     * 向SocketChannel中写入数据
     */
    public int writeTo(SocketChannel channel) throws IOException {
        int bytesProduced = 0;
        while (buffer.hasRemaining()) {
            int len = channel.write(buffer);
            if (len < 0) {
                throw new EOFException();
            }
            bytesProduced += len;
        }
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
     */
    public void setLimit(int limit) {
        this.limit = Math.min(limit, buffer.capacity());
    }

    /**
     * 用于判断当前IoArgs是否还有空间可以写入数据
     */
    public boolean remained() {
        return buffer.remaining() > 0;
    }

    /**
     * 从bytes中进行消费
     */
    public int readForm(byte[] bytes, int offset, int count) {
        int size = Math.min(count, buffer.remaining());
        if (size <= 0) {
            return 0;
        }
        buffer.put(bytes, offset, size);
        return size;
    }

    /**
     * 获取数据到bytes中
     */
    public int writeTo(byte[] bytes, int offset) {
        int size = Math.min(bytes.length - offset, buffer.remaining());
        buffer.get(bytes, offset, size);
        return size;
    }

    public int fillEntity(int size) {
        int fillSize=Math.min(size,buffer.remaining());
        buffer.position(buffer.position()+fillSize);
        return fillSize;
    }

    public interface IoArgsEventProcessor {
        /**
         * 提供一份可消费的IoArgs
         */
        IoArgs provideIoArgs();

        /**
         * 消费失败时回调
         */
        void onConsumeFailed(IoArgs args, Exception e);

        /**
         * 消费成功时回调
         */
        void onConsumeCompleted(IoArgs args);
    }

//    public void writeLength(int total) {
//        startWriting();
//        buffer.putInt(total);
//        finishWriting();
//    }

    public int readLength() {
        return buffer.getInt();
    }

    public int capacity() {
        return buffer.capacity();
    }


}
