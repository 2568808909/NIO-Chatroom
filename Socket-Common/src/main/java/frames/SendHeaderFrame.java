package frames;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import core.Frame;
import core.IoArgs;
import core.SendPacket;

public class SendHeaderFrame extends AbsSendPacketFrame{

    public static final int PACKET_HEADER_FRAME_MIN_LENGTH=6;
    private final byte[] body;

    public SendHeaderFrame(short identifier, SendPacket packet) {
        super(PACKET_HEADER_FRAME_MIN_LENGTH,
                Frame.TYPE_PACKET_HEADER,
                Frame.FLAG,
                identifier,
                packet);
        final long length=packet.length();
        final byte type=packet.type();
        final byte[] headInfo=packet.headInfo();

        this.body=new byte[bodyRemaining];
        body[0]=(byte)(length>>32);
        body[1]=(byte)(length>>24);
        body[2]=(byte)(length>>16);
        body[3]=(byte)(length>>8);
        body[4]=(byte)length;

        body[5]=type;
        if(headInfo!=null){
            System.arraycopy(headInfo,0,body,PACKET_HEADER_FRAME_MIN_LENGTH,headInfo.length);
        }
    }

    @Override
    protected int consumeBody(IoArgs args) throws IOException {
        int count=bodyRemaining;
        int offset=body.length-count;
        return args.readForm(body,offset,count);
    }

    @Override
    public Frame nextFrame() {
        InputStream inputStream=packet.open();
        ReadableByteChannel channel= Channels.newChannel(inputStream);
        return new SendEntityFrame(getBodyIdentifier(),packet.length(),channel,packet);
    }
}
