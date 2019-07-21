package impl;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import core.CloseUtils;
import core.IoProvider;

/**
 * 该类通过实现IoProvider来提供注册，解绑的方法
 * @author Administrator
 *
 */
public class IoSelectorProvider implements IoProvider{
	
	private AtomicBoolean isClose=new AtomicBoolean(false);
	//是否正处于某个状态(正在输入/正在输出)
	private AtomicBoolean isReqInput=new AtomicBoolean(false);
	private AtomicBoolean isReqOutput=new AtomicBoolean(false);
	
	//将读写的selector分开是为后来seletor不用做多次判断(判断可读，然后判断可写)
	private final Selector readSelector;
	private final Selector writeSelector;
	
	private HashMap<SelectionKey, Runnable> inputCallBackMap=new HashMap<>();
	private HashMap<SelectionKey, Runnable> outputCallBackMap=new HashMap<>();
	
	private final ExecutorService inputHandlePool;
	private final ExecutorService outputHandlePool;

	public IoSelectorProvider() throws IOException{
		readSelector=Selector.open();
		writeSelector=Selector.open();
		inputHandlePool=Executors.newFixedThreadPool(4, 
				new IoProviderThreadFactory("IoProvider-input-Thread"));
		outputHandlePool=Executors.newFixedThreadPool(4,
				new IoProviderThreadFactory("IoProvider-output-Thread"));
		//开始对输入输出的监听
		startRead();
		startWrite();
	}
	
	/**
	 * 开启读取监听
	 */
	private void startRead() {
		Thread thread=new Thread("Clink IoSelectorProvider ReadSelector") {
			public void run() {
				try {
					while(!isClose.get()) {
						if(readSelector.select()==0) {
							waitSelector(isReqInput);
							continue;
						}
						Set<SelectionKey> keys=readSelector.selectedKeys();
						for (SelectionKey selectionKey : keys) {
							if(selectionKey.isValid()) {
								handleSelection(selectionKey,SelectionKey.OP_READ,inputCallBackMap,inputHandlePool);
							}
						}
						keys.clear();
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
			}

		
		};
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
	}
	
	/**
	 * 开启输出监听
	 */
	private void startWrite() {
		Thread thread=new Thread("Clink IoSelectorProvider WriteSelector") {
			public void run() {
				try {
					while(!isClose.get()) {
						if(writeSelector.select()==0) {
							waitSelector(isReqOutput);
							continue;
						}
						Set<SelectionKey> keys=writeSelector.selectedKeys();
						for (SelectionKey selectionKey : keys) {
							if(selectionKey.isValid()) {
								handleSelection(selectionKey,SelectionKey.OP_WRITE,outputCallBackMap,outputHandlePool);
							}
						}
						keys.clear();
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
	}
	
	/**
	 * 等待操作，如果某个Selector的通道正在进行注册操作，就等待注册操作完成，不再执行selector的select操作
	 * @param locker
	 */
	private static void waitSelector(AtomicBoolean locker) {
		synchronized (locker) {
			if(locker.get()) {
				try {
					locker.wait();
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 进行注册操作
	 * @param channel
	 * @param selector
	 * @param registerOps
	 * @param locker
	 * @param map
	 * @param runnable
	 * @return
	 */
	private static SelectionKey registerSelection(SocketChannel channel,
			Selector selector,
			int registerOps,
			AtomicBoolean locker,
			HashMap<SelectionKey, Runnable> map,
			Runnable runnable) {
		synchronized (locker) {
			//设置锁定状态
			locker.set(true);
			try { 
				//唤醒当前selector，使其不处于select的状态
				selector.wakeup();
				SelectionKey key=null;
				//查询是否已经注册过
				if(channel.isRegistered()) {
					key=channel.keyFor(selector);
					if(key!=null) {
						//注册操作
						key.interestOps(key.readyOps()|registerOps);
					}
				}
				if(key==null) {
					//注册selector，得到key
					key=channel.register(selector, registerOps);
					//注册回调
					map.put(key, runnable);
				}
				return key;
			}catch (Exception e) {
				e.printStackTrace();
				return null;
			}finally {
				//接触锁定
				locker.set(false);
				try {
					locker.notify();
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 解除注册
	 * @param channel
	 * @param selector
	 * @param registerOps
	 * @param locker
	 * @param map
	 * @param runnable
	 */
	private static void unRegisterSelection(SocketChannel channel,
			Selector selector,
			HashMap<SelectionKey, Runnable> map) {
		if(channel.isRegistered()) {
			SelectionKey key=channel.keyFor(selector);
			if(key!=null) {
				//cancel方法时取消所有的监听，因为这里读和写用的是不同的selector，所以可以使用cancel
				key.cancel();
				map.remove(key);
				selector.wakeup();
			}
		}
	}
	
	@Override
	public void close() throws IOException {
		if(isClose.compareAndSet(false, true)) {
			inputHandlePool.shutdown();
			outputHandlePool.shutdown();
			
			outputCallBackMap.clear();
			inputCallBackMap.clear();
			
			readSelector.wakeup();
			writeSelector.wakeup();
			CloseUtils.close(readSelector,writeSelector);
		}
	}

	@Override
	public boolean registerInput(SocketChannel channel, HandleInputCallback callback) {
		return registerSelection(channel, readSelector, SelectionKey.OP_READ, isReqInput, inputCallBackMap, callback)!=null;
	}

	@Override
	public boolean registerOutput(SocketChannel channel, HandleOutputCallback callback) {
		return registerSelection(channel, writeSelector, SelectionKey.OP_WRITE, isReqOutput, outputCallBackMap, callback)!=null;

	}

	@Override
	public void unRegisterInput(SocketChannel channel) {
		unRegisterSelection(channel,readSelector,inputCallBackMap);
	}

	@Override
	public void unRegisterOutput(SocketChannel channel) {
		unRegisterSelection(channel,writeSelector,outputCallBackMap);
	}
	
	private void handleSelection(SelectionKey key, int keyOps,
			HashMap<SelectionKey, Runnable> map, ExecutorService pool) {
		//重点，先取消注册
		//之所以可以同时注册多个操作，是因为注册操作是按位操作，取消注册只需要将相应位置0就可以了
		key.interestOps(key.readyOps() & ~keyOps);//这里以与运算的方式取消注册
		
		Runnable runnable=null;
		runnable=map.get(key);
		if(runnable!=null&&!pool.isShutdown()) {
			pool.execute(runnable);
		}
		
	}
	
	static class IoProviderThreadFactory implements ThreadFactory {
	        private static final AtomicInteger poolNumber = new AtomicInteger(1);
	        private final ThreadGroup group;
	        private final AtomicInteger threadNumber = new AtomicInteger(1);
	        private final String namePrefix;

	        IoProviderThreadFactory(String namePrefix) {
	            SecurityManager s = System.getSecurityManager();
	            group = (s != null) ? s.getThreadGroup() :
	                                  Thread.currentThread().getThreadGroup();
	            this.namePrefix = namePrefix;
	        }

	        public Thread newThread(Runnable r) {
	            Thread t = new Thread(group, r,
	                                  namePrefix + threadNumber.getAndIncrement(),
	                                  0);
	            if (t.isDaemon())
	                t.setDaemon(false);
	            if (t.getPriority() != Thread.NORM_PRIORITY)
	                t.setPriority(Thread.NORM_PRIORITY);
	            return t;
	        }
	    }
}
