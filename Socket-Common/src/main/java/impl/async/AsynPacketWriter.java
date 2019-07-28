package impl.async;

import core.IoArgs;
import core.ReceivePacket;

import java.io.Closeable;

public class AsynPacketWriter implements Closeable {

    private final PacketProvider provider;

    public AsynPacketWriter(PacketProvider provider){
        this.provider=provider;
    }

    public void consumeIoArgs(IoArgs args) {

    }

    public IoArgs takeIoArgs() {
        return null;
    }

    public void close() {
    }

    /**
     * packet提供者
     */
    interface PacketProvider {
        /**
         * 获取packet操作
         * @return 如果队列中有可以发送的packet则不会返回null
         */
        ReceivePacket takePacket(byte type,long length,byte[] headerInfo);

        /**
         * 结束一份packet
         * @param packet 接收包
         * @param isSucceed 是否接收成功
         */
        void completedPacket(ReceivePacket packet, boolean isSucceed);
    }
}
