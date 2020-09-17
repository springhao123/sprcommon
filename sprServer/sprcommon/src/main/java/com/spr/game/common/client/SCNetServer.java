package com.spr.game.common.client;

import com.spr.game.common.NioWriteDelay;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * @ClassName: NetServer
 * @Description TODO
 * @Author springhao123
 * @Date 2020/9/11 14:23
 * Version 1.0
 **/
@Log4j2
@Component
public class SCNetServer implements Runnable {
	/**
	 * 服务器连接通道
	 */
	protected ServerSocketChannel serverChannel;

	/**
	 * 服务器对应的socket
	 */
	protected ServerSocket serverSocket;

	protected Selector selector;

	protected SelectionKey serverKey;

	/**
	 * 是否已经启动
	 */
	protected boolean running;

	/**
	 * 数据存放字节数组
	 */
	protected byte data[];

	/**
	 * nio数据堆栈
	 */
	protected ByteBuffer buffer;
	/**
	 * 所有的连接数组
	 */
	protected ArrayList<SCConnection> connectionList;

	public static final int SEND_BUFFER_SIZE = 128 * 1024;

	protected NioWriteDelay nioWritedelay;

	/**
	 * 游戏启动端口
	 */
	@Setter
	@Getter
	private int port;

	/**
	 * 游戏启动绑定ip
	 */
	@Setter
	@Getter
	private String host;


	/**
	 * 最大连接数
	 */
	@Setter
	@Getter
	private int maxConnections;


	/**
	 * 对每个连接是否顺序处理接到的请求
	 */
	@Setter
	@Getter
	protected boolean order = false;


	/**
	 * 关闭服务器
	 */
	public void stop() {

	}

	public SCNetServer(int bufferSize, int clientSize, boolean isServer) {
		data = new byte[bufferSize];
		nioWritedelay = new NioWriteDelay();
		buffer = ByteBuffer.wrap(data, 0, data.length);
		connectionList = new ArrayList<SCConnection>(clientSize);
	}

	/**
	 * 启动服务
	 */
	public boolean start() {
		boolean flag = bindServer();
		if (!flag) {
			return false;
		} else {
			(new Thread(this, "NIONetServerThread")).start();
			serverOpened();
			return true;
		}
	}


	/**
	 * 开启服务器，绑定服务端口
	 */
	private boolean bindServer() {
		try {
			serverChannel = ServerSocketChannel.open();
			serverSocket = serverChannel.socket();
			serverSocket.setReceiveBufferSize(512);
			serverSocket.setPerformancePreferences(0, 2, 1);
			serverSocket.bind(new InetSocketAddress(getAddress(), getPort()));
			serverChannel.configureBlocking(false);
			selector = Selector.open();
			serverKey = serverChannel.register(selector, 16);
			System.out.println("start server :" + getAddress().getHostAddress()
					+ " " + getPort());
			return true;
		} catch (IOException ioexception) {
			ioexception.printStackTrace();
		}
		return false;
	}

	/**
	 * 服务启动
	 */
	protected void serverOpened() {

	}

	/**
	 * 获得连接服务器的地址
	 */
	public InetAddress getAddress() {
		InetAddress address = null;
		try {
			if (host == null) {
				address = InetAddress.getByName("0.0.0.0");
			} else {
				address = InetAddress.getByName(host);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return address;
	}

	/**
	 * 启动服务器启动后，一直定时检测各个连接
	 */
	@Override
	public void run() {
		running = true;
		int i = 0;
		// 定时检测连接是否处于活动状态
		while (running) {
			long l = System.currentTimeMillis();
			int i1 = connectionList.size();
			SCConnection nioconnection;
			for (int j1 = 0; j1 < i1; j1++) {
				nioconnection = connectionList.get(j1);
				if (nioconnection.isActive()) {
					nioconnection.update(l);
				} else {
					nioconnection.close();
					connectionList.remove(j1--);
					i1--;
				}
			}
			try {
				i = selector.selectNow();
				if (i > 0) {
					processSelection(l);
				}
			} catch (Exception ioexception) {
				ioexception.printStackTrace();
			}

			try {
				Thread.sleep(5);// 休眠5毫秒
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 处理select响应
	 */
	protected void processSelection(long l) {
		Set<SelectionKey> set = selector.selectedKeys();
		Iterator<SelectionKey> iterator = set.iterator();
		while (iterator.hasNext()) {
			SelectionKey selectionkey = iterator.next();
			iterator.remove();
			// 连接进入
			if (selectionkey == serverKey) {
				if (selectionkey.isAcceptable()) {
					try {
						SocketChannel socketchannel = serverChannel.accept();
						socketchannel.configureBlocking(false);
						socketchannel.socket().setSendBufferSize(
								SEND_BUFFER_SIZE);
						SelectionKey selectionkey1 = socketchannel.register(selector, 1);
						// 新建一个连接对象
						SCConnection nioconnection2 = new SCConnection(socketchannel);
						nioconnection2.setNioWritedelay(nioWritedelay);
						// 设置连接时间
						nioconnection2.setCreatedTime(System
								.currentTimeMillis());
						selectionkey1.attach(nioconnection2);
						// 添加到连接列表
						connectionList.add(nioconnection2);
						log.debug(nioconnection2.getHost() + " "
								+ nioconnection2.getPort() + " come in");
					} catch (IOException ioexception) {
						try {
							Thread.sleep(100);
						} catch (Exception e) {
							e.printStackTrace();
						}
						ioexception.printStackTrace();
					}
				}
			} else if (!selectionkey.isValid()) {
				SCConnection nioconnection = (SCConnection) selectionkey
						.attachment();
				if (nioconnection != null) {
					log.debug("client close the connect "
							+ nioconnection.toString());
					nioconnection.close();
				}
			} else if (selectionkey.isReadable()) {
				// 有数据读
				SCConnection nioconnection1 = (SCConnection) selectionkey.attachment();
				try {
					SocketChannel socketchannel1 = (SocketChannel) selectionkey
							.channel();
					buffer.clear();
					int i = socketchannel1.read(buffer);
					if (i > 0) {
						nioconnection1.onDataRead(data, 0, i, l);
					}
				} catch (Exception ioexception1) {
					log.debug("[Unexpected close] closed while reading data:"
							+ nioconnection1, ioexception1);
					ioexception1.printStackTrace();
					// 有错误关闭
					nioconnection1.close();
					selectionkey.cancel();
				}
			}
		}
	}
}
