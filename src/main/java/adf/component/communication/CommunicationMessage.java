package adf.component.communication;

import javax.annotation.Nonnull;

import adf.component.communication.util.BitOutputStream;

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
