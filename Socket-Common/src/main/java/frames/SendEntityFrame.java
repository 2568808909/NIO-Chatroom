package frames;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

import core.Frame;
import core.IoArgs;
import core.SendPacket;

public class SendEntityFrame extends AbsSendPacketFrame{

    public SendEntityFrame(short identifier,
                           long entityLength,
                           ReadableByteChannel channel,
                           SendPacket packet) {
        super((int)Math.min(entityLength,Frame.MAX_CAPACITY),
                Frame.TYPE_PACKET_HEADER,
                Frame.FLAG,
                identifier,
                packet);

    }


    @Override
    protected int consumeBody(IoArgs args) throws IOException {
        return 0;
    }

    @Override
    public Frame nextFrame() {
        return null;
    }
}
