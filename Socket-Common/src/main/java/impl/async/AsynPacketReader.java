package impl.async;

import core.Frame;
import core.IoArgs;
import core.SendPacket;
import core.ds.BytePriorityNode;
import frames.AbsSendPacketFrame;
import frames.CancelSendFrame;
import frames.SendEntityFrame;
import frames.SendHeaderFrame;

import java.io.Closeable;
import java.io.IOException;

public class AsynPacketReader implements Closeable {
    private volatile IoArgs args = new IoArgs();
    private final PacketProvider provider;
    private volatile BytePriorityNode<Frame> node; //一个用于存放所有要发送的帧的队列
    private volatile int nodeSize = 0;

    private short lastIentifier = 0;

    private short generateIdentifier() {
        short identifier = ++lastIentifier;
        if (identifier == 255) {
            lastIentifier = 0;
        }
        return identifier;
    }

    public AsynPacketReader(PacketProvider provider) {
        this.provider = provider;
    }

    /**
     * 取消packet对饮的帧发送，如果当前packet已发送部分数据(就算只发送了头数据)
     * 也应该在当前帧队列中发送一份取消发送的标志{@link frames.CancelSendFrame}
     *
     * @param packet 待取消的packet
     */
    public synchronized void cancel(SendPacket packet) {
        if (nodeSize == 0) {
            return;
        }
        for (BytePriorityNode<Frame> x = node, before = null; x != null; before = x, x = x.next) {
            Frame frame = x.item;
            if (frame instanceof AbsSendPacketFrame) {
                AbsSendPacketFrame packetFrame = (AbsSendPacketFrame) frame;
                if (packetFrame.getSendPacket() == packet) {
                    boolean removable = packetFrame.abort();
                    if (removable) {
                        removeFrame(x, before);
                    }
                    if (frame instanceof SendHeaderFrame) {
                        //头帧，并且未发送任何数据，直接取消发送不需要添加取消帧
                        break;
                    }
                    // 添加终止帧，通知接收方
                    CancelSendFrame cancelSendFrame = new CancelSendFrame(packetFrame.getBodyIdentifier());
                    appendNewFrame(cancelSendFrame);
                    //意外终止，设置为失败
                    provider.completedPacket(packet, false);
                    break;
                }
            }
        }
    }

    /**
     * 从provider中拿一份packet进行发送
     *
     * @return 如果当前网络中有数据可以发送，则返回true
     */
    public boolean requestTakePacket() {
        synchronized (this) {
            if (nodeSize >= 1) {
                return true;
            }
        }
        SendPacket packet = provider.takePacket();
        if (packet != null) {
            short identifier = generateIdentifier();
            SendHeaderFrame frame = new SendHeaderFrame(identifier, packet);
            appendNewFrame(frame);
        }
        synchronized (this) {
            return nodeSize > 0;
        }
    }

    public synchronized void close() {
        while (node != null) {
            Frame frame = node.item;
            if (frame instanceof AbsSendPacketFrame) {
                SendPacket packet = ((AbsSendPacketFrame) frame).getSendPacket();
                provider.completedPacket(packet, false);
            }

        }
        nodeSize = 0;
        node = null;
    }

    public IoArgs fillData() {
        Frame currentFrame = getCurrentFrame();
        if (currentFrame == null) {
            return null;
        }
        try {
            if (currentFrame.handle(args)) {
                //消费完本帧
                //尝试基于本帧构建后续帧
                Frame frame = currentFrame.nextFrame();
                if (frame != null) {
                    appendNewFrame(frame);
                } else if (currentFrame instanceof SendEntityFrame) {
                    //末尾实体帧
                    //通知完成
                    provider.completedPacket(((SendEntityFrame) currentFrame).getSendPacket(), true);
                }
                popCurrentFrame();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private synchronized void popCurrentFrame() {
        node = node.next;
        nodeSize--;
        if (node == null) {
            requestTakePacket();
        }
    }

    private Frame getCurrentFrame() {
        if (node == null) {
            return null;
        }
        return node.item;
    }

    private synchronized void appendNewFrame(Frame frame) {
        BytePriorityNode<Frame> newNode = new BytePriorityNode<>(frame);
        if (node != null) {
            node.appendWithPriority(newNode);
        } else {
            node = newNode;
        }
        nodeSize++;
    }

    private synchronized void removeFrame(BytePriorityNode<Frame> removeNode, BytePriorityNode<Frame> before) {
        if (before == null) {
            node = removeNode.next;
        } else {
            before.next = removeNode.next;
        }
        nodeSize--;
        if (node == null) {
            requestTakePacket();
        }
    }

    interface PacketProvider {
        SendPacket takePacket();

        void completedPacket(SendPacket packet, boolean isSucceed);
    }
}
