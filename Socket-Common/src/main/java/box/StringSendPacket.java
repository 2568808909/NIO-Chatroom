package box;

import core.SendPacket;

public class StringSendPacket extends SendPacket{

	private final byte[] bytes;
	
	public StringSendPacket(String msg) {
		this.bytes=msg.getBytes();
		this.length=bytes.length;
	}
	
	@Override
	public byte[] bytes() {
		return bytes;
	}
	
	

}
