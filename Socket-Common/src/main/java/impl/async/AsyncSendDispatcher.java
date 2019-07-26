package impl.async;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import core.CloseUtils;
import core.IoArgs;
import core.SendDispatcher;
import core.SendPacket;
import core.Sender;

public class AsyncSendDispatcher implements SendDispatcher, IoArgs.IoArgsEventProcessor {
	
	private AtomicBoolean isClose=new AtomicBoolean(false);
	private final Sender sender;
	private final Queue<SendPacket> queue=new ConcurrentLinkedQueue<>();
	private final AtomicBoolean isSending=new AtomicBoolean(false);
	private IoArgs ioArgs=new IoArgs();
	private SendPacket<?> packetTemp;
	private ReadableByteChannel packetChannel;
	//由于IoArgs的容量仅为256，packet中的数据可能远远大于这个值，所以我们需要维护一个数据的中长度和已发送的数据数量(也就是一个数据的指针)
	private long total;
	private long position;
	
	public AsyncSendDispatcher(Sender sender) {
		this.sender=sender;
		sender.setSendListener(this);
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
		if(position>=total) {
			completePacket(position==total);
			sendNextPacket();
			return;
		}
		try {
			sender.postSendAsync();
		}catch (Exception e) {
			e.printStackTrace();
			closeAndNotify();
		}
	}

	/**
	 * 完成packet的发送
	 * @param isSuccessed 是否成功完成，有可能被取消
	 */
	private void completePacket(boolean isSuccessed){
		SendPacket packet=packetTemp;
		if(packet==null){
			return;
		}else {
			CloseUtils.close(packet, packetChannel);
			packetTemp = null;
			packetChannel = null;
			position = 0;
			total=0;
		}
	}

	private void closeAndNotify() {
		CloseUtils.close(this);
	}

	@Override
	public void close() throws IOException {
		if(isClose.compareAndSet(false, true)) {
			isSending.set(false);
			//异常操作导致的关闭
			completePacket(false);
		}
	}

	@Override
	public IoArgs provideIoArgs() {
		IoArgs args=ioArgs;
		if(packetChannel==null){
			packetChannel= Channels.newChannel(packetTemp.open());
			args.setLimit(4);
			//args.writeLength((int)packetTemp.length());
		}else{
			args.setLimit((int)Math.min(args.capacity(),total-position));
			try {
				int count=args.readForm(packetChannel);
				position+=count;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return ioArgs;
	}

	@Override
	public void onConsumeFailed(IoArgs args, Exception e) {
		e.printStackTrace();
	}

	@Override
	public void onConsumeCompleted(IoArgs args) {
		//继续发送当前包
		sendCurrentPacket();
	}
}
