package core;

/**
 * 发送包的定义
 * @author Administrator
 *
 */
public abstract class SendPacket extends Packet{
	
	private boolean isCancel;
	
	public abstract byte[] bytes();
	
	public boolean isCancel() {
		return isCancel;
	}
}
