package adf.agent.communication.standard.bundle.information;

import adf.agent.communication.standard.bundle.StandardMessage;
import adf.agent.communication.standard.bundle.StandardMessagePriority;
import adf.component.communication.util.BitOutputStream;
import adf.component.communication.util.BitStreamReader;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.worldmodel.EntityID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MessageCivilian extends StandardMessage {
	private static final int SIZE_ID = 32;
	private static final int SIZE_HP = 14;
	private static final int SIZE_BURIEDNESS = 13;
	private static final int SIZE_DAMAGE = 14;
	private static final int SIZE_POSITION = 32;

	protected int rawAgentID;
	protected EntityID agentID;
	protected int rawHumanPosition;
	protected int humanHP;
	protected int humanBuriedness;
	protected int humanDamage;
	protected EntityID humanPosition;

	public MessageCivilian(boolean isRadio, @Nonnull Civilian civilian) {
		this(isRadio, StandardMessagePriority.NORMAL, civilian);
	}

	public MessageCivilian(boolean isRadio, StandardMessagePriority sendingPriority, @Nonnull Civilian civilian) {
		super(isRadio, sendingPriority);
		this.agentID = civilian.getID();
		this.humanHP = civilian.isHPDefined() ? civilian.getHP() : -1;
		this.humanBuriedness = civilian.isBuriednessDefined() ? civilian.getBuriedness() : -1;
		this.humanDamage = civilian.isDamageDefined() ? civilian.getDamage() : -1;
		this.humanPosition = civilian.isPositionDefined() ? civilian.getPosition() : null;
	}

	public MessageCivilian(boolean isRadio, int from, int ttl, @Nonnull BitStreamReader bitStreamReader) {
		super(isRadio, from, ttl, bitStreamReader);
		rawAgentID = bitStreamReader.getBits(SIZE_ID);
		this.humanHP = (bitStreamReader.getBits(1) == 1) ? bitStreamReader.getBits(SIZE_HP) : -1;
		this.humanBuriedness = (bitStreamReader.getBits(1) == 1) ? bitStreamReader.getBits(SIZE_BURIEDNESS) : -1;
		this.humanDamage = (bitStreamReader.getBits(1) == 1) ? bitStreamReader.getBits(SIZE_DAMAGE) : -1;
		this.rawHumanPosition = (bitStreamReader.getBits(1) == 1) ? bitStreamReader.getBits(SIZE_POSITION) : -1;
	}

	@Override
	public int getByteArraySize() {
		return this.toBitOutputStream().size();
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
		bitOutputStream.writeBits(this.agentID.getValue(), SIZE_ID);
		if (this.humanHP != -1) {
			bitOutputStream.writeBitsWithExistFlag(this.humanHP, SIZE_HP);
		} else {
			bitOutputStream.writeNullFlag();
		}
		if (this.humanBuriedness != -1) {
			bitOutputStream.writeBitsWithExistFlag(this.humanBuriedness, SIZE_BURIEDNESS);
		} else {
			bitOutputStream.writeNullFlag();
		}
		if (this.humanDamage != -1) {
			bitOutputStream.writeBitsWithExistFlag(this.humanDamage, SIZE_DAMAGE);
		} else {
			bitOutputStream.writeNullFlag();
		}
		if (this.humanPosition != null) {
			bitOutputStream.writeBitsWithExistFlag(this.humanPosition.getValue(), SIZE_POSITION);
		} else if (this.rawHumanPosition != -1) {
			bitOutputStream.writeBitsWithExistFlag(this.rawHumanPosition, SIZE_POSITION);
		} else {
			bitOutputStream.writeNullFlag();
		}
		return bitOutputStream;
	}

	@Nonnull
	public EntityID getAgentID() {
		if (this.agentID == null) {
			this.agentID = new EntityID(this.rawAgentID);
		}
		return this.agentID;
	}

	public int getHP() {
		return this.humanHP;
	}

	public int getBuriedness() {
		return this.humanBuriedness;
	}

	public int getDamage() {
		return this.humanDamage;
	}

	@Nullable
	public EntityID getPosition() {
		if (this.humanPosition == null) {
			if (this.rawHumanPosition != -1) this.humanPosition = new EntityID(this.rawHumanPosition);
		}
		return this.humanPosition;
	}

	public boolean isHPDefined() {
		return this.humanHP != -1;
	}

	public boolean isBuriednessDefined() {
		return this.humanBuriedness != -1;
	}

	public boolean isDamageDefined() {
		return this.humanDamage != -1;
	}

	public boolean isPositionDefined() {
		return (this.humanPosition != null || this.rawHumanPosition != -1);
	}

	@Override
	@Nonnull
	public String getCheckKey() {
		return getClass().getCanonicalName() + " > agent:" + this.getAgentID().getValue();
	}
}

