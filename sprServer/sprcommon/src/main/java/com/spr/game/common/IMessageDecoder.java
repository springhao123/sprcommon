package com.spr.game.common;

/**
 * @ClassName:
 * @Description 接数据时编码
 * @Author springhao123
 * @Date 2020/6/7 19:09
 * Version 1.0
 **/

public interface IMessageDecoder {
	Packet decode(IByteBuf bytebuf);
}
