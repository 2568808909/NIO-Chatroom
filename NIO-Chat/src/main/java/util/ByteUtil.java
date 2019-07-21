package util;

public class ByteUtil {
	public static boolean startWith(byte[] mainByte,byte[] with) {
		boolean result=false;
		if(mainByte.length>-with.length) {			
			result=true;
			for(int i=0;i<with.length;i++) {
				if(mainByte[i]!=with[i]) {
					result=false;
					break;
				}
			}
		}
		return result;
	}
}
