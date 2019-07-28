package frames;

import java.io.IOException;

import core.Frame;
import core.IoArgs;
import core.SendPacket;

public abstract class AbsSendPacketFrame extends AbsSendFrame{

    protected volatile SendPacket<?> packet;

    public AbsSendPacketFrame(int length, byte type, byte flag, short identifier,SendPacket packet) {
        super(length, type, flag, identifier);
        this.packet=packet;
    }

    public final synchronized boolean abort(){
        boolean isSending=isSending();
        if(isSending){
            fillDirtyDataOnAbort();
        }
        packet=null;
        return !isSending;
    }

    public synchronized SendPacket getSendPacket(){ return packet; }

    protected void fillDirtyDataOnAbort(){
        
    }

    @Override
    public final synchronized Frame nextFrame() {
        return packet==null?null:buildNextFrame();
    }

    protected abstract Frame buildNextFrame();

    @Override
    public synchronized boolean handle(IoArgs args) throws IOException {
        //没有发送过数据,并且已经取消发送
        if(packet==null&&!isSending()){
            return true;
        }
        return super.handle(args);
    }
}
