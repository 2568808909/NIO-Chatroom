package server;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import core.CloseUtils;
import core.Connector;
import core.Packet;
import core.ReceivePacket;
import foo.Foo;
import util.CloseUtil;

public class ClientHandler extends Connector {

    private File cachePath;
    private ClientHandlerCallBack clientHandlerCallBack;
    private final String clientInfo;


    public ClientHandler(SocketChannel socketChannel, ClientHandlerCallBack clientHandlerCallBack,File cachePath) throws IOException {
        this.clientHandlerCallBack = clientHandlerCallBack;
        this.clientInfo = socketChannel.getRemoteAddress().toString();
        this.cachePath=cachePath;
        setup(socketChannel);
        System.out.println("新客户端连接: " + clientInfo);
    }

    public void exit() {
        CloseUtils.close(this);
    }

    @Override
    public void onChannelClose(SocketChannel socketChannel) {
        super.onChannelClose(socketChannel);
        exitBySelf();
    }

    @Override
    protected File onCreateNewReceiveFile() {
        return Foo.createRandomTemp(cachePath);
    }

    @Override
    protected void onReceivedNewPacket(ReceivePacket packet) {
        super.onReceivedNewPacket(packet);
        if(packet.type()== Packet.TYPE_MEMORY_STRING){
            String msg=(String) packet.getEntity();
            System.out.println(key.toString() + ":" + msg);
            clientHandlerCallBack.onReadNotify(this,msg);
        }
    }




    public void exitBySelf() {
        exit();
        clientHandlerCallBack.onCloseNotify(this);
    }
}