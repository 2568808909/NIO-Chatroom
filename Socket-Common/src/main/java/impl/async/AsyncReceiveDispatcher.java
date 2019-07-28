package impl.async;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import box.StringReceivePacket;
import core.CloseUtils;
import core.IoArgs;
import core.Packet;
import core.ReceiveDispatcher;
import core.ReceivePacket;
import core.Receiver;

public class AsyncReceiveDispatcher implements ReceiveDispatcher, IoArgs.IoArgsEventProcessor,AsynPacketWriter.PacketProvider {
    private AtomicBoolean isClose = new AtomicBoolean(false);
    private final Receiver receiver;
    private final ReceivePacketCallback callback;
    private WritableByteChannel packetChannel;
    //private IoArgs ioArgs = new IoArgs();
    private ReceivePacket<?, ?> packetTemp;
    private AsynPacketWriter writer=new AsynPacketWriter(this);
    //private long total;
    //private long position;

//    /**
//     * 解析数据到Packet
//     *
//     * @param args
//     */
//    private void assemblePacket(IoArgs args) {
//        if (packetTemp == null) {
//            int length = args.readLength();
//            byte type=length>200? Packet.TYPE_STREAM_FILE:Packet.TYPE_MEMORY_STRING;
//            packetTemp = callback.onArrivedNewPacket(type,length);
//            packetChannel = Channels.newChannel(packetTemp.open());
//            total = length;
//            position = 0;
//        }
//        try {
//            int count = args.writeTo(packetChannel);
//            position += count;
//            if (position == total) {
//                completedPacket(true);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            completedPacket(false);
//        }
//    }

//    /**
//     * 完成数据的接受操作
//     */
//    private void completedPacket(boolean isSuccess) {
//        ReceivePacket packet = this.packetTemp;
//        CloseUtils.close(packet, packetChannel);
//        packetTemp = null;
//        packetChannel = null;
//        if (packet != null) {
//            callback.onReceivePacketCompleted(packet);
//        }
//    }

    public AsyncReceiveDispatcher(Receiver receiver, ReceivePacketCallback callback) {
        this.receiver = receiver;
        this.receiver.setReceiveListener(this);
        this.callback = callback;
    }

    @Override
    public void start() {
        registerReceive();
    }

    private void registerReceive() {
        try {
            receiver.postReceiveAsync();
        } catch (Exception e) {
            closeAndNotify();
        }

    }

    private void closeAndNotify() {
        CloseUtils.close(this);
    }

    @Override
    public void stop() {

    }

    @Override
    public void close() throws IOException {
        if (isClose.compareAndSet(false, true)) {
            writer.close();
//            ReceivePacket packet = packetTemp;
//            if (packet != null) {
//                packetTemp = null;
//                CloseUtils.close(packet);
//            }
        }
    }

    @Override
    public IoArgs provideIoArgs() {
        return writer.takeIoArgs();
//        IoArgs args = ioArgs;
//        int receiveSize;
//        if (packetTemp == null) {
//            receiveSize = 4;
//        } else {
//            receiveSize = (int) Math.min(total - position, args.capacity());
//        }
//        args.setLimit(receiveSize);
//        return args;
    }

    @Override
    public void onConsumeFailed(IoArgs args, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onConsumeCompleted(IoArgs args) {
        do {
            writer.consumeIoArgs(args);
        }while(args.remained());
        registerReceive();
    }

    @Override
    public ReceivePacket takePacket(byte type, long length, byte[] headerInfo) {
        return callback.onArrivedNewPacket(type,length);
    }

    @Override
    public void completedPacket(ReceivePacket packet, boolean isSucceed) {
        CloseUtils.close(packet);
        callback.onReceivePacketCompleted(packet);
    }
}
