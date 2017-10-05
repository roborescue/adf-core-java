package adf.agent.communication.standard.bundle;


import adf.component.communication.util.BitOutputStream;
import adf.component.communication.util.BitStreamReader;

import javax.annotation.Nonnull;

public class MessageDummy extends StandardMessage {
	final private int SIZE_TEST = 32;
	private int dummyTest;

	public MessageDummy(boolean isRadio, int test) {
		this(isRadio, StandardMessagePriority.NORMAL, test);
	}

	public MessageDummy(boolean isRadio, StandardMessagePriority sendingPriority, int test) {
		super(isRadio, sendingPriority);
		dummyTest = test;
	}

	public MessageDummy(boolean isRadio, int from, int ttl, @Nonnull BitStreamReader bitStreamReader) {
		super(isRadio, from, ttl, bitStreamReader);
		dummyTest = bitStreamReader.getBits(SIZE_TEST);
	}

	public int getValue() {
		return this.dummyTest;
	}

	@Override
	public int getByteArraySize() {
		return toBitOutputStream().size();
	}

	@Override
	@Nonnull
	public byte[] toByteArray() {
		return this.toBitOutputStream().toByteArray();
	}

	@Override
	@Nonnull
	public BitOutputStream toBitOutputStream() {
		BitOutputStream bitOutputStream = new BitOutputStream();
		bitOutputStream.writeBits(dummyTest, SIZE_TEST);
		return bitOutputStream;
	}

	@Override
	@Nonnull
	public String getCheckKey() {
		return getClass().getCanonicalName() + " > test:" + this.getValue();
	}
}
