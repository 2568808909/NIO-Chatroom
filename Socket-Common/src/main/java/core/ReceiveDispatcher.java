package core;

import java.io.Closeable;

/**
 * 接收者数据的调度封装
 * 把一份数据或者多份的IoArgs组合成一份Packet，一个Packet代表一个完整的数据
 * @author Administrator
 *
 */
public interface ReceiveDispatcher extends Closeable{
	void start();
	
	void stop();
	
	interface ReceivePacketCallback{
		/**
		 * 接收packet完成后进行回调
		 * @param packet
		 */
		void onReceivePacketCompleted(ReceivePacket packet);

        ReceivePacket<?,?> onArrivedNewPacket(byte type,long length);
    }
}
