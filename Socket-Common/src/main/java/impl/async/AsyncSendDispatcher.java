package impl.async;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import core.CloseUtils;
import core.IoArgs;
import core.SendDispatcher;
import core.SendPacket;
import core.Sender;

public class AsyncSendDispatcher implements SendDispatcher{
	
	private AtomicBoolean isClose=new AtomicBoolean(false);
	private final Sender sender;
	private final Queue<SendPacket> queue=new ConcurrentLinkedQueue<>();
	private final AtomicBoolean isSending=new AtomicBoolean(false);
	private IoArgs ioArgs=new IoArgs();
	private SendPacket packetTemp;
	//由于IoArgs的容量仅为256，packet中的数据可能远远大于这个值，所以我们需要维护一个数据的中长度和已发送的数据数量(也就是一个数据的指针)
	private int total;
	private int position;
	
	public AsyncSendDispatcher(Sender sender) {
		this.sender=sender;
	}

	@Override
	public void send(SendPacket packet) {
		queue.offer(packet);
		if(isSending.compareAndSet(false, true)) {
			sendNextPacket();
		}
	}

	@Override
	public void cancel(SendPacket packet) {
		
	}
	
	private SendPacket takePacket() {
		SendPacket sendPacket=queue.poll();
		if(sendPacket!=null && sendPacket.isCancel()) {
			//该消息已取消，不能发送，所以递归获取下一条
			return takePacket();
		}
		return sendPacket;
	}
	
	private void sendNextPacket() {
		SendPacket temp=packetTemp;
		if(temp!=null) {
			CloseUtils.close(temp);
		}
		SendPacket packet=packetTemp=takePacket();
		if(packet==null) {
			//队列中已经没有要发送的数据了，取消发送
			isSending.set(false);
			return;
		}
		
		total=packet.length();
		position=0;
		//System.out.println("sendCurrentPacket");
		sendCurrentPacket();
	}
	
	private void sendCurrentPacket() {
		IoArgs args=ioArgs;
		//开始，清理
		args.startWriting();
		System.out.println("position :"+position+" total :"+total);
		if(position>=total) {
			sendNextPacket();
			return;
		}else if(position==0) {
			//首包，需要携带长度信息
			args.writeLength(total);
		}
		byte[] bytes=packetTemp.open();
		//把bytes写入到IoArgs中
		int count=args.readForm(bytes, position);
		position+=count;
		//完成封装
		args.finishWriting();
		try {
			//System.out.println("sending "+args);
			sender.sendAsync(args, ioArgsEventListener);
		}catch (Exception e) {
			e.printStackTrace();
			closeAndNotify();
		}
	}
	
	private void closeAndNotify() {
		CloseUtils.close(this);
	}

	private final IoArgs.IoArgsEventListener ioArgsEventListener=new IoArgs.IoArgsEventListener() {
		
		@Override
		public void onStart(IoArgs args) {
			
		}
		
		@Override
		public void onCompleted(IoArgs args) {
			//继续发送当前包
			sendCurrentPacket();
		}
	};

	@Override
	public void close() throws IOException {
		if(isClose.compareAndSet(false, true)) {
			isSending.set(false);
			SendPacket packet=packetTemp;
			if(packet!=null) {
				packetTemp=null;
				CloseUtils.close(packet);
			}
		}
	}
}
