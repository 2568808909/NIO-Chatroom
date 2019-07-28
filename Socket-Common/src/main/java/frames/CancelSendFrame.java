package frames;

import core.Frame;
import core.IoArgs;

import java.io.IOException;

public class CancelSendFrame extends AbsSendFrame {

    public CancelSendFrame(short identifier) {
        super(0, Frame.TYPE_COMMAND_SEND_CANCEL, Frame.FLAG, identifier);
    }

    /**
     * 无需消费数据
     *
     * @param args
     * @return
     * @throws IOException
     */
    @Override
    protected int consumeBody(IoArgs args) throws IOException {
        return 0;
    }

    /**
     * 无下一帧，返回空
     *
     * @return
     */
    @Override
    public Frame nextFrame() {
        return null;
    }
}
