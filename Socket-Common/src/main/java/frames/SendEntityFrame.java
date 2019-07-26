package frames;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

import core.Frame;
import core.IoArgs;
import core.SendPacket;

public class SendEntityFrame extends AbsSendPacketFrame{

    private long unConsumeEntityLength;
    private ReadableByteChannel channel;

    public SendEntityFrame(short identifier,
                           long entityLength,
                           ReadableByteChannel channel,
                           SendPacket packet) {
        super((int)Math.min(entityLength,Frame.MAX_CAPACITY),
                Frame.TYPE_PACKET_HEADER,
                Frame.FLAG,
                identifier,
                packet);
        this.channel=channel;
        this.unConsumeEntityLength=entityLength-bodyRemaining;
    }


    @Override
    protected int consumeBody(IoArgs args) throws IOException {
        if(packet==null){
            return args.fillEntity(bodyRemaining);
        }
        return args.readForm(channel);
    }

    @Override
    public Frame buildNextFrame() {
        if(unConsumeEntityLength==0){
            return null;
        }
        return new SendEntityFrame(getBodyIdentifier(),unConsumeEntityLength,channel,packet);
    }
}
