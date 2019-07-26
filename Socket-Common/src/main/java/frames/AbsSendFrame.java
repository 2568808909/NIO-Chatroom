package frames;

import java.io.IOException;

import core.Frame;
import core.IoArgs;

public abstract class AbsSendFrame extends Frame {

    protected volatile byte headerRemaining=Frame.FRAME_HEADER_LENGTH;

    protected volatile int bodyRemaining;

    public AbsSendFrame(int length, byte type, byte flag, short identifier) {
        super(length, type, flag, identifier);
        bodyRemaining=length;
    }

    @Override
    public synchronized boolean handle(IoArgs args) throws IOException {
        try {
            args.setLimit(headerRemaining + bodyRemaining);
            args.startWriting();
            //头部还有数据需要消费
            if (headerRemaining > 0 && args.remained()) {
                headerRemaining -= consumeHeader(args);
            }
            if (headerRemaining == 0 && args.remained() && bodyRemaining > 0) {
                bodyRemaining -= consumeBody(args);
            }
            //返回是否所有数据都消费完成
            return headerRemaining == 0 && bodyRemaining == 0;
        }finally {
            args.finishWriting();
        }
    }

    private byte consumeHeader(IoArgs args) throws IOException{
        int count=headerRemaining;
        int offset=header.length-count;
        return (byte)args.readForm(header,offset,count);
    }

    protected abstract int consumeBody(IoArgs args) throws IOException;
}
