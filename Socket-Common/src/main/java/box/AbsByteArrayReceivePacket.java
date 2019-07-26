package box;

import core.Packet;
import core.ReceivePacket;

import java.io.ByteArrayOutputStream;

public abstract class AbsByteArrayReceivePacket<Entity> extends ReceivePacket<ByteArrayOutputStream, Entity> {

    public AbsByteArrayReceivePacket(long len) {
        super(len);
    }


    @Override
    public byte type() {
        return Packet.TYPE_MEMORY_BYTES;
    }

    protected final ByteArrayOutputStream createStream() {
        return new ByteArrayOutputStream((int) length);
    }
}
