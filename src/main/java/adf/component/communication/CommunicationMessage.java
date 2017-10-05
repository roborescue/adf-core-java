package adf.component.communication;

import adf.component.communication.util.BitOutputStream;

import javax.annotation.Nonnull;

abstract public class CommunicationMessage {
	private boolean isRadio;

	public CommunicationMessage(boolean isRadio) {
		this.isRadio = isRadio;
	}

	public boolean isRadio() {
		return this.isRadio;
	}

	abstract public int getByteArraySize();

	@Nonnull
	abstract public byte[] toByteArray();

	@Nonnull
	abstract public BitOutputStream toBitOutputStream();

	@Nonnull
	abstract public String getCheckKey();
}
