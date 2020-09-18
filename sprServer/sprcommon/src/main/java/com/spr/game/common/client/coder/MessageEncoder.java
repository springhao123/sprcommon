package com.spr.game.common.client.coder;
/**
 * @ClassName:
 * @Description 发出数据时编码
 * @Author springhao123
 * @Date 2020/6/7 19:09
 * Version 1.0
 **/

import com.spr.game.common.IByteBuf;
import com.spr.game.common.IMessageEncoder;
import com.spr.game.common.Packet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
public class MessageEncoder implements IMessageEncoder {

//	@Value("${packet.encrypt.enable}")
	private boolean isKeyEnable = true;

//	@Value("${packet.encrypt.key}")
	private String netKeyStr = "PS4->XBOX1@Switch";

	@Override
	public void encode(Packet message, IByteBuf bytebuffer) {
		short packetID = message.getPacketID();
		byte[] body = message.getBody();
		int packetLen = body.length;
		bytebuffer.writeShort(packetID);
		bytebuffer.writeInt(packetLen);
//		if (isKeyEnable) {
//			CodeKit.coding(body, netKeyStr);
//		}
		bytebuffer.writeBytes(body);
		log.debug("Send packId:{} , packLength:{}", packetID, packetLen);
	}
}
