package com.spr.game.common;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 网络消息包
 */
@Getter
@ToString
public class Packet {

    public Packet(){
        
    }
    public Packet(short packetID, byte[] body) {
        this.packetID = packetID;
        this.body = body;
        this.packetLen = body.length;
    }

    public static final int HEAD_LEN = 14;
    /**
     * 消息id
     */
    @Getter
    @Setter
    private short packetID;

    /**
     * 消息长度
     */
    @Getter
    private int packetLen;

    /**
     * 消息来源id
     */
    @Getter
    @Setter
    private int srcID;

    /**
     * 消息目的id
     */
    @Getter
    @Setter
    private int dstID;

    /**
     * 消息体
     */
    @Getter
    private byte[] body;

    public void setBody(byte[] body)
    {
        this.body = body;
        packetLen = body.length;
    }
}
