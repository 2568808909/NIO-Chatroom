package impl.async;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import box.StringReceivePacket;
import core.CloseUtils;
import core.IoArgs;
import core.ReceiveDispatcher;
import core.ReceivePacket;
import core.Receiver;

public class AsyncReceiveDispatcher implements ReceiveDispatcher{
	private AtomicBoolean isClose=new AtomicBoolean(false);
	private final Receiver receiver;
	private final ReceivePacketCallback callback;
	
	private IoArgs ioArgs=new IoArgs();
	private ReceivePacket packetTemp;
	private byte[] buffer;
	private int total;
	private int position;
	
	private IoArgs.IoArgsEventListener listener=new IoArgs.IoArgsEventListener() {
		
		@Override
		public void onStart(IoArgs args) {
			int receiveSize;
			if(packetTemp==null) {
				receiveSize=4;//也就是长度
			}else {
				receiveSize=Math.min(total-position, args.capacity());
			}
			//设置本次接受数据的大小
			args.setLimit(receiveSize);
		}
		
		@Override
		public void onCompleted(IoArgs args) {
			assemblePacket(args);
			//继续接受下一条数据
			registerReceive();
		}
	};
	
	/**
	 * 解析数据到Packet
	 * @param args
	 */
	private void assemblePacket(IoArgs args) {
		if(packetTemp==null) {
			int length=args.readLength();
			packetTemp=new StringReceivePacket(length);
			buffer=new byte[length];
			total=length;
			position=0;
		}
		int count=args.writeTo(buffer, 0);
		if(count>0) {
			packetTemp.save(buffer, count);
			position+=count;
			if(position==total) {
				completedPacket();
				packetTemp=null;
			}
		}
	}
	
	/**
	 * 完成数据的接受操作
	 */
	private void completedPacket() {
		ReceivePacket packet=this.packetTemp;
		CloseUtils.close(packet);
		callback.onReceivePacketCompleted(packet);
	}

	public AsyncReceiveDispatcher(Receiver receiver,ReceivePacketCallback callback) {
		this.receiver=receiver;
		this.receiver.setReceiveListener(listener);
		this.callback=callback;
	}

	@Override
	public void start() {
		registerReceive();
	}

	private void registerReceive() {
		try {
			receiver.receiveAsync(ioArgs);
		}catch (Exception e) {
			closeAndNotify();
		}
		
	}

	private void closeAndNotify() {
		CloseUtils.close(this);
	}

	@Override
	public void stop() {
		
	}

	@Override
	public void close() throws IOException {
		if(isClose.compareAndSet(false, true)) {
			ReceivePacket packet=packetTemp;
			if(packet!=null) {
				packetTemp=null;
				CloseUtils.close(packet);
			}
		}
	}
	
}
