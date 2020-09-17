package com.spr.game.common;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @interface: com.spr.game.net.ByteBuf
 * @Description 序列化的ByteBuf实现
 * @Author springhao123
 * @Date 2020/6/9 17:08
 * Version 1.0
 **/

@Slf4j
public class ByteBuf implements Cloneable, IByteBuf {

	private int readPos;

	private int writePos;

	private byte[] data;

	private boolean highEndian = true;

	public static final int MAX_DATA_LENGTH = 2457600;

	public ByteBuf(boolean highEndian) {
		this();
		this.highEndian = highEndian;
	}

	@Override
	public void position(int i) {
		this.readPos = (this.writePos = i);
	}

	@Override
	public void readFrom(InputStream inputstream) throws IOException {
		readFrom(inputstream, capacity() - length());
	}

	@Override
	public void skipBytes(int i) {
		this.readPos += i;
	}

	@Override
	public void readFrom(InputStream inputstream, int i) throws IOException {
		ensureCapacity(this.writePos + i);
		for (int j = 0; j < i; j += inputstream.read(this.data, this.writePos + j, i - j)) {

		}
		this.writePos += i;
	}

	@Override
	public int capacity() {
		return this.data.length;
	}

	private void ensureCapacity(int i) {
		if (i > this.data.length) {
			byte[] abyte0 = new byte[i * 3 / 2];
			System.arraycopy(this.data, 0, abyte0, 0, this.writePos);
			this.data = abyte0;
		}
	}

	@Override
	public void writeTo(OutputStream outputstream) throws IOException {
		int i = available();
		for (int j = 0; j < i; j++) {
			outputstream.write(this.data[(this.readPos++)]);
		}
	}

	@Override
	public void pack() {
		if (this.readPos == 0) {
			return;
		}
		int i = available();
		for (int j = 0; j < i; j++) {
			this.data[j] = this.data[(this.readPos++)];
		}
		this.readPos = 0;
		this.writePos = i;
	}

	@Override
	public void writeByte(int i) {
		writeNumber(i, 1);
	}

	@Override
	public int readByte() {
		return this.data[(this.readPos++)];
	}

	@Override
	public int readUnsignedByte() {
		return this.data[(this.readPos++)] & 0xFF;
	}

	@Override
	public void read(byte[] abyte0, int i, int j, int k) {
		System.arraycopy(this.data, k, abyte0, i, j);
	}

	@Override
	public int getReadPos() {
		return this.readPos;
	}

	@Override
	public void setReadPos(int i) {
		this.readPos = i;
	}

	@Override
	public void write(byte[] abyte0, int i, int j, int k) {
		ensureCapacity(k + j);
		System.arraycopy(abyte0, i, this.data, k, j);
	}

	@Override
	public void writeChar(char c) {
		writeNumber(c, 2);
	}

	@Override
	public char readChar() {
		return (char) (int) (readNumber(2) & 0xFFFF);
	}

	private void writeNumber(long l, int i) {
		if (this.highEndian) {
			writeNumberHigh(l, i);
		} else {
			writeNumberLow(l, i);
		}
	}

	private void writeNumberLow(long l, int i) {
		ensureCapacity(this.writePos + i);
		for (int j = 0; j < i; j++) {
			this.data[(this.writePos++)] = ((byte) (int) l);
			l >>= 8;
		}
	}

	private void writeNumberHigh(long l, int i) {
		ensureCapacity(this.writePos + i);
		for (int j = i - 1; j >= 0; j--) {
			this.data[(this.writePos++)] = ((byte) (int) (l >>> (j << 3)));
		}
	}

	private long readNumberHigh(int i) {
		long l = 0L;
		for (int j = i - 1; j >= 0; j--) {
			l |= (this.data[(this.readPos++)] & 0xFF) << (j << 3);
		}
		return l;
	}

	private long readNumberLow(int i) {
		long l = 0L;
		for (int j = 0; j < i; j++) {
			l |= (this.data[(this.readPos++)] & 0xFF) << (j << 3);
		}
		return l;
	}

	private long readNumber(int i) {
		if (this.highEndian) {
			return readNumberHigh(i);
		}
		return readNumberLow(i);
	}

	@Override
	public byte[] getBytes() {
		byte[] abyte0 = new byte[length()];
		System.arraycopy(this.data, 0, abyte0, 0, abyte0.length);
		return abyte0;
	}

	@Override
	public Object clone() {
		ByteBuf bytebuffer = new ByteBuf(this.writePos);
		System.arraycopy(this.data, 0, bytebuffer.data, 0, this.writePos);
		bytebuffer.writePos = this.writePos;
		bytebuffer.readPos = this.readPos;
		return bytebuffer;
	}

	@Override
	public void writeAnsiString(String s) {
		if ((s == null) || (s.length() == 0)) {
			writeShort(0);
		} else {
			if (s.length() > 32767) {
				throw new IllegalArgumentException("string over flow");
			}
			byte[] abyte0 = s.getBytes();
			writeShort(abyte0.length);
			writeBytes(abyte0);
		}
	}

	@Override
	public String readAnsiString() {
		int i = readUnsignedShort();
		if (i == 0) {
			return "";
		}
		byte[] abyte0 = readBytes(i);
		return new String(abyte0);
	}

	@Override
	public int length() {
		return this.writePos;
	}

	@Override
	public void writeBoolean(boolean flag) {
		writeByte(flag ? 1 : 0);
	}

	@Override
	public boolean readBoolean() {
		return readByte() != 0;
	}

	@Override
	public float readFloat() {
		int i = readInt();
		return Float.intBitsToFloat(i);
	}

	@Override
	public void reset() {
		this.readPos = 0;
	}

	@Override
	public void writeLong(long l) {
		writeNumber(l, 8);
	}

	public ByteBuf() {
		this(1024);
	}

	public ByteBuf(int i) {
		if (i > MAX_DATA_LENGTH) {
			throw new IllegalArgumentException("data overflow " + i);
		}
		this.data = new byte[i];
	}

	public ByteBuf(byte[] abyte0) {
		this(abyte0, 0, abyte0.length);
	}

	public ByteBuf(byte[] abyte0, int i, int j) {
		this.data = abyte0;
		this.readPos = i;
		this.writePos = (i + j);
	}

	@Override
	public void writeShortAnsiString(String s) {
		if ((s == null) || (s.length() == 0)) {
			writeByte(0);
		} else {
			byte[] abyte0 = s.getBytes();
			if (abyte0.length > 255) {
				throw new IllegalArgumentException("short string over flow");
			}
			writeByte(abyte0.length);
			writeBytes(abyte0);
		}
	}

	@Override
	public long readLong() {
		return readNumber(8);
	}

	@Override
	public void writeShort(int i) {
		writeNumber(i, 2);
	}

	@Override
	public int readShort() {
		return (short) (int) (readNumber(2) & 0xFFFF);
	}

	@Override
	public void writeByteBuf(IByteBuf bytebuf) {
		writeByteBuf(bytebuf, bytebuf.available());
	}

	@Override
	public void writeByteBuf(IByteBuf bytebuf, int i) {
		ensureCapacity(length() + i);
		byte[] sourceData = bytebuf.getRawBytes();
		int sourceReadPos = bytebuf.getReadPos();

		System.arraycopy(sourceData, sourceReadPos, this.data, this.writePos, i);
		setWritePos(this.writePos + i);
		bytebuf.setReadPos(sourceReadPos + i);
	}

	@Override
	public void writeBytes(byte[] abyte0) {
		writeBytes(abyte0, 0, abyte0.length);
	}

	@Override
	public byte[] readData() {
		int len = readInt();
		if (len < 0) {
			return null;
		}
		if (len > MAX_DATA_LENGTH) {
			throw new IllegalArgumentException(this + " readData, data overflow:" + len);
		}
		return readBytes(len);
	}

	@Override
	public void writeData(byte[] data) {
		writeData(data, 0, data != null ? data.length : 0);
	}

	public void writeData(byte[] data, int pos, int len) {
		if (data == null) {
			writeInt(0);
			return;
		}
		writeInt(len);
		writeBytes(data);
	}

	@Override
	public void writeBytes(byte[] abyte0, int i, int j) {
		ensureCapacity(this.writePos + j);
		for (int k = 0; k < j; k++) {
			this.data[(this.writePos++)] = abyte0[(i++)];
		}
	}

	@Override
	public byte[] readBytes(int i) {
		byte[] abyte0 = new byte[i];
		for (int j = 0; j < i; j++) {
			abyte0[j] = this.data[(this.readPos++)];
		}
		return abyte0;
	}

	@Override
	public int readUnsignedShort() {
		return (int) (readNumber(2) & 0xFFFF);
	}

	@Override
	public String readShortAnsiString() {
		int i = readUnsignedByte();
		if (i == 0) {
			return "";
		}
		byte[] abyte0 = readBytes(i);
		return new String(abyte0);
	}

	@Override
	public int available() {
		return this.writePos - this.readPos;
	}

	@Override
	public String toString() {
		return new String(this.data, 0, this.writePos);
	}

	@Override
	public int getWritePos() {
		return this.writePos;
	}

	@Override
	public void setWritePos(int i) {
		this.writePos = i;
	}

	@Override
	public byte[] getRawBytes() {
		return this.data;
	}

	@Override
	public void writeUTF(String s) {
		if (s == null) {
			s = "";
		}
		int i = s.length();
		int j = 0;
		for (int k = 0; k < i; k++) {
			char c = s.charAt(k);
			if (c < '') {
				j++;
			} else if (c > '߿') {
				j += 3;
			} else {
				j += 2;
			}
		}
		if (j > 65535) {
			throw new IllegalArgumentException("the string is too long:" + i);
		}
		ensureCapacity(this.writePos + j + 2);
		writeShort(j);
		for (int l = 0; l < i; l++) {
			char c1 = s.charAt(l);
			if (c1 < '') {
				this.data[(this.writePos++)] = ((byte) c1);
			} else if (c1 > '߿') {
				this.data[(this.writePos++)] = ((byte) (0xE0 | c1 >> '\f' & 0xF));
				this.data[(this.writePos++)] = ((byte) (0x80 | c1 >> '\006' & 0x3F));
				this.data[(this.writePos++)] = ((byte) (0x80 | c1 & 0x3F));
			} else {
				this.data[(this.writePos++)] = ((byte) (0xC0 | c1 >> '\006' & 0x1F));
				this.data[(this.writePos++)] = ((byte) (0x80 | c1 & 0x3F));
			}
		}
	}

	@Override
	public String readUTF() {
		int i = readUnsignedShort();
		if (i == 0) {
			return "";
		}
		char[] ac = new char[i];
		int j = 0;
		for (int l = this.readPos + i; this.readPos < l; ) {
			int k = this.data[(this.readPos++)] & 0xFF;
			if (k < 127) {
				ac[(j++)] = ((char) k);
			} else if (k >> 5 == 7) {
				byte byte0 = this.data[(this.readPos++)];
				byte byte2 = this.data[(this.readPos++)];
				ac[(j++)] = ((char) ((k & 0xF) << 12 | (byte0 & 0x3F) << 6 | byte2 & 0x3F));
			} else {
				byte byte1 = this.data[(this.readPos++)];
				ac[(j++)] = ((char) ((k & 0x1F) << 6 | byte1 & 0x3F));
			}
		}

		return new String(ac, 0, j);
	}

	@Override
	public void clear() {
		this.writePos = (this.readPos = 0);
	}

	@Override
	public void writeInt(int i) {
		writeNumber(i, 4);
	}

	@Override
	public int readInt() {
		return (int) (readNumber(4) & 0xFFFFFFFF);
	}

	@Override
	public int position() {
		return this.readPos;
	}

	public boolean isHighEndian() {
		return this.highEndian;
	}

	public void setHighEndian(boolean highEndian) {
		this.highEndian = highEndian;
	}
}
