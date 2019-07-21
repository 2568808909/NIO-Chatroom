package core;

import java.io.IOException;
import java.io.InputStream;

/**
 * 发送包的定义
 * @author Administrator
 *
 */
public abstract class SendPacket<T extends InputStream> extends Packet<T>{
	
	private boolean isCancel;

	public boolean isCancel() {
		return isCancel;
	}
}
