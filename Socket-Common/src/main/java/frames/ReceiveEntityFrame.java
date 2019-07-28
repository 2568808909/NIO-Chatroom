package frames;

import core.IoArgs;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

public class ReceiveEntityFrame extends AbsReceiveFrame{

    private WritableByteChannel channel;

    public ReceiveEntityFrame(byte[] header) {
        super(header);
    }

    public void bindPacketChannel(WritableByteChannel channel){
        this.channel=channel;
    }

    @Override
    protected int consumeBody(IoArgs args) throws IOException {
        return args.writeTo(channel);
    }
}
