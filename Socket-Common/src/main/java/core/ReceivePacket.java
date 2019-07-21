package core;

/**
 * 接收包的定义
 * @author Administrator
 *
 */
public abstract class ReceivePacket extends Packet{
	
	public abstract void save(byte[] bytes,int count);
}
