package core;

import java.io.Closeable;
import java.io.IOException;

public class Packet implements Closeable{
	protected byte type;
	protected int length;
	
	public Packet() {
		
	}
	
	public byte type() {
		return type;
	}
	
	public int length() {
		return length;
	}

	@Override
	public void close() throws IOException {
		
	}
}
