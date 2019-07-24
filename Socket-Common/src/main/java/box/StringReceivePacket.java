package box;

import core.Packet;
import core.ReceivePacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class StringReceivePacket extends AbsByteArrayReceivePacket<String>{

	public StringReceivePacket(long len) {
		super(len);
	}

	@Override
	protected String buildEntity(ByteArrayOutputStream stream) {
		return new String(stream.toByteArray());
	}

	@Override
	protected byte type() {
		return Packet.TYPE_MEMORY_STRING;
	}

}
