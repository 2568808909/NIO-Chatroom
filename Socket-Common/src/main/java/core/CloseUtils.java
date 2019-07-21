package core;

import java.io.Closeable;

/**
 * 该类为关闭操作的工具类，提供关闭资源的方法
 * @author Administrator
 *
 */
public class CloseUtils {
	
	/**
	 * 关闭方法，可以传入多个实现了Closeable接口的对象，执行它们的close方法
	 * @param closeables
	 */
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
