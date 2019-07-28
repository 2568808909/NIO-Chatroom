package frames;

import core.Frame;
import core.IoArgs;

import java.io.IOException;

public abstract class AbsReceiveFrame extends Frame {

    protected volatile int bodyRemaing;

    public AbsReceiveFrame(byte[] header) {
        super(header);
        bodyRemaing=getBodyLength();
    }

    @Override
    public synchronized boolean handle(IoArgs args) throws IOException {
        if(bodyRemaing==0){
            //已经读取所有数据
            return true;
        }
        bodyRemaing-=consumeBody(args);
        return false;
    }

    /**
     * 接收帧没有构建下一帧的操作，接收只负责不断接收来自发送方发送的帧即可
     * 所以此处定义为final，不用在向下继承了
     * @return
     */
    @Override
    public final Frame nextFrame() {
        return null;
    }

    protected abstract int consumeBody(IoArgs args) throws IOException;
}
