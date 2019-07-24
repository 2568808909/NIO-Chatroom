package box;

import core.Packet;
import core.SendPacket;

import java.io.ByteArrayInputStream;

public class BytesSendPacket extends SendPacket<ByteArrayInputStream> {

    private final byte[] bytes;

    public BytesSendPacket(byte[] bytes){
        this.bytes=bytes;
        this.length=bytes.length;
    }

    @Override
    protected ByteArrayInputStream createStream() {
        return new ByteArrayInputStream(bytes);
    }

    @Override
    protected byte type() {
        return Packet.TYPE_MEMORY_BYTES;
    }
}
