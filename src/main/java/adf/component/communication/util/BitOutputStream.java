package adf.component.communication.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BitOutputStream extends ByteArrayOutputStream {
	private int buf = 0;
	private final int BUF_SIZE = 8;
	private int cnt = 8;
	private int now_size = 0;

	@Override
	public void reset() {
		super.reset();
		this.buf = 0;
		this.cnt = 8;
		this.now_size = 0;
	}

	@Override
	public synchronized void write(int b) {
		this.writeBits(b, 8);
	}

	public synchronized void writeBits(int value, int len) {
		for (int i = len - 1; i >= 0; i--) {
			this.writeBits(value >> i & 0x1);
		}
	}

	public synchronized void writeBitsWithExistFlag(int value, int len) {
		this.writeBits(1);
		this.writeBits(value, len);
	}

	public synchronized void writeNullFlag() {
		this.writeBits(0);
	}

	private synchronized void writeBits(int value) {
		this.buf = this.buf | (value << --this.cnt);
		now_size++;

		if (cnt == 0) {
			super.write(this.buf);
			this.cnt = this.BUF_SIZE;
			this.buf = 0;
		}
	}

	public synchronized int getBuffer() {
		return buf;
	}

	public synchronized void writeBits(BitOutputStream bos) {
		//書き出し済みバイト列の転記
		byte[] byteArray = bos.toByteArray();
		for (int i = 0, n = byteArray.length; i < n; i++) {
			//int value = (byteArray[i / 8] >>> (7 - i % 8) & 0x1);
			//this.writeBits(value);
			this.write(byteArray[i]);
		}
	}

	public synchronized int size() {
		return now_size;
	}

	public synchronized int getBitBufferCount() {
		return cnt;
	}

	public synchronized byte[] toByteArray() {
		if (cnt != BUF_SIZE) {
			super.write(this.buf);
			this.cnt = this.BUF_SIZE;
			this.buf = 0;
		}
		return super.toByteArray();
	}
}
