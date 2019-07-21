package util;

public class MessageCreator {
	public final static String SN_MESSAGE="收到暗号，over :";
	public final static String PORT_MESSAGE="这是暗号,收到请回复: ";
	
	public static String getPortMessage(int port) {
		return PORT_MESSAGE+port;
	}
	
	public static String getSNMessage(String sn) {
		return SN_MESSAGE+sn;
	}
	
	public static int parsePort(String portMessage) {
		return Integer.parseInt(portMessage.substring(PORT_MESSAGE.length()));
	}
	
	public static String parseSN(String SNMessage) {
		return SNMessage.substring(SN_MESSAGE.length());
	}
}
