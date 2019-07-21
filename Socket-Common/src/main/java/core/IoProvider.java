package core;

import java.io.Closeable;
import java.nio.channels.SocketChannel;

/**
 * 该接口主要提供注册，解除注册的方法规范
 * @author Administrator
 *
 */
public interface IoProvider extends Closeable{
	
	boolean registerInput(SocketChannel channel,HandleInputCallback callback);
	
	boolean registerOutput(SocketChannel channel, HandleOutputCallback callback);
	
	void unRegisterInput(SocketChannel channel);
	
	void unRegisterOutput(SocketChannel channel);
	
	
	abstract class HandleInputCallback implements Runnable{
		public final void run() {canProviderInput();}
		
		protected abstract void canProviderInput();
	}
	
	abstract class HandleOutputCallback implements Runnable{
		private Object attach;
		
		public final void run() {canProviderOutput();}
		
		public final void setAttach(Object attach) {this.attach=attach;}
		
		public final <T> T getAttach() {
			@SuppressWarnings("unchecked")
			T attach=(T)this.attach;
			return attach;
		}
		
		protected abstract void canProviderOutput();
	}
}
