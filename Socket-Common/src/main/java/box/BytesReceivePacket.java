package box;

import core.Packet;

import java.io.ByteArrayOutputStream;

public class BytesReceivePacket extends AbsByteArrayReceivePacket<byte[]>{

    public BytesReceivePacket(long len) {
        super(len);
    }

    @Override
    protected byte[] buildEntity(ByteArrayOutputStream stream) {
        return stream.toByteArray();
    }

    @Override
    protected byte type() {
        return Packet.TYPE_MEMORY_BYTES;
    }
}
