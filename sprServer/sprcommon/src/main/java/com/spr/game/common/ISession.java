package com.spr.game.common;

import java.net.InetAddress;

/**
 * @InterfaceName: ISession
 * @Description TODO
 * @Author zhangchunhui
 * @Date 2018/11/6 18:13
 * Version 1.0
 **/
public interface ISession {

	/**
	 * 发出数据
	 */
	void send(Packet packet);

	/**
	 * 关闭连接
	 */
	void close();

	/**
	 * 是否已经关闭
	 */
	boolean isClosed();

	/**
	 * 附加数据
	 */
	void attach(Object ob);

	/**
	 * 获得附加数据
	 */
	Object attachment();

	/**
	 * 得到连接的地址
	 */
	InetAddress getAddress();

	/**
	 * 得到端口
	 */
	int getPort();
}
