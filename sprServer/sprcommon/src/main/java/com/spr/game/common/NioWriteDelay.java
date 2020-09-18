package com.spr.game.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName:
 * @Description NioWriteDelay
 * @Author springhao123
 * @Date 2020/6/7 19:09
 * Version 1.0
 **/
@Slf4j
@Component
@Scope("prototype")
public class NioWriteDelay {

	protected ExecutorService executorService;

	protected ScheduledThreadPoolExecutor stpe;

	public static int MAX_SEND = 256;

	//延迟多久以后再进行发送 毫秒
	public final int DelayTimeMILLIS =100;

	public NioWriteDelay(int pool, int thread) {
		executorService = Executors.newFixedThreadPool(pool);
		stpe = new ScheduledThreadPoolExecutor(thread, new ThreadFactory() {
			private final AtomicInteger threadNumber = new AtomicInteger(1);
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("Timermanager-nioWrite-" + threadNumber.getAndIncrement());
				return t;
			}
		});
		stpe.setMaximumPoolSize(1);
		stpe.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
	}

	public NioWriteDelay() {
		executorService = Executors.newFixedThreadPool(1);
		stpe = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
			private final AtomicInteger threadNumber = new AtomicInteger(1);
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("Timermanager-nioWrite-" + threadNumber.getAndIncrement());
				return t;
			}
		});
		stpe.setMaximumPoolSize(1);
		stpe.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
	}

	protected ScheduledFuture<?> schedule(Runnable r, long delay) {
		return stpe.schedule(r, delay, TimeUnit.MILLISECONDS);
	}

	public void sendDealy(ISendData net, IByteBuf data) {
		log.debug("send delay start ");
		addWork(new SendWork(net, data));
	}

	protected void addWork(Runnable work) {
		executorService.execute(work);
	}

	public class SendWork implements Runnable {

		protected ISendData net;

		protected IByteBuf data;

		protected int sendCount;

		protected SendWork(ISendData net, IByteBuf data) {
			this.net = net;
			this.data = data;
			sendCount = 0;
		}

		@Override
		public void run() {
			try {
				synchronized (data) {
					int i = data.available();
					if (i == 0) {
						net.setIsbusy(false);
						return;
					}
					log.debug("send delay sendCount :" + sendCount + "  left " + i);
					int sendlen = this.net.sendDataImpl(data.getRawBytes(), 0, i);
					data.setReadPos(sendlen);
					data.pack();
					if (sendlen == i) {
						net.setIsbusy(false);
						return;
					}
					sendCount++;
					if (sendCount > MAX_SEND) {
						log.error("send overflow ,so close");
						net.close();
						net.setIsbusy(false);
						return;
					}
				}
				schedule(this, DelayTimeMILLIS);// 未刷新完缓冲区，100毫秒后继续
			} catch (Exception e) {
				net.setIsbusy(false);
				net.close();
				e.printStackTrace();
			}
		}
	}
}
