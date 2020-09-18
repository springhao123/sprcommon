package com.spr.game.common.server.coder;
/**
 * @ClassName:
 * @Description 发出数据时编码
 * @Author springhao123
 * @Date 2020/6/7 19:09
 * Version 1.0
 **/

import com.spr.game.common.IByteBuf;
import com.spr.game.common.IMessageDecoder;
import com.spr.game.common.Packet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Slf4j
public class MessageDecoder implements IMessageDecoder {

//	@Value("${packet.encrypt.enable}")
	private boolean isKeyEnable = true;

//	@Value("${packet.encrypt.key}")
	private String netKeyStr = "PS4->XBOX1@Switch";


	private long recordTmp = 0;

	@Override
	public Packet decode(IByteBuf bytebuffer) {
		if (bytebuffer.available() < 2) {
			return null;
		}
		int postion = bytebuffer.getReadPos();
		short packetId = (short) bytebuffer.readShort();

		int packetLength = bytebuffer.readInt();
		// 粘包
		if (bytebuffer.available() < packetLength) {
			bytebuffer.setReadPos(postion);
			return null;
		}
		bytebuffer.setReadPos(postion + 2);
		byte[] body = bytebuffer.readData();
		recordTmp += 2;
		recordTmp += body.length;
//		if (isKeyEnable) {
//			CodeKit.coding(body, netKeyStr);
//		}
		Packet packet = new Packet(packetId, body);
		log.info("Receive packId:{} , packLength:{}", packetId, body.length);
		log.info("lengthTmp::" + recordTmp);
		return packet;
	}

}