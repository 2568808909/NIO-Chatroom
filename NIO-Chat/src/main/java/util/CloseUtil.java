package util;

import java.io.Closeable;

public class CloseUtil {
	
	public static void close(Closeable... closeables) {
		if(closeables!=null) {
			try {
				for (Closeable closeable : closeables) {
					if(closeable!=null) {
						closeable.close();
					}
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
