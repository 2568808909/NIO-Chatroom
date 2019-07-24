package core;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 接收包的定义
 * @author Administrator
 *
 */
public abstract class ReceivePacket<Stream extends OutputStream,Entity> extends Packet<Stream>{
    private Entity entity;

    public ReceivePacket(long length){
        this.length=length;
    }

    /**
     * 获取实体
     * @return
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * 根据接收到的流转化为对应的实体
     * @param stream
     * @return
     */
    protected abstract Entity buildEntity(Stream stream);

    /**
     * 先关闭流，随后将流中的内容转化为实体。
     * @param stream
     * @throws IOException
     */
    protected final void closeStream(Stream stream) throws IOException {
        super.closeStream(stream);
        entity=buildEntity(stream);
    }
}
