package adf.agent.communication.standard.bundle;

import adf.component.communication.CommunicationMessage;
import rescuecore2.worldmodel.EntityID;

import javax.annotation.Nonnull;

abstract public class StandardMessage extends CommunicationMessage {
	int rawSenderID = -1;
	EntityID mySenderID;
	int ttl = -1;
	StandardMessagePriority sendingPriority = StandardMessagePriority.NORMAL;

	public StandardMessage(boolean isRadio, StandardMessagePriority sendingPriority) {
		super(isRadio);
		this.sendingPriority = sendingPriority;
	}

	public StandardMessage(boolean isRadio, int senderID, int ttl, adf.component.communication.util.BitStreamReader bsr) {
		super(isRadio);
		this.rawSenderID = senderID;
		this.ttl = ttl;
	}

	@Nonnull
	public EntityID getSenderID() {
		if (mySenderID == null) {
			mySenderID = new EntityID(rawSenderID);
		}
		return mySenderID;
	}

	public int getTTL() {
		return this.ttl;
	}

	public StandardMessagePriority getSendingPriority() {
		return this.sendingPriority;
	}

	protected int getBitSize(int value) {
		if ((value & 0xFFFF0000) != 0) {
			if ((value & 0xFF000000) != 0) {
				if ((value & 0xF0000000) != 0) {
					return (value & 0xC0000000) != 0 ? (value & 0x80000000) != 0 ? 32 : 31 : (value & 0x20000000) != 0 ? 30 : 29;
				} else { //0x0F000000
					return (value & 0x0C000000) != 0 ? (value & 0x08000000) != 0 ? 28 : 27 : (value & 0x02000000) != 0 ? 26 : 25;
				}
			} else { //0x00FF0000
				if ((value & 0x00F00000) != 0) {
					return (value & 0x00C00000) != 0 ? (value & 0x00800000) != 0 ? 24 : 23 : (value & 0x00200000) != 0 ? 22 : 21;
				} else { //0x000F0000
					return (value & 0x000C0000) == 0 ? (value & 0x00020000) != 0 ? 18 : 17 : (value & 0x00080000) != 0 ? 20 : 19;
				}
			}
		} else { //0x0000FFFF
			if ((value & 0x0000FF00) != 0) {
				if ((value & 0x0000F000) != 0) {
					return (value & 0x0000C000) != 0 ? (value & 0x00008000) != 0 ? 16 : 15 : (value & 0x00002000) != 0 ? 14 : 13;
				} else {
					return (value & 0x00000C00) != 0 ? (value & 0x00000800) != 0 ? 12 : 11 : (value & 0x00000200) != 0 ? 10 : 9;
				}
			} else { //000000FF
				if ((value & 0x000000F0) != 0) {
					return (value & 0x000000C0) != 0 ? (value & 0x00000080) != 0 ? 8 : 7 : (value & 0x00000020) != 0 ? 6 : 5;
				} else { //0000000F
					return (value & 0x0000000C) != 0 ? (value & 0x00000008) != 0 ? 4 : 3 : (value & 0x00000002) != 0 ? 2 : 1;
				}
			}
		}
	}
}
