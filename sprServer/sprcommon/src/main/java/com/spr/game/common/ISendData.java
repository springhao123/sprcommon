package com.spr.game.common;

/**
 * @ClassName:
 * @Description
 * @Author springhao123
 * @Date 2020/6/7 19:09
 * Version 1.0
 **/
public interface ISendData {
	/**
	 * 从nio中发出消息
	 */
	int sendDataImpl(byte abyte0[], int i, int j);

	/**
	 * 是否忙
	 */
	boolean isIsbusy();

	/**
	 * 设置是否繁忙
	 */
	void setIsbusy(boolean isbusy);

	void close();
}
