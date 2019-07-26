package core;

import java.io.Closeable;
import java.io.IOException;

public abstract class Packet<Stream extends Closeable> implements Closeable {
    protected long length;
    private Stream stream;

    //byte 类型
    public static final byte TYPE_MEMORY_BYTES = 1;
    //String 类型
    public static final byte TYPE_MEMORY_STRING = 2;
    //文件类型
    public static final byte TYPE_STREAM_FILE = 3;
    //长连接流 类型
    public static final byte TYPE_STREAM_DIRECT = 4;


    protected abstract Stream createStream();

    protected void closeStream(Stream stream) throws IOException {
        stream.close();
    }

    public Packet() {

    }

    /**
     * TYPE_MEMORY_BYTES=1 byte
     * TYPE_MEMORY_STRING String
     * TYPE_STREAM_FILE=3 文件
     * TYPE_STREAM_DIRECT 长连接流
     */
    public abstract byte type();

    public long length() {
        return length;
    }

    @Override
    public void close() throws IOException {
        if (stream != null) {
            closeStream(stream);
            stream = null;
        }
    }

    public Stream open() {
        if (stream == null) {
            stream = createStream();
        }
        return stream;
    }

}
