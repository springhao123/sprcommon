package com.spr.game.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * @interface: com.spr.game.net.IByteBuf
 * @Description TODO
 * @Author springhao123
 * @Date 2020/6/9 17:08
 * Version 1.0
 **/
public interface IByteBuf extends Serializable {

	void position(int i);

	void readFrom(InputStream inputstream) throws IOException;

	void skipBytes(int i);

	void readFrom(InputStream inputstream, int i) throws IOException;

	int capacity();

	void writeTo(OutputStream outputstream) throws IOException;

	void pack();

	void writeByte(int i);

	int readByte();

	int readUnsignedByte();

	void read(byte abyte0[], int i, int j, int k);

	int getReadPos();

	void setReadPos(int i);

	void write(byte abyte0[], int i, int j, int k);

	void writeChar(char c);

	char readChar();

	byte[] getBytes();

	Object clone();

	void writeAnsiString(String s);

	String readAnsiString();

	int length();

	void writeBoolean(boolean flag);

	boolean readBoolean();

	float readFloat();

	void reset();

	void writeLong(long l);

	void writeShortAnsiString(String s);

	long readLong();

	void writeShort(int i);

	int readShort();

	void writeByteBuf(IByteBuf bytebuffer);

	void writeByteBuf(IByteBuf bytebuffer, int i);

	void writeBytes(byte abyte0[]);

	void writeBytes(byte abyte0[], int i, int j);

	byte[] readBytes(int i);

	int readUnsignedShort();

	String readShortAnsiString();

	int available();

	int getWritePos();

	void setWritePos(int i);

	byte[] getRawBytes();

	void writeUTF(String s);

	String readUTF();

	void clear();

	void writeInt(int i);

	int readInt();

	int position();

	/**
	 * 读出一个指定长度的字节数组
	 */
	byte[] readData();

	/**
	 * 写入一个字节数组，可以为null
	 */
	void writeData(byte[] data);
}
