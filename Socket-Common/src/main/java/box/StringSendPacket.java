package box;

import core.Packet;
import core.SendPacket;

import java.io.ByteArrayInputStream;

public class StringSendPacket extends BytesSendPacket{
	
	public StringSendPacket(String msg) {
		super(msg.getBytes());
	}



	@Override
	protected byte type() {
		return Packet.TYPE_MEMORY_STRING;
	}
}
