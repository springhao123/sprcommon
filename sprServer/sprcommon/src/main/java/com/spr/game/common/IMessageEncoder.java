package com.spr.game.common;

/**
 * @ClassName:
 * @Description 发出数据时编码
 * @Author springhao123
 * @Date 2020/6/7 19:09
 * Version 1.0
 **/


public interface IMessageEncoder {
	/**
	 * 编码数据，然后发出
	 *
	 * @param message 发出的消息
	 * @param bytebuf 发出消息缓存堆栈
	 */
	void encode(Packet message, IByteBuf bytebuf);
}
