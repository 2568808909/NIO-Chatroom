package impl;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import core.CloseUtils;
import core.IoArgs;
import core.IoProvider;
import core.Receiver;
import core.Sender;

public class SocketChannelAdapater implements Receiver, Sender, Cloneable {

    private final AtomicBoolean isClose = new AtomicBoolean(false);
    private final SocketChannel socketChannel;
    private final IoProvider ioProvider;
    private final OnChannelStatusChangedListener listener;

    private IoArgs.IoArgsEventProcessor receiveIoEventProcessor;
    private IoArgs.IoArgsEventProcessor sendIoEventProcessor;

    private IoArgs receiveArgsTemp;

    private IoProvider.HandleInputCallback inputCallback = new IoProvider.HandleInputCallback() {
        @Override
        protected void canProviderInput() {
            if (isClose.get()) {
                return;
            }
            IoArgs.IoArgsEventProcessor processor = SocketChannelAdapater.this.receiveIoEventProcessor;
            IoArgs args = processor.provideIoArgs();
            //具体的读取操作
            try {
                if (args == null) {
                    processor.onConsumeFailed(null, new IOException("ProvideIoArgs is null"));
                } else if (args.readForm(socketChannel) > 0) {
                    //完成读取，进行回调
                    processor.onConsumeCompleted(args);
                } else {
                    processor.onConsumeFailed(args, new IOException("Cannot read any data!"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                CloseUtils.close(SocketChannelAdapater.this);
            }
        }
    };

    private IoProvider.HandleOutputCallback outputCallback = new IoProvider.HandleOutputCallback() {

        @Override
        protected void canProviderOutput() {
            if (isClose.get()) {
                return;
            }
            IoArgs.IoArgsEventProcessor processor = SocketChannelAdapater.this.sendIoEventProcessor;
            IoArgs args = processor.provideIoArgs();
            try {
                if (args == null) {
                    processor.onConsumeFailed(null, new IOException("ProvideIoArgs is null"));
                } else if (args.writeTo(socketChannel) > 0) {
                    //完成读取，进行回调
                    processor.onConsumeCompleted(args);
                } else {
                    processor.onConsumeFailed(args, new IOException("Cannot write any data!"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                CloseUtils.close(SocketChannelAdapater.this);
            }
        }
    };

    public SocketChannelAdapater(
            SocketChannel channel,
            IoProvider ioProvider,
            OnChannelStatusChangedListener listener) throws IOException {
        this.socketChannel = channel;
        this.ioProvider = ioProvider;
        this.listener = listener;
        channel.configureBlocking(false);
    }

    @Override
    public void close() throws IOException {
        //CAS,比较主存中的isClose值是否为false，是则更新为true，更新成功返回true
        if (isClose.compareAndSet(false, true)) {
            //解除注册与回调
            ioProvider.unRegisterInput(socketChannel);
            ioProvider.unRegisterOutput(socketChannel);
            //关闭当前channel
            CloseUtils.close(socketChannel);
        }
    }

    @Override
    public void setReceiveListener(IoArgs.IoArgsEventProcessor processor) {
        this.receiveIoEventProcessor = processor;
    }

    @Override
    public boolean postReceiveAsync() throws IOException {
        if (isClose.get()) {
            System.out.println("Current channel is close");
            throw new IOException();
        }
        return ioProvider.registerInput(socketChannel, inputCallback);
    }

    @Override
    public void setSendListener(IoArgs.IoArgsEventProcessor processor) {
        this.sendIoEventProcessor = processor;
    }

    @Override
    public boolean postSendAsync() throws IOException {
        if (isClose.get()) {
            System.out.println("Current channel is close");
            throw new IOException();
        }
        return ioProvider.registerOutput(socketChannel, outputCallback);
    }


    public interface OnChannelStatusChangedListener {
        void onChannelClose(SocketChannel socketChannel);
    }


}
