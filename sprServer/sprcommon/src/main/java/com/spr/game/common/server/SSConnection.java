package com.spr.game.common.server;

import com.spr.game.common.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @ClassName: NetConnection
 * @Description TODO
 * @Author springhao123
 * @Date 2020/9/11 14:30
 * Version 1.0
 **/
@Log4j2
@Setter
@Getter
@Component
public class SSConnection implements ISession, ISendData {
	/**
	 * 数据缓冲区大小
	 */
	public static final int DATA_BUFFER_SIZE = 64;

	/**
	 * 连接通道
	 */
	protected SocketChannel channel;
	/**
	 * 读写数据的缓冲区大小
	 */
	public static final int RW_BUFFER_SIZE = 1024;

	/**
	 * 是否处于活动
	 */
	protected boolean active;

	/**
	 * 最大缓存数据
	 */
	public static final int MAX_STORED_BYTES = 0x10000;

	/**
	 * 最大发送数据
	 */
	public static final int MAX_SEND_SIZE = 8092;

	/**
	 * 最大buffer长度
	 */
	protected int maxBufSize;

	/**
	 * 输入数据缓冲
	 */
	protected IByteBuf readBuf;

	/**
	 * 输出数据缓冲
	 */
	protected IByteBuf sendBuf;

	/**
	 * 创建时间
	 */
	protected long createdTime;

	/**
	 * 上次接受数据时间
	 */
	protected long lastReceiveTime;

	/**
	 * ping时间
	 */
	protected int pingTime;

	/**
	 * 超时时间
	 */
	protected int timeout;

	/**
	 * 接受数据时的解码器
	 */
	protected IMessageDecoder messageDecoder;

	/**
	 * 发出数据时的编码器
	 */
	protected IMessageEncoder messageEncdoer;

	/**
	 * 对每个连接是否顺序处理接到的请求
	 */
	protected boolean order = true;

	/**
	 * 读取输出的缓冲区
	 */
	protected ByteBuffer writer = null;

	protected String host;

	protected int port;

	/**
	 * 是否继续读数据
	 */
	protected boolean continueReadData = true;

	protected NioWriteDelay nioWritedelay;

	public static class MessageInfo {

		public long time;

		public Packet data;
	}

	public boolean isOrder() {
		return order;
	}


	/**
	 * 附加数据
	 */
	protected Object source;

	/**
	 * 已读取的数据队列
	 */
	protected Deque deque = new Deque(DATA_BUFFER_SIZE);

	protected long id;

	/**
	 * 是否正在处理
	 */
	protected boolean isProcessing = false;

	protected volatile boolean isBusy = false;


	/**
	 * 发出消息
	 */
	public void sendMessage(Packet message) {
		synchronized (sendBuf) {
			messageEncdoer.encode(message, sendBuf);
		}
		if (sendBuf.length() > 4000) {
			flush();
		}
	}

	/**
	 * 立即发出消息
	 */
	public void sendAndFlushMessage(Packet message) {
		synchronized (sendBuf) {
			messageEncdoer.encode(message, sendBuf);
		}
		flush();
	}

	/**
	 * 连接超时检测
	 */
	public void update(long l) {
		if (lastReceiveTime == 0L) {
			lastReceiveTime = l;
		}
		if (pingTime > 0 && l - lastReceiveTime > pingTime) {
		}
		if (timeout > 0 && l - lastReceiveTime > timeout) {
			System.out.println("timeOutClose:" + timeout + ";" + toString());
			close();
		}
	}

	/**
	 * 得到消息时通知
	 */
	public void dispatchMessage(Packet message) {

	}

	/**
	 * 从缓存中发出消息
	 */
	public void flush() {
		synchronized (sendBuf) {
			if (isBusy) {
				return;
			}
			int i = sendBuf.available();
			if (i > 0) {
				int sendlen = sendDataImpl(sendBuf.getRawBytes(), 0, i);
				sendBuf.setReadPos(sendlen);
				sendBuf.pack();
//				sendedDataLength += sendlen;
				if (sendlen < i) {
					isBusy = true;
					nioWritedelay.sendDealy(this, sendBuf);
					return;
				}
			}
			if (sendBuf.length() >= 0x10000) {
				log.error("send data too long,clear.overflow");
				sendBuf.clear();
			}
		}
	}

	/**
	 * 从nio中发出消息
	 */
	/**
	 * 发出数据
	 */
	@Override
	public int sendDataImpl(byte abyte0[], int offset, int len) {
		int total = len;
		if (!active) {
			return len;
		}
		try {
			if (channel.socket().isClosed()) {
				close();
				return len;
			}
		} catch (Exception e) {
			close();
			log.error("[Unexpected close] closed while prepare write data:"
					+ this);
			e.printStackTrace();
		}
		try {
			int limit = 0, n = 0, r = 0;
			writer.clear();
			int i = RW_BUFFER_SIZE;
			for (; len > 0; offset += i, len -= i) {
				if (i > len) {
					i = len;
				}
				writer.put(abyte0, offset, i);
				writer.flip();
				limit = writer.limit();
				r = channel.write(writer);
				n += r;
				writer.clear();
				if (r < limit) {
					return n;
				}
			}
		} catch (Exception e) {
			// active = false;
			close();
			System.err
					.println("[Unexpected close] closed while send data to client:"
							+ this);
			e.printStackTrace();

		}
		return total;
	}

	@Override
	public boolean isIsbusy() {
		return false;
	}

	@Override
	public void setIsbusy(boolean isbusy) {

	}

	@Override
	public void send(Packet packet) {

	}

	/**
	 * 关闭连接
	 */
	@Override
	public void close() {
		if (!active) {
			return;
		}
		active = false;
		Thread.dumpStack();
		try {
			channel.socket().close();
		} catch (IOException ioexception) {
			ioexception.printStackTrace();
		}
		this.dispatchClose();
	}

	@Override
	public boolean isClosed() {
		return false;
	}

	/**
	 * 得到消息时通知
	 */
	public void dispatchClose() {

	}

	/**
	 * 读入数据，发出消息，通知连接的观察者，处理对应的读入数据
	 */
	protected void onDataRead(byte abyte0[], int offset, int length, long l) {
		lastReceiveTime = l;
//		receivedDataLength += length;
		if (!this.continueReadData) {
			log.error(getHost() + "  " + getPort() + " not read data any more");
			return;
		}
		if (messageDecoder != null) {
			readBuf.writeBytes(abyte0, offset, length);
			log.debug("get Data from " + getHost() + " " + getPort() + " data size ");
			Packet message = null;
			while ((message = messageDecoder.decode(readBuf)) != null) {
				synchronized (deque) {
					if (deque.isFull()) {
						log.error(getHost() + "  " + getPort()
								+ " deque is full:" + deque.size() + " id:"
								+ id);
						continue;
					}
					deque.pushTail(message);
				}
//				// 记录前后消息和时间
//				if (info1.data == null) {
//					// first time
//					info1.data = message;
//					info1.time = System.currentTimeMillis();
//				} else {
//					info2.data = info1.data;
//					info2.time = info1.time;
//					info1.data = message;
//					info1.time = System.currentTimeMillis();
//					if (info1.time - info2.time < 10) {
//						log.error("Id1:{}, Id2:{}", info1.data.getPacketID(), info2.data.getPacketID());
//					}
//				}
				if (isOrder()) {
					if (!isProcessing) {
						//isProcessing = true;
						dispatchMessage(message);
					}
				} else {
					isProcessing = true;
					dispatchMessage(message);
				}
				readBuf.pack();
			}
		}
		if (readBuf.available() > maxBufSize) {
			System.out.println("buffer data overflow:" + maxBufSize + ";"
					+ toString());
			synchronized (readBuf) {
				readBuf.clear();
				close();
			}
		}
	}

	/**
	 * 构造函数
	 */
	public SSConnection(SocketChannel socketchannel) {
		maxBufSize = 0x7fffffff;
		readBuf = new ByteBuf(1024);
		sendBuf = new ByteBuf(16384);
		pingTime = 0;
		timeout = 0;
		this.id = System.currentTimeMillis();
		active = true;
		channel = socketchannel;
		Socket socket = socketchannel.socket();
		host = socket.getInetAddress().getHostAddress();
		port = socket.getPort();
		writer = ByteBuffer.allocate(RW_BUFFER_SIZE);
	}

	/**
	 * 添加附加数据
	 */
	@Override
	public void attach(Object ob) {
		this.source = ob;
	}

	/**
	 * 得到附加数据
	 */
	@Override
	public Object attachment() {
		return this.source;
	}

	@Override
	public InetAddress getAddress() {
		return null;
	}

	/**
	 * 取出新的任务
	 */
	private Packet popData() {
		synchronized (deque) {
			if (!deque.isEmpty()) {
				Packet popData = (Packet) (deque.popHead());
//				this.cInfo.data = popData;
//				this.cInfo.time = System.currentTimeMillis();
				return popData;
			}
			isProcessing = false;
			return null;
		}
	}
}
