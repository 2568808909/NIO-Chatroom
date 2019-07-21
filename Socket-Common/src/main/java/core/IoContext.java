package core;

import java.io.IOException;

public class IoContext{

	private static IoContext INSTANCE;
	private final IoProvider ioProvider;
	
	private IoContext(IoProvider ioProvider) {
		this.ioProvider=ioProvider;
	}
	
	public static IoContext get() {
		return INSTANCE;
	}
	
	public static void close()throws IOException {
		if(INSTANCE!=null) {
			INSTANCE.callClose();
		}
	}
	
	private void callClose() throws IOException {
		ioProvider.close();
	}
	
	public IoProvider getIoProvider() {
		return ioProvider;
	}
	
	public static StartBoot setup() throws IOException {
		return new StartBoot();
	}
	
	public static class StartBoot{
		private IoProvider ioProvider;
		
		private StartBoot(){}
		
		public StartBoot ioProvider(IoProvider ioProvider) {
			this.ioProvider=ioProvider;
			return this;
		}
		
		public IoContext start() {
			INSTANCE=new IoContext(ioProvider);
			return INSTANCE;
		}
	}

}
